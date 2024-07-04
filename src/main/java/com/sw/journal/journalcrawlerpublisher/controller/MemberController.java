package com.sw.journal.journalcrawlerpublisher.controller;

import com.sw.journal.journalcrawlerpublisher.domain.MemberCreateForm;
import com.sw.journal.journalcrawlerpublisher.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/members")
public class MemberController {
    private final MemberService memberService;

    @GetMapping("/signup")
    public String signup(MemberCreateForm memberCreateForm) {
        return "signup_form";
    }

    @PostMapping("/signup")
    public String createMember(
            @Valid MemberCreateForm memberCreateForm,
            BindingResult bindingResult,
            Model model
    ) {
        // 1. Form 데이터 검증
        // 입력값 내용 검사
        validateMemberCreateForm(memberCreateForm, bindingResult);
        // 입력값 바인딩 검사
        if(bindingResult.hasErrors()) {
            model.addAttribute("memberCreateForm", memberCreateForm);
            return "signup_form";
        }

        // 2. 백엔드 검증
        try {
            memberService.create(
                    memberCreateForm.getUsername(), memberCreateForm.getNickname(),
                    memberCreateForm.getEmail(), memberCreateForm.getPassword()
            );
        } catch (IllegalArgumentException e) {
            bindingResult.reject(
                    "signupFailed",
                    "이미 가입된 계정입니다."
            );
            model.addAttribute("memberCreateForm", memberCreateForm);
            return "signup_form";
        } catch (Exception e) {
            bindingResult.reject(
                    "signupFailed",
                    e.getMessage()
            );
            model.addAttribute("memberCreateForm", memberCreateForm);
            return "signup_form";
        }

        // 3. 회원 가입 성공
        return "redirect:/";
    }

    // 입력값 검사 메서드
    private void validateMemberCreateForm(MemberCreateForm memberCreateForm, BindingResult bindingResult) {
        // 아이디 중복 확인
        if(memberService.existsByUsername(memberCreateForm.getUsername())) {
            bindingResult.rejectValue(
                    "userName",
                    "idDuplicationError",
                    "이미 존재하는 아이디입니다."
            );
        }

        // 닉네임 중복 확인
        if(memberService.existsByNickname(memberCreateForm.getNickname())) {
            bindingResult.rejectValue(
                    "nickname",
                    "nickNameDuplicationError",
                    "이미 존재하는 닉네임입니다."
            );
        }

        // 이메일 중복 확인
        if(memberService.existsByEmail(memberCreateForm.getEmail())) {
            bindingResult.rejectValue(
                    "email",
                    "EmailDuplicationError",
                    "이미 존재하는 이메일입니다."
            );
        }

        // 비밀번호, 비밀번호 재입력값 일치 확인
        if(!memberCreateForm.getPassword().equals(memberCreateForm.getPassword2())) {
            bindingResult.rejectValue(
                    "password2",
                    "passwordDoubleCheckError",
                    "비밀번호가 일치하지 않습니다."
            );
        }

        // 비밀번호에 숫자가 포함됐는지 확인
        if(!memberCreateForm.getPassword().matches("^(?=.*[a-zA-Z])(?=.*[0-9]).+$")) {
            bindingResult.rejectValue(
                    "password",
                    "passwordRuleError",
                    "비밀번호는 영문자와 숫자를 조합해야 합니다."
            );
        }
    }

    @GetMapping("/login")
    public String login() {
        return "login_form";
    }
}
