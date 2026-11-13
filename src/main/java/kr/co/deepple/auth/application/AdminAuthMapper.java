package kr.co.deepple.auth.application;

import kr.co.deepple.auth.application.dto.AdminLoginResponse;
import kr.co.deepple.auth.application.dto.AdminSignupResponse;
import kr.co.deepple.auth.application.port.in.SignupAdminCommand;
import kr.co.deepple.auth.domain.Admin;
import kr.co.deepple.auth.domain.Email;
import kr.co.deepple.auth.domain.Name;
import kr.co.deepple.auth.domain.Password;
import kr.co.deepple.auth.domain.PhoneNumber;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AdminAuthMapper {

  public static Admin toAdmin(SignupAdminCommand command, Password password) {
    return Admin.builder()
        .email(Email.from(command.email()))
        .password(password)
        .name(Name.from(command.name()))
        .phoneNumber(PhoneNumber.from(command.phoneNumber()))
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
