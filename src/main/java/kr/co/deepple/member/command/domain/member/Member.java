package kr.co.deepple.member.command.domain.member;

import kr.co.deepple.common.entity.SoftDeleteBaseEntity;
import kr.co.deepple.common.event.Events;
import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.heart.command.domain.hearttransaction.vo.HeartBalance;
import kr.co.deepple.member.command.domain.member.event.*;
import kr.co.deepple.member.command.domain.MemberErrorCode;
import kr.co.deepple.member.command.domain.member.vo.KakaoId;
import kr.co.deepple.member.command.domain.member.vo.MemberProfile;
import kr.co.deepple.member.command.domain.member.vo.PhoneNumber;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "members", indexes = {
    @Index(name = "idx_deleted_at", columnList = "deletedAt")
})
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Member extends SoftDeleteBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter
    private Long id;

    @Embedded
    private PhoneNumber phoneNumber;

    @Embedded
    private KakaoId kakaoId;

    @Embedded
    @Getter
    private MemberProfile profile;

    @Getter
    private boolean isVip;

    @Getter
    @Builder.Default
    private boolean isProfilePublic = false;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar(50)")
    @Getter
    private Grade grade;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar(50)")
    @Getter
    private ActivityStatus activityStatus;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar(50)")
    @Getter
    private PrimaryContactType primaryContactType;

    @Embedded
    @Getter
    private HeartBalance heartBalance;

    @Getter
    @Builder.Default
    private boolean isDatingExamSubmitted = false;

    @Getter
    private LocalDateTime lastAccessedAt;

    public static Member fromPhoneNumber(@NonNull String phoneNumber) {
        return Member.builder()
            .phoneNumber(PhoneNumber.from(phoneNumber))
            .activityStatus(ActivityStatus.INITIAL)
            .heartBalance(HeartBalance.init())
            .isVip(false)
            .primaryContactType(PrimaryContactType.NONE)
            .grade(Grade.SILVER)
            .build();
    }

    public void updateProfile(@NonNull MemberProfile profile) {
        this.profile = profile;

        if (!isProfileSettingNeeded() && activityStatus == ActivityStatus.INITIAL) {
            Events.raise(MemberProfileInitializedEvent.from(id));
        }
    }

    public boolean isProfileSettingNeeded() {
        if (profile == null) {
            return true;
        }
        return profile.isProfileSettingNeeded();
    }

    public String getKakaoId() {
        if (kakaoId == null) {
            return null;
        }
        return kakaoId.getValue();
    }

    public String getPhoneNumber() {
        return phoneNumber.getValue();
    }

    private void changeToDormant() {
        activityStatus = ActivityStatus.DORMANT;
        Events.raise(MemberBecameDormantEvent.from(id));
    }

    private void changeToActive() {
        activityStatus = ActivityStatus.ACTIVE;
        Events.raise(MemberBecameActiveEvent.from(id));
    }

    public void deactivate() {
        if (!isActive()) {
            throw new DeeppleException(
                MemberErrorCode.INVALID_ACTIVITY_STATUS_CHANGE,
                "활동중인 회원만 휴면으로 전환할 수 있습니다." + " memberId=" + id + ", activityStatus=" + activityStatus
            );
        }
        changeToDormant();
    }

    public void reactivate() {
        if (!isDormant()) {
            throw new DeeppleException(
                MemberErrorCode.INVALID_ACTIVITY_STATUS_CHANGE,
                "휴면 회원만 휴면을 해제할 수 있습니다." + " memberId=" + id + ", activityStatus=" + activityStatus
            );
        }
        changeToActive();
    }

    public void approveScreening() {
        changeToActive();
    }

    public void changeToRejected() {
        activityStatus = ActivityStatus.REJECTED_SCREENING;
    }

    public void changeToWaiting() {
        activityStatus = ActivityStatus.WAITING_SCREENING;
    }

    public void changePrimaryContactTypeToKakao(KakaoId kakaoId) {
        this.kakaoId = kakaoId;
        this.primaryContactType = PrimaryContactType.KAKAO;
    }

    public void changePrimaryContactTypeToPhoneNumber(@NonNull String phoneNumber) {
        this.phoneNumber = PhoneNumber.from(phoneNumber);
        this.primaryContactType = PrimaryContactType.PHONE_NUMBER;
    }

    public boolean isActive() {
        return activityStatus == ActivityStatus.ACTIVE;
    }

    public boolean isDormant() {
        return activityStatus == ActivityStatus.DORMANT;
    }

    public Gender getGender() {
        return profile.getGender();
    }

    public void useHeart(long amount) {
        heartBalance = heartBalance.useHeart(amount);
    }

    public void gainPurchaseHeart(long amount) {
        heartBalance = heartBalance.gainPurchaseHeart(amount);
        Events.raise(PurchaseHeartGainedEvent.of(id, amount, heartBalance.getMissionHeartBalance(),
            heartBalance.getPurchaseHeartBalance()));
    }

    public void gainMissionHeart(long amount, String actionType) {
        heartBalance = heartBalance.gainMissionHeart(amount);
        Events.raise(MissionHeartGainedEvent.of(id, amount, heartBalance.getMissionHeartBalance(),
            heartBalance.getPurchaseHeartBalance(), actionType));
    }

    public void gainAdminGrantedHeart(long amount, Long adminId, String reason) {
        heartBalance = heartBalance.gainMissionHeart(amount);
        Events.raise(AdminHeartGrantedEvent.of(id, adminId, amount,
            heartBalance.getMissionHeartBalance(), heartBalance.getPurchaseHeartBalance(), reason));
    }

    public void refundPurchaseHeart(long amount) {
        heartBalance = heartBalance.refundPurchaseHeart(amount);
    }

    public void updateGrade(@NonNull Grade grade) {
        this.grade = grade;
    }

    public void updateSetting(
        @NonNull Grade grade,
        boolean isVip,
        boolean isPushNotificationEnabled
    ) {
        this.grade = grade;
        this.isVip = isVip;
        Events.raise(MemberSettingUpdatedEvent.of(id, isPushNotificationEnabled));
    }

    public void publishProfile() {
        isProfilePublic = true;
    }

    public void nonPublishProfile() {
        isProfilePublic = false;
    }

    public void changeActivityStatus(@NonNull ActivityStatus activityStatus) {
        switch (activityStatus) {
            case ActivityStatus.DORMANT -> changeToDormant();
            case ActivityStatus.ACTIVE -> changeToActive();
            default -> this.activityStatus = activityStatus;
        }
    }

    public void markDatingExamSubmitted() {
        if (isDatingExamSubmitted) {
            throw new IllegalStateException("이미 연애 모의고사를 제출한 멤버입니다. member id: " + id);
        }
        isDatingExamSubmitted = true;
    }

    public boolean hasSubmittedDatingExam() {
        return isDatingExamSubmitted;
    }

    @Override
    public void delete() {
        this.activityStatus = ActivityStatus.DELETED;
        super.delete();
    }

    public boolean hasSameGender(Member other) {
        return this.getGender() == other.getGender();
    }
}
