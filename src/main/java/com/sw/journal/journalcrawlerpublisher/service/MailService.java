package com.sw.journal.journalcrawlerpublisher.service;

import com.sw.journal.journalcrawlerpublisher.domain.*;
import com.sw.journal.journalcrawlerpublisher.dto.ArticleWithTagsDTO;
import com.sw.journal.journalcrawlerpublisher.repository.ImageRepository;
import com.sw.journal.journalcrawlerpublisher.repository.MemberRepository;
import com.sw.journal.journalcrawlerpublisher.repository.VerificationCodeRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
@Transactional
@RequiredArgsConstructor
public class MailService {
    private final VerificationCodeRepository verificationCodeRepository;
    // 이메일 전송을 처리하는 스프링의 JavaMailSender
    private final JavaMailSender javaMailSender;
    // 이메일 발신자 주소를 저장하는 상수
    private static final String senderEmail= "ekals0070@gmail.com";
    // 생성된 인증 코드를 저장하는 변수
    private static String code;
    private final MemberRepository memberRepository;
    private final ImageRepository imageRepository;

    // 인증번호를 생성하고 DB에 저장하는 메서드
    public void createCode(String email) {
        // 6자리 난수를 생성하여 인증번호로 사용
        code = String.format("%06d", new Random().nextInt(999999));

        // VerificationCode 객체 생성
        VerificationCode verificationCode = new VerificationCode();
        verificationCode.setEmail(email); // 사용자가 입력한 이메일 설정
        verificationCode.setCode(code); // 인증번호 설정
        verificationCode.setCreatedAt(LocalDateTime.now()); // 인증번호 생성 시간 설정

        // 생성된 VerificationCode 객체를 DB에 저장
        verificationCodeRepository.save(verificationCode);
    }

    // 인증 번호 발송 이메일의 MimeMessage 객체 생성 메서드
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

    // 사용자가 입력한 인증번호 검증 메서드
    public boolean verifyCode(String email, String code) {
        // 사용자가 입력한 이메일로 DB 조회
        Optional<VerificationCode> optionalVerificationCode = verificationCodeRepository.findByEmail(email);

        // DB 조회가 성공할 경우
        // 이메일로 발송한 인증 코드와 사용자가 입력한 코드 비교
        if (optionalVerificationCode.isPresent()) {
            VerificationCode verificationCode = optionalVerificationCode.get();
            return verificationCode.getCode().equals(code);
        }
        // DB 조회가 실패한 경우
        // false 반환 (잘못된 이메일이거나 코드가 발송되지 않았음)
        else {
            return false;
        }
    }

    // 인증번호 삭제 메서드 (인증이 완료된 경우 호출)
    public void deleteCode(String email) {
        // DB 에서 해당 이메일로 발송한 인증번호 삭제
        verificationCodeRepository.deleteByEmail(email);
    }

    // 신작 기사 알람 이메일의 MimeMessage 객체 생성 메서드
    public MimeMessage createAlarmMail(String email, ArticleWithTagsDTO articleDTO) {
        // MimeMessage 객체 생성
        MimeMessage message = javaMailSender.createMimeMessage();

        // 이메일 수신자의 닉네임을 가져옴
        Optional<Member> member = memberRepository.findByEmail(email);
        String nickname = member.get().getNickname();

        // 신작 기사의 카테고리, 제목, 이미지, 링크를 가져옴
        String newCategory = articleDTO.getCategory().getName();
        String title = articleDTO.getTitle();
        Image image = imageRepository.findByArticleId(articleDTO.getId()).get(0);
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
            body.append("<p>We’ve just published a new article in your favorite category: ").append(newCategory).append("</p>");
            body.append("<p>Here’s what’s new:</p>");
            body.append("<br>");
            body.append("<div style='margin-bottom: 20px;'>");
            body.append("<h2>").append(title).append("</h2>");
//            기사 링크는 나중에 추가
//            body.append("<a href='").append(article.getLink()).append("' style='display: inline-block; padding: 10px 15px; font-size: 16px; color: #ffffff; background-color: #007bff; text-decoration: none; border-radius: 4px;'>Read more</a>");
            if (image != null) {
                body.append("<br>");
                body.append("<img src='").append(image.getImgUrl()).append("' alt='Article Image' style='max-width: 100%; height: auto; border-radius: 4px;'>");
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
    public void sendAlarmMail(String email, ArticleWithTagsDTO articleDTO) {
        MimeMessage message = createAlarmMail(email, articleDTO); // 이메일 생성
        javaMailSender.send(message); // 이메일 발송
    }
}
