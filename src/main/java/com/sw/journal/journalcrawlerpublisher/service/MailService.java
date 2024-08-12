package com.sw.journal.journalcrawlerpublisher.service;

import com.sw.journal.journalcrawlerpublisher.domain.VerificationCode;
import com.sw.journal.journalcrawlerpublisher.repository.VerificationCodeRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
@Transactional
@RequiredArgsConstructor
public class MailService {
    private final VerificationCodeRepository verificationCodeRepository;

    private final JavaMailSender javaMailSender;
    private static final String senderEmail= "ekals0070@gmail.com";
    private static String code;

    // 인증번호 생성 후 verification_code 테이블에 저장하는 메서드
    public void createCode(String email) {
         code = String.format("%06d", new Random().nextInt(999999));
        // verification_code 테이블에 이메일과 인증번호 저장
        VerificationCode verificationCode = new VerificationCode();
        verificationCode.setEmail(email);
        verificationCode.setCode(code);
        verificationCode.setCreatedAt(LocalDateTime.now());
        verificationCodeRepository.save(verificationCode);
    }

    // 메일로 보낼 메시지 생성 메서드
    public MimeMessage createMail(String email) {
        // 인증번호 생성
        createCode(email);
        //
        MimeMessage message = javaMailSender.createMimeMessage();

        try {
            message.setFrom(senderEmail);
            message.setRecipients(MimeMessage.RecipientType.TO, email);
            message.setSubject("[IT SUBSCRIBE] Verify Your Email Address");

            // 0809 - wildmantle : StringBuilder를 사용하여 문자열 생성
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

            message.setText(body.toString(), "UTF-8", "html");
        } catch (MessagingException e) {
            e.printStackTrace();
        }

        return message;
    }

    // 메일 송신 메서드
    // 0809 - wildmantle : 매개변수 네이밍 컨벤션 통일
    public String sendMail(String email) {
        MimeMessage message = createMail(email);
        javaMailSender.send(message);

        return code;
    }

    // 인증번호 검증 메서드
    public boolean verifyCode(String email, String code) {
        // 사용자가 입력한 이메일로 인증번호가 발송됐는지 확인
        Optional<VerificationCode> optionalVerificationCode = verificationCodeRepository.findByEmail(email);
        // 발송됐을시 이메일로 발송한 인증번호와 사용자가 입력한 인증번호 비교
        if (optionalVerificationCode.isPresent()) {
            VerificationCode verificationCode = optionalVerificationCode.get();
            return verificationCode.getCode().equals(code);
        } // 사용자가 입력한 이메일로 인증번호가 발송되지 않았을 때
        else {
            return false;
        }
    }

    // 인증번호 삭제 메서드
    public void deleteCode(String email) {
        verificationCodeRepository.deleteByEmail(email);
    }
}
