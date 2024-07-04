package com.sw.journal.journalcrawlerpublisher.controller;

import com.sw.journal.journalcrawlerpublisher.domain.Member;
import com.sw.journal.journalcrawlerpublisher.domain.MemberCreateForm;
import com.sw.journal.journalcrawlerpublisher.domain.UpdatePwForm;
import com.sw.journal.journalcrawlerpublisher.repository.MemberRepository;
import com.sw.journal.journalcrawlerpublisher.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
@RequestMapping("/members")
public class MemberController {
    private final MemberService memberService;
    private final PasswordEncoder passwordEncoder;
    private final MemberRepository memberRepository;

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
                    "username",
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

          // @Pattern 어노테이션으로 대체?
//        // 비밀번호에 숫자가 포함됐는지 확인
//        if(!memberCreateForm.getPassword().matches("^(?=.*[a-zA-Z])(?=.*[0-9]).+$")) {
//            bindingResult.rejectValue(
//                    "password",
//                    "passwordRuleError",
//                    "비밀번호는 영문자와 숫자를 조합해야 합니다."
//            );
//        }
    }

    @GetMapping("/login")
    public String login() {
        return "login_form";
    }

    @GetMapping("/mypage")
    public String mypage(UpdatePwForm updatePwForm) {
        return "mypage_form";
    }

    @PostMapping("/mypage")
    public String updatePassword(@Valid UpdatePwForm updatePwForm,
                                 BindingResult bindingResult,
                                 Model model) {
        // 현재 사용자 id를 가져옴
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        // 현재 사용자 비밀번호를 가져옴
        Optional<Member> member = memberRepository.findByUsername(currentUsername);
        Member currentMember = member.get();
        String currentPassword = member.get().getPassword();

        // 1. Form 데이터 검증
        // 입력값 내용 검사
            if (!updatePwForm.getNewPassword().equals(updatePwForm.getConfirmPassword())) {
                bindingResult.rejectValue(
                        "confirmPassword",
                        "newPasswordCheckError",
                        "비밀번호가 일치하지 않습니다."
                );
            }

            if (!passwordEncoder.matches(updatePwForm.getCurrentPassword(), currentPassword)) {
                bindingResult.rejectValue(
                        "confirmPassword",
                        "passwordCheckError",
                        "잘못된 비밀번호입니다."
                );
            }

        // 입력값 바인딩 검사
        if (bindingResult.hasErrors()) {
            model.addAttribute("updatePwForm", updatePwForm);
            return "mypage_form";
        }

        // 비밀번호 변경
        currentMember.updatePassword(passwordEncoder.encode(updatePwForm.getNewPassword()));
        memberRepository.save(currentMember);
        return "redirect:/";
    }
}
