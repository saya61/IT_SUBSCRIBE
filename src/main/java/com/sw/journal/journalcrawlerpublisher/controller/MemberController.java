package com.sw.journal.journalcrawlerpublisher.controller;

import com.sw.journal.journalcrawlerpublisher.domain.*;
import com.sw.journal.journalcrawlerpublisher.repository.MemberRepository;
import com.sw.journal.journalcrawlerpublisher.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

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
        // 1. 입력값 내용 검사
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
        // 2. 입력값 바인딩 검사
        if(bindingResult.hasErrors()) {
            model.addAttribute("memberCreateForm", memberCreateForm);
            return "signup_form";
        }

//        // 백엔드 검증
//        try {
//            memberService.create(
//                    memberCreateForm.getUsername(), memberCreateForm.getNickname(),
//                    memberCreateForm.getEmail(), memberCreateForm.getPassword()
//            );
//        } catch (IllegalArgumentException e) {
//            bindingResult.reject(
//                    "signupFailed",
//                    "이미 가입된 계정입니다."
//            );
//            model.addAttribute("memberCreateForm", memberCreateForm);
//            return "signup_form";
//        } catch (Exception e) {
//            bindingResult.reject(
//                    "signupFailed",
//                    e.getMessage()
//            );
//            model.addAttribute("memberCreateForm", memberCreateForm);
//            return "signup_form";
//        }

        // 3. 회원 가입 성공
        return "redirect:/";
    }

    @GetMapping("/login")
    public String login() {
        return "login_form";
    }

    @GetMapping("/mypage")
    public String mypage(Model model) {
        // 사용자 인증
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        Optional<Member> member = memberRepository.findByUsername(currentUsername);
        if (member.isPresent()) {
            model.addAttribute("member", member.get());
            model.addAttribute("memberNicknameUpdateForm", new MemberNicknameUpdateForm());
            model.addAttribute("memberEmailUpdateForm", new MemberEmailUpdateForm());
            model.addAttribute("memberPwUpdateForm", new MemberPwUpdateForm());
        }
        return "mypage_form";
    }

    @PostMapping("/mypage/updateNickname")
    public String updateNickname(@Valid MemberNicknameUpdateForm memberNicknameUpdateForm,
                                 BindingResult bindingResult,
                                 Model model) {
        // 사용자 인증
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        Optional<Member> member = memberRepository.findByUsername(currentUsername);
        Member currentMember = member.get();

        // 1. 입력값 내용 검사
        // 변경하려는 닉네임이 이미 존재하면 중복 오류 반환
        if (memberService.existsByNickname(memberNicknameUpdateForm.getNickname())) {
            bindingResult.rejectValue("nickname", "nicknameDuplicationError", "이미 존재하는 닉네임입니다.");
        }

        // 2. 입력값 바인딩 검사
        if (bindingResult.hasErrors()) {
            model.addAttribute("member", currentMember);
            model.addAttribute("memberNicknameUpdateForm", memberNicknameUpdateForm);
            model.addAttribute("memberEmailUpdateForm", new MemberEmailUpdateForm());
            model.addAttribute("memberPwUpdateForm", new MemberPwUpdateForm());
            return "mypage_form";
        }

        // 3. 닉네임 변경
        if (!memberNicknameUpdateForm.getNickname().isEmpty()) {
            currentMember.setNickname(memberNicknameUpdateForm.getNickname());
        }
        memberRepository.save(currentMember);
        return "redirect:/";
    }

    @PostMapping("/mypage/updateEmail")
    public String updateEmail(@Valid MemberEmailUpdateForm memberEmailUpdateForm,
                              BindingResult bindingResult,
                              Model model) {
        // 사용자 인증
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        Optional<Member> member = memberRepository.findByUsername(currentUsername);
        Member currentMember = member.get();

        // 1. 입력값 내용 검사
        // 변경하려는 이메일이 이미 존재하면 중복 오류 반환
        if (memberService.existsByEmail(memberEmailUpdateForm.getEmail())) {
            bindingResult.rejectValue("email", "emailDuplicationError", "이미 가입된 이메일입니다.");
        }

        // 2. 입력값 바인딩 검사
        if (bindingResult.hasErrors()) {
            model.addAttribute("member", currentMember);
            model.addAttribute("memberNicknameUpdateForm", new MemberNicknameUpdateForm());
            model.addAttribute("memberEmailUpdateForm", memberEmailUpdateForm);
            model.addAttribute("memberPwUpdateForm", new MemberPwUpdateForm());
            return "mypage_form";
        }

        // 3. 이메일 변경
        if (!memberEmailUpdateForm.getEmail().isEmpty()) {
            currentMember.setEmail(memberEmailUpdateForm.getEmail());
        }
        memberRepository.save(currentMember);
        return "redirect:/";
    }

    @PostMapping("/mypage/updatePassword")
    public String updatePassword(@Valid MemberPwUpdateForm memberPwUpdateForm,
                                 BindingResult bindingResult,
                                 Model model) {
        // 사용자 인증
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        Optional<Member> member = memberRepository.findByUsername(currentUsername);
        Member currentMember = member.get();
        // 현재 로그인한 사용자의 비밀번호를 가져옴
        String currentPassword = currentMember.getPassword();

        // 1. 입력값 내용 검사
        // 변경 비밀번호, 변경 비밀번호 재입력값 비교
        if (!memberPwUpdateForm.getNewPassword().equals(memberPwUpdateForm.getConfirmPassword())) {
            bindingResult.rejectValue(
                    "confirmPassword",
                    "newPasswordCheckError",
                    "비밀번호가 일치하지 않습니다."
            );
        }
        // 현재 비밀번호값 비교
        if (!passwordEncoder.matches(memberPwUpdateForm.getCurrentPassword(), currentPassword)) {
            bindingResult.rejectValue(
                    "currentPassword",
                    "passwordCheckError",
                    "잘못된 비밀번호입니다."
            );
        }

        // 2. 입력값 바인딩 검사
        if(bindingResult.hasErrors()) {
            model.addAttribute("member", currentMember);
            model.addAttribute("memberNicknameUpdateForm", new MemberNicknameUpdateForm());
            model.addAttribute("memberEmailUpdateForm", new MemberEmailUpdateForm());
            model.addAttribute("memberPwUpdateForm", memberPwUpdateForm);
            return "mypage_form";
        }

        // 3. 비밀번호 변경
        if (!memberPwUpdateForm.getNewPassword().isEmpty()) {
            currentMember.setPassword(passwordEncoder.encode(memberPwUpdateForm.getNewPassword()));
        }
        memberRepository.save(currentMember);
        return "redirect:/";
    }


}
