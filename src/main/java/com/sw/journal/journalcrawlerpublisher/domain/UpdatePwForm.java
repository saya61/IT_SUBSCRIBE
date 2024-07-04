package com.sw.journal.journalcrawlerpublisher.domain;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdatePwForm {  // DTO 클래스 역할을 하는 Form 클래스
    @NotEmpty(message = "현재 비밀번호를 입력해주세요.")
    private String currentPassword;

    @NotEmpty(message = "변경할 비밀번호를 입력해주세요.")
    @Pattern(regexp = "(?=.*[a-zA-z])(?=.*[0-9]).+$", message = "비밀번호는 영문자와 숫자를 조합해야 합니다.")
    @Size(min = 8, max = 16)
    private String newPassword;

    @NotEmpty(message = "비밀번호를 다시 입력해주세요.")
    private String confirmPassword;
}
