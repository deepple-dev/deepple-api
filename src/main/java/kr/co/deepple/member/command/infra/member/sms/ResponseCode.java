package kr.co.deepple.member.command.infra.member.sms;

import lombok.Getter;

@Getter
public enum ResponseCode {
    SUCCESS(200), EXPIRED_TOKEN(403);

    private final int code;

    ResponseCode(int code) {
        this.code = code;
    }
}
