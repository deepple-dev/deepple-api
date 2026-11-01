package deepple.deepple.notification.command.domain;

import lombok.Getter;

@Getter
public enum NotificationType {
    MATCH_REQUEST("매치 요청"),
    MATCH_ACCEPT("매치 수락"),
    MATCH_REJECT("매치 거절"),

    PROFILE_EXCHANGE_REQUEST("프로필 교환 요청"),
    PROFILE_EXCHANGE_ACCEPT("프로필 교환 수락"),
    PROFILE_EXCHANGE_REJECT("프로필 교환 거절"),

    LIKE("좋아요 보내기"),

    PROFILE_IMAGE_CHANGE_REQUEST("프로필 이미지 변경 요청"),
    INTERVIEW_WRITE_REQUEST("인터뷰 작성 요청"),

    INAPPROPRIATE_PROFILE("부적절한 프로필 경고"),
    INAPPROPRIATE_PROFILE_IMAGE("부적절한 프로필 사진 경고"),
    INAPPROPRIATE_INTERVIEW("부적절한 인터뷰 경고"),
    INAPPROPRIATE_SELF_INTRODUCTION("부적절한 셀프소개 게시글 경고"),

    SCREENING_APPROVED("심사 승인"),
    SCREENING_REJECTED("심사 반려"),

    HEART_GRANTED("하트 지급"),

    INACTIVITY_REMINDER("장기 미로그인 알림");

    private final String description;

    NotificationType(String description) {
        this.description = description;
    }
}
