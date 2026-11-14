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
    boolean vip,
    boolean datingExamSubmitted) {

  private static final String ACTIVE = "ACTIVE";
  private static final String MALE = "MALE";
  private static final String FEMALE = "FEMALE";

  public boolean isActive() {
    return ACTIVE.equals(activityStatus);
  }

  public String requireNickname() {
    if (nickname == null) {
      throw new IllegalStateException("닉네임이 없는 회원입니다. memberId=" + memberId);
    }
    return nickname;
  }

  public String requireGender() {
    if (gender == null) {
      throw new IllegalStateException("성별이 없는 회원입니다. memberId=" + memberId);
    }
    return gender;
  }

  public String oppositeGender() {
    return MALE.equals(requireGender()) ? FEMALE : MALE;
  }
}
