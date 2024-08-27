package com.sw.journal.journalcrawlerpublisher.service;

import com.sw.journal.journalcrawlerpublisher.domain.*;
import com.sw.journal.journalcrawlerpublisher.dto.VerificationDTO;
import com.sw.journal.journalcrawlerpublisher.repository.*;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
@Transactional
@RequiredArgsConstructor
public class MailService {
    // 이메일 전송을 처리하는 스프링의 JavaMailSender
    private final JavaMailSender javaMailSender;
    // 이메일 발신자 주소를 저장하는 상수
    @Value("${spring.mail.username}")
    private static String senderEmail;
    // 생성된 인증 코드를 저장하는 변수
    private static String code;
    private final MemberRepository memberRepository;
    private final ImageRepository imageRepository;
    private final ArticleRepository articleRepository;
    private final KafkaTemplate<String, VerificationDTO> verificationKafkaTemplate;

    // Kafka 에서 메시지를 캐싱해와 저장하는 변수
    // ConcurrentMap 을 사용하는 이유 :
    // 멀티스레드 환경에서의 안전한 데이터 공유 가능, 동시성 문제 예방
    // 동시에 많은 스레드가 안전하게 데이터에 접근 가능하고 업데이트할 수 있음 (동시에 같은 이메일로 데이터를 읽거나 쓸 때 발생할 수 있는 충돌 방지)
    private ConcurrentMap<String, VerificationDTO> verificationCodeCache = new ConcurrentHashMap<>();

    // 인증번호를 생성하고 Kafka 로 전송하는 메서드
    public void createCode(String email) {
        // 6자리 난수를 생성하여 인증번호로 사용
        code = String.format("%06d", new Random().nextInt(999999));

        // 인증 번호 DTO 생성 및 필드 설정
        VerificationDTO verificationDTO = new VerificationDTO();
        verificationDTO.setEmail(email); // 사용자가 입력한 이메일 설정
        verificationDTO.setCode(code); // 인증번호 설정
        verificationDTO.setCreatedAt(LocalDateTime.now()); // 인증번호 생성 시간 설정

        // Kafka로 이벤트 발행
        verificationKafkaTemplate.send("verification_topic", verificationDTO);
    }

    // 인증 번호 발송 이메일의 메시지 내용 생성 메서드
    public MimeMessage createVerificationMail(String email) {
        // 인증번호 생성 및 DB에 저장
        createCode(email);
        // MimeMessage 객체 생성
        MimeMessage message = javaMailSender.createMimeMessage();

        try {
            // 이메일 발신자 설정
            message.setFrom(senderEmail);
            // 이메일 수신자 설정
            message.setRecipients(MimeMessage.RecipientType.TO, email);
            // 이메일 제목 설정
            message.setSubject("[IT SUBSCRIBE] Verify Your Email Address");

            // 이메일 본문 내용 설정
            StringBuilder body = new StringBuilder();
            body.append("<h1>").append("Thank you for signing up with IT SUBSCRIBE. ").append("</h1>");
            body.append("<h1>").append("To complete your registration, please verify your email address by using the verification code below:").append("</h1>");
            body.append("<br>");
            body.append("<h1>").append("Your Verification Code: ").append(code).append("</h1>");
            body.append("<br>");
            body.append("<h3>").append("Please enter this code in the provided field on our website to confirm your email address.").append("</h3>");
            body.append("<h3>").append("If you did not sign up for an ExampleCo account, please ignore this email.").append("</h3>");
            body.append("<br>");
            body.append("<h3>").append("Thank you,").append("</h3>");
            body.append("<h3>").append("The IT SUBSCRIBE Team").append("</h3>");

            // 이메일 본문을 HTML 형식으로 설정
            message.setText(body.toString(), "UTF-8", "html");
        } catch (MessagingException e) {
            // 이메일 생성 중 예외가 발생하면 스택 트레이스를 출력하여 디버깅
            e.printStackTrace();
        }

        // 생성된 MimeMessage 객체 반환
        return message;
    }

    // 인증 번호 이메일 발송 메서드
    public String sendVerificationMail(String email) {
        MimeMessage message = createVerificationMail(email);
        javaMailSender.send(message);
        // 전송한 인증번호 반환
        return code;
    }

