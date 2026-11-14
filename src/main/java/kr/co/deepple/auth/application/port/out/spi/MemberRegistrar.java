package kr.co.deepple.auth.application.port.out.spi;

public interface MemberRegistrar {

  void register(long accountId, String phoneNumber);
}
