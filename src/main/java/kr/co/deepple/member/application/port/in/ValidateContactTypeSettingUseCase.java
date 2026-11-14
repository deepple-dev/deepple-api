package kr.co.deepple.member.application.port.in;

public interface ValidateContactTypeSettingUseCase {
  void validateContactTypeSetting(long memberId, String contactType);
}
