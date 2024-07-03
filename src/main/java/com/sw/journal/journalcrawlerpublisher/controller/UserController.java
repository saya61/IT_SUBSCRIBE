package com.sw.journal.journalcrawlerpublisher.controller;

import com.sw.journal.journalcrawlerpublisher.domain.UserCreateForm;
import com.sw.journal.journalcrawlerpublisher.service.UserService;
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
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    @GetMapping("/signup")
    public String signup(UserCreateForm userCreateForm) {
        return "signup_form";
    }

    @PostMapping("/signup")
    public String createUser(
            @Valid UserCreateForm userCreateForm,
            BindingResult bindingResult,
            Model model
    ) {
        // 1. Form 데이터 검증
        // 입력값 내용 검사
        validateUserCreateForm(userCreateForm, bindingResult);
        // 입력값 바인딩 검사
        if(bindingResult.hasErrors()) {
            model.addAttribute("userCreateForm", userCreateForm);
            return "signup_form";
        }

        // 2. 백엔드 검증
        try {
            userService.create(
                    userCreateForm.getUserId(), userCreateForm.getUserNickname(),
                    userCreateForm.getUserEmail(), userCreateForm.getUserPw()
            );
        } catch (IllegalArgumentException e) {
            bindingResult.reject(
                    "signupFailed",
                    "이미 가입된 계정입니다."
            );
            model.addAttribute("userCreateForm", userCreateForm);
            return "signup_form";
        } catch (Exception e) {
            bindingResult.reject(
                    "signupFailed",
                    e.getMessage()
            );
            model.addAttribute("userCreateForm", userCreateForm);
            return "signup_form";
        }

        // 3. 회원 가입 성공
        return "redirect:/";
    }

    // 입력값 검사 메서드
    private void validateUserCreateForm(UserCreateForm userCreateForm, BindingResult bindingResult) {
        // 아이디 중복 확인
        if(userService.existsByUserId(userCreateForm.getUserId())) {
            bindingResult.rejectValue(
                    "userId",
                    "idDuplicationError",
                    "이미 존재하는 아이디입니다."
            );
        }

        // 닉네임 중복 확인
        if(userService.existsByUserNickname(userCreateForm.getUserNickname())) {
            bindingResult.rejectValue(
                    "userNickname",
                    "nickNameDuplicationError",
                    "이미 존재하는 닉네임입니다."
            );
        }

        // 이메일 중복 확인
        if(userService.existsByUserEmail(userCreateForm.getUserEmail())) {
            bindingResult.rejectValue(
                    "userEmail",
                    "EmailDuplicationError",
                    "이미 존재하는 이메일입니다."
            );
        }

        // 비밀번호, 비밀번호 재입력값 일치 확인
        if(!userCreateForm.getUserPw().equals(userCreateForm.getUserPw2())) {
            bindingResult.rejectValue(
                    "userPw2",
                    "passwordDoubleCheckError",
                    "비밀번호가 일치하지 않습니다."
            );
        }

        // 비밀번호에 숫자가 포함됐는지 확인
        if(!userCreateForm.getUserPw().matches("^(?=.*[a-zA-Z])(?=.*[0-9]).+$")) {
            bindingResult.rejectValue(
                    "userPw",
                    "passwordRuleError",
                    "비밀번호는 영문자와 숫자를 조합해야 합니다."
            );
        }
    }
}
