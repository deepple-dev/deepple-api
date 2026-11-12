package kr.co.deepple.member.command.domain.introduction;

import kr.co.deepple.common.entity.BaseEntity;
import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.member.command.domain.MemberErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Entity
@Table(name = "member_introductions",
    indexes = {
        @Index(name = "idx_member_introductions_member_introduced", columnList = "memberId, introducedMemberId"),
    }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class MemberIntroduction extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long memberId;

    private Long introducedMemberId;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar(50)")
    private IntroductionType type;

    private MemberIntroduction(@NonNull Long memberId, @NonNull Long introducedMemberId,
        @NonNull IntroductionType type) {
        validateMemberId(memberId, introducedMemberId);
        this.memberId = memberId;
        this.introducedMemberId = introducedMemberId;
        this.type = type;
    }

    public static MemberIntroduction of(Long memberId, Long introducedMemberId, @NonNull IntroductionType type) {
        return new MemberIntroduction(memberId, introducedMemberId, type);
    }

    private void validateMemberId(@NonNull Long memberId, @NonNull Long introducedMemberId) {
        if (memberId.equals(introducedMemberId)) {
            throw new DeeppleException(MemberErrorCode.SELF_MEMBER_INTRODUCTION, "memberId=" + memberId);
        }
    }
}
