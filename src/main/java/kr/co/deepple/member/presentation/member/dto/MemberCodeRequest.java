package kr.co.deepple.member.presentation.member.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record MemberCodeRequest(
    @NotBlank(message = "휴대폰 번호는 필수입니다.")
        @Pattern(regexp = "^010\\d{8}$", message = "010으로 시작하고 8자리의 숫자가 와야합니다.")
        String phoneNumber) {}