    // Kafka 에서 인증 번호에 대한 메시지를 수신하는 메서드
    @KafkaListener(topics = "verification_topic"
            , groupId = "verification_consumer_group"
            // 수신한 JSON 메시지를 VerificationDTO 로 자동으로 역직렬화
            , properties = {"spring.json.value.default.type:com.sw.journal.journalcrawlerpublisher.dto.VerificationDTO"}
            // Listener 가 어느 컨테이너 팩토리를 사용할지 명시적으로 지정 (알람 기능과는 다른 컨테이너 팩토리를 사용하기 때문)
            , containerFactory = "verificationKafkaListenerContainerFactory")
    public void listenVerificationCode(VerificationDTO verificationDTO, Acknowledgment ack) {
        // 이메일을 키로 하여 Kafka 의 메시지를 캐시
        // 동일한 이메일로 여러 개의 인증번호가 발급될 경우 가장 최신 것으로 덮어씀
        verificationCodeCache.put(verificationDTO.getEmail(), verificationDTO);
        // 수동 오프셋 커밋
        ack.acknowledge();
    }

    // 사용자가 입력한 인증번호 검증 메서드
    public boolean verifyCode(String email, String code) {
        // Kafka 에서 캐싱한 메시지(VerificationDTO)를 사용자 이메일로 조회
        VerificationDTO storedCodeDTO = verificationCodeCache.get(email);
        // 사용자 이메일로 조회한 메시지에서 인증번호 캐싱 -> 사용자가 입력한 인증번호와 일치하는지 확인
        // 일치할시 true 반환, 불일치할시 false 반환
        return storedCodeDTO.getCode().equals(code);
    }

    // 신작 기사 알람 이메일의 메시지 내용 생성 메서드
    public MimeMessage createAlarmMail(String email, Long articleId) {
        // MimeMessage 객체 생성
        MimeMessage message = javaMailSender.createMimeMessage();

        // 이메일 수신자의 닉네임 조회
        Optional<Member> member = memberRepository.findByEmail(email);
        String nickname = member.get().getNickname();

        // 기사 id로 기사 조회
        Optional<Article> article = articleRepository.findById(articleId);

        // 신작 기사의 카테고리, 제목, 이미지, 링크를 가져옴
        String newCategory = article.get().getCategory().getName();
        String title = article.get().getTitle();
        List<Image> images = imageRepository.findByArticleId(articleId);
        // TO DO : 링크 가져오는 코드 나중에 추가

        try {
            // 이메일 발신자 설정
            message.setFrom(senderEmail);
            // 이메일 수신자 설정
            message.setRecipients(MimeMessage.RecipientType.TO, email);
            // 이메일 제목 설정
            message.setSubject("[IT SUBSCRIBE] New Articles Just for You!");

            // 이메일 본문 내용 설정
            StringBuilder body = new StringBuilder();
            body.append("<h1>Hi ").append(nickname).append(",</h1>");
            body.append("<h3>We’ve just published a new article in your favorite category: ").append(newCategory).append("</h3>");
            body.append("<h3>Here’s what’s new:</h3>");
            body.append("<br>");
            body.append("<div style='margin-bottom: 20px;'>");
            body.append("<h2>").append(title).append("</h2>");
//            기사 링크는 나중에 추가
//            body.append("<a href='").append(article.getLink()).append("' style='display: inline-block; padding: 10px 15px; font-size: 16px; color: #ffffff; background-color: #007bff; text-decoration: none; border-radius: 4px;'>Read more</a>");
            // 이미지가 있는 경우 이메일 내용에 추가
            if (!images.isEmpty()) {
                Image image = images.get(0);
                body.append("<br>");
                body.append("<img src='").append(image.getImgUrl()).append("' alt='Article Image' style='max-width: 100%; height: auto; border-radius: 4px;'>");
            } else {
                // 이미지가 없을 경우 대체 텍스트나 기본 이미지 추가 (선택사항)
                body.append("<p>No image available for this article.</p>");
            }
            body.append("</div>");
            body.append("<br>");
            body.append("<h3>").append("Thank you,").append("</h3>");
            body.append("<h3>").append("The IT SUBSCRIBE Team").append("</h3>");

            // 이메일 본문을 HTML 형식으로 설정
            message.setText(body.toString(), "UTF-8", "html");
        } catch (MessagingException e) {
            // 이메일 생성 중 예외가 발생하면 스택 트레이스를 출력하여 디버깅
            e.printStackTrace();
        }

        // 생성된 MimeMessage 객체 반환
        return message;
    }

    // 신작 기사 알람 이메일 발송 메서드
    public void sendAlarmMail(String email, Long articleId) {
        MimeMessage message = createAlarmMail(email, articleId); // 이메일 생성
        javaMailSender.send(message); // 이메일 발송
    }
}
