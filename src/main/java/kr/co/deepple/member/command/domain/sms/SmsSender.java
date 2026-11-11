package kr.co.deepple.member.command.domain.sms;

public interface SmsSender {

    void sendMessage(String message, String phoneNumber);
}
