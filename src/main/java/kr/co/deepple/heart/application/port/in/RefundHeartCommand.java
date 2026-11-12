package kr.co.deepple.heart.application.port.in;

public record RefundHeartCommand(
    long memberId,
    long orderId,
    long amount
) {
}
