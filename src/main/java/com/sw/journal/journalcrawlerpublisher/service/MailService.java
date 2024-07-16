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
            String body = "";
            body += "<h1>" + "Thank you for signing up with IT SUBSCRIBE. " + "</h1>";
            body += "<h1>" + "To complete your registration, please verify your email address by using the verification code below:" + "</h1>";
            body += "<br>";
            body += "<h1>" + "Your Verification Code: "+ code + "</h1>";
            body += "<br>";
            body += "<h3>" + "Please enter this code in the provided field on our website to confirm your email address." + "</h3>";
            body += "<h3>" + "If you did not sign up for an ExampleCo account, please ignore this email." + "</h3>";
            body += "<br>";
            body += "<h3>" + "Thank you," + "</h3>";
            body += "<h3>" + "The IT SUBSCRIBE Team" + "</h3>";
            message.setText(body,"UTF-8", "html");
        } catch (MessagingException e) {
            e.printStackTrace();
        }

        return message;
    }

    // 메일 송신 메서드
    public String sendMail(String mail) {
        MimeMessage message = createMail(mail);
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
