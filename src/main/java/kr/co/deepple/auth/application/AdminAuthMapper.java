package kr.co.deepple.auth.application;

import kr.co.deepple.auth.application.dto.AdminLoginResponse;
import kr.co.deepple.auth.application.dto.AdminSignupRequest;
import kr.co.deepple.auth.application.dto.AdminSignupResponse;
import kr.co.deepple.auth.domain.Admin;
import kr.co.deepple.auth.domain.Email;
import kr.co.deepple.auth.domain.Name;
import kr.co.deepple.auth.domain.Password;
import kr.co.deepple.auth.domain.PhoneNumber;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
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
        admin.getPhoneNumber().getValue());
  }

  public static AdminLoginResponse toLoginResponse(String accessToken, String refreshToken) {
    return new AdminLoginResponse(accessToken, refreshToken);
  }
}
