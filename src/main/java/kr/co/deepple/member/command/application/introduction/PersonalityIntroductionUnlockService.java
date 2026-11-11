package kr.co.deepple.member.command.application.introduction;

import kr.co.deepple.block.application.required.BlockRepository;
import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.datingexam.domain.AnswerPersonalityType;
import kr.co.deepple.member.command.domain.MemberErrorCode;
import kr.co.deepple.member.command.domain.introduction.IntroductionType;
import kr.co.deepple.member.command.domain.introduction.MemberIntroduction;
import kr.co.deepple.member.command.domain.introduction.MemberIntroductionCommandRepository;
import kr.co.deepple.member.command.domain.member.Member;
import kr.co.deepple.member.command.domain.member.MemberCommandRepository;
import kr.co.deepple.member.query.introduction.intra.PersonalityIntroductionOpenState;
import kr.co.deepple.member.query.introduction.intra.PersonalityIntroductionRedisRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class PersonalityIntroductionUnlockService {
    private final MemberCommandRepository memberCommandRepository;
    private final MemberIntroductionCommandRepository memberIntroductionCommandRepository;
    private final BlockRepository blockRepository;
    private final PersonalityIntroductionRedisRepository personalityIntroductionRedisRepository;

    /**
     * 유형별 이상형 프로필 상세를 언락한다.
     * 오픈된 유형의 첫 언락은 무료(하트 이벤트 미발행), 이후 언락은 하트를 차감한다.
     * 이미 언락된 대상은 멱등하게 처리한다.
     */
    @Transactional
    public void unlock(long memberId, AnswerPersonalityType type, long targetMemberId) {
        PersonalityIntroductionOpenState openState = personalityIntroductionRedisRepository.findOpenState(memberId)
            .orElseThrow(() -> new DeeppleException(
                MemberErrorCode.INVALID_PERSONALITY_INTRODUCTION_UNLOCK,
                "오픈된 유형별 이상형 목록이 없습니다."
            ));
        if (openState.type() != type || !openState.memberIds().contains(targetMemberId)) {
            throw new DeeppleException(
                MemberErrorCode.INVALID_PERSONALITY_INTRODUCTION_UNLOCK,
                "오픈된 목록에 없는 대상입니다. targetMemberId: " + targetMemberId);
        }

        Set<Long> unlockedMemberIds = personalityIntroductionRedisRepository.findUnlockedMemberIds(memberId);
        if (unlockedMemberIds.contains(targetMemberId)
            || memberIntroductionCommandRepository.existsByMemberIdAndIntroducedMemberId(memberId, targetMemberId)) {
            return;
        }

        validateTarget(memberId, targetMemberId);

        boolean isFirstUnlock = unlockedMemberIds.isEmpty();
        MemberIntroduction memberIntroduction = isFirstUnlock
            ? MemberIntroduction.ofWithoutCharge(memberId, targetMemberId, IntroductionType.PERSONALITY)
            : MemberIntroduction.of(memberId, targetMemberId, IntroductionType.PERSONALITY);
        memberIntroductionCommandRepository.save(memberIntroduction);

        personalityIntroductionRedisRepository.addUnlockedMemberId(memberId, targetMemberId);
    }

    private void validateTarget(long memberId, long targetMemberId) {
        Member target = memberCommandRepository.findById(targetMemberId)
            .orElseThrow(() -> new DeeppleException(MemberErrorCode.MEMBER_NOT_FOUND, "memberId=" + targetMemberId));
        if (!target.isActive()) {
            throw new DeeppleException(
                MemberErrorCode.INTRODUCED_MEMBER_NOT_ACTIVE,
                "introducedMemberId=" + targetMemberId
            );
        }
        if (blockRepository.existsByBlockerIdAndBlockedId(memberId, targetMemberId)
            || blockRepository.existsByBlockerIdAndBlockedId(targetMemberId, memberId)) {
            throw new DeeppleException(
                MemberErrorCode.INTRODUCED_MEMBER_BLOCKED,
                "memberId=" + memberId + ", introducedMemberId=" + targetMemberId
            );
        }
    }
}
