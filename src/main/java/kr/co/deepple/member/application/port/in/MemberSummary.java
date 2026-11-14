package kr.co.deepple.member.application.port.in;

public record MemberSummary(
    long memberId,
    String nickname,
    String gender,
    Integer age,
    String city,
    String district,
    String grade,
    String activityStatus,
    boolean vip) {

  private static final String ACTIVE = "ACTIVE";

  public boolean isActive() {
    return ACTIVE.equals(activityStatus);
  }
}
