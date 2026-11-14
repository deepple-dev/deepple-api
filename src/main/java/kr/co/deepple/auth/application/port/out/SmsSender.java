package kr.co.deepple.auth.application.port.out;

public interface SmsSender {
  void sendMessage(String message, String phoneNumber);
}
