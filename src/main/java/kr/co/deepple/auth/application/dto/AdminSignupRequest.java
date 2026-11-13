package kr.co.deepple.auth.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record AdminSignupRequest(
    @NotBlank(message = "이메일을 입력해주세요.") @Email(message = "유효한 이메일 주소를 입력해주세요.") String email,
    @NotBlank(message = "비밀번호를 입력해주세요.")
        @Pattern(
            regexp =
                "^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[!@#$%^&*(),.?\":{}|<>])[A-Za-z\\d!@#$%^&*(),.?\":{}|<>]{10,20}$",
            message = "비밀번호는 영문, 숫자, 특수문자를 포함한 10-20자리여야 합니다.")
        String password,
    @NotBlank(message = "이름을 입력해주세요.")
        @Pattern(regexp = "^[a-zA-Z0-9가-힣]{1,10}$", message = "이름은 한글, 영문, 숫자를 포함하여 1-10자만 가능합니다.")
        String name,
    @NotBlank(message = "전화번호를 입력해주세요.")
        @Pattern(regexp = "^010\\d{8}$", message = "전화번호는 01012345678 형식이어야 합니다.")
        String phoneNumber) {}
