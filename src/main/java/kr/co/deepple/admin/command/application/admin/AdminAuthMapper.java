package kr.co.deepple.admin.command.application.admin;

import kr.co.deepple.admin.command.domain.admin.*;
import kr.co.deepple.admin.presentation.admin.dto.AdminLoginResponse;
import kr.co.deepple.admin.presentation.admin.dto.AdminSignupRequest;
import kr.co.deepple.admin.presentation.admin.dto.AdminSignupResponse;
import lombok.NoArgsConstructor;

import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public class AdminAuthMapper {

    public static Admin toAdmin(AdminSignupRequest request, Password password) {
        return Admin.builder()
            .email(Email.from(request.email()))
            .password(password)
            .name(Name.from(request.name()))
            .phoneNumber(PhoneNumber.from(request.phoneNumber()))
            .build();
    }

    public static AdminSignupResponse toSignupResponse(Admin admin) {
        return new AdminSignupResponse(
            admin.getId(),
            admin.getEmail().getAddress(),
            admin.getName().getValue(),
            admin.getPhoneNumber().getValue()
        );
    }

    public static AdminLoginResponse toLoginResponse(String accessToken, String refreshToken) {
        return new AdminLoginResponse(accessToken, refreshToken);
    }
}
