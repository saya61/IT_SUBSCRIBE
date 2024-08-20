package com.sw.journal.journalcrawlerpublisher.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sw.journal.journalcrawlerpublisher.domain.Member;
import com.sw.journal.journalcrawlerpublisher.dto.VerificationDTO;
import com.sw.journal.journalcrawlerpublisher.repository.MemberRepository;
import com.sw.journal.journalcrawlerpublisher.service.MailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/members")
public class MailController {
    private final MailService mailService;
    private final MemberRepository memberRepository;
    private final ObjectMapper jacksonObjectMapper;

    // 인증 이메일 전송
    @PostMapping("/send-code")
    public HashMap<String, Object> sendMail(
            // body에 {"email" :"사용자 이메일"}로 전송
            @RequestBody String emailRequest) throws IOException {
        // 응답 메시지
        HashMap<String, Object> map = new HashMap<>();

        // JSON 파싱
        JsonNode jsonNode = jacksonObjectMapper.readTree(emailRequest);
        String email = jsonNode.get("email").asText();

        // 인증 번호 발송 성공
        try {
            String code = mailService.sendVerificationMail(email);
            map.put("success", Boolean.TRUE);
            map.put("code", code);
        // 인증 번호 발송 실패
        } catch (Exception e) {
            map.put("success", Boolean.FALSE);
            map.put("error", e.getMessage());
        }

        return map;
    }

    // 이메일 인증 번호 검증 (회원가입 할 때)
    @PostMapping("/verify-code-signup")
    public ResponseEntity<?> verifyCodeToSignUp(
            // 사용자가 입력한 email, 인증번호를 body로 받음
            @RequestBody VerificationDTO request) throws IOException {
        // 인증 번호와 사용자 입력 코드 비교
        if (!mailService.verifyCode(request.getEmail(), request.getCode())) {
            return ResponseEntity.badRequest().body("인증코드가 일치하지 않습니다.");
        }

        // 인증 성공 후 DB에서 인증번호 삭제
        mailService.deleteCode(request.getEmail());
        return ResponseEntity.ok("인증이 완료되었습니다.");
    }

    // 이메일 인증 코드 검증 (비밀번호 재설정할 때)
    @PostMapping("/verify-code-change-pw")
    public ResponseEntity<?> verifyCodeToChangePw(
            // 사용자가 입력한 email, 인증번호를 body로 받음
            @RequestBody VerificationDTO request) throws IOException {
        Map<String, String> response = new HashMap<>();

        // 인증 번호와 사용자 입력 코드 비교
        if (!mailService.verifyCode(request.getEmail(), request.getCode())) {
            return ResponseEntity.badRequest().body("인증코드가 일치하지 않습니다.");
        }

        // 인증 성공 후 DB에서 인증번호 삭제
        mailService.deleteCode(request.getEmail());

        // 사용자가 입력한 email을 통해 사용자 id를 찾음
        Optional<Member> member = memberRepository.findByEmail(request.getEmail());
        if (member.isEmpty()) {
            return ResponseEntity.badRequest().body("사용자를 찾을 수 없습니다.");
        } else {
            response.put("message", "인증이 완료되었습니다.");
            response.put("id", member.get().getUsername());
        }
        return ResponseEntity.ok(response);
    }
}
