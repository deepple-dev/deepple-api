package kr.co.deepple.heart.application.port.in;

public record AddPurchaseHeartCommand(
    long memberId,
    long orderId,
    long amount
) {
}
