package deepple.deepple.datingexam.adapter.database;

import deepple.deepple.block.domain.Block;
import deepple.deepple.common.MockEventsExtension;
import deepple.deepple.common.config.QueryDslConfig;
import deepple.deepple.datingexam.domain.AnswerPersonalityType;
import deepple.deepple.datingexam.domain.DatingExamSubmitResult;
import deepple.deepple.member.command.domain.member.ActivityStatus;
import deepple.deepple.member.command.domain.member.Gender;
import deepple.deepple.member.command.domain.member.Member;
import deepple.deepple.member.command.domain.member.vo.MemberProfile;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@Import({QueryDslConfig.class, PersonalityTypeMemberQueryRepositoryImpl.class})
@ExtendWith(MockEventsExtension.class)
@DataJpaTest
class PersonalityTypeMemberQueryRepositoryImplTest {

    private static final AnswerPersonalityType TARGET_TYPE = AnswerPersonalityType.STIMULATING_ADVENTURER;

    @Autowired
    private TestEntityManager em;

    @Autowired
    private PersonalityTypeMemberQueryRepositoryImpl repository;

    private int phoneSeq = 0;

    @Test
    @DisplayName("대표 유형이 일치하는 반대 성별·공개·활성 회원만 최신 가입순으로 조회하고, 다른 유형·동성·비공개·비활성·차단·본인은 제외한다.")
    void findMemberIdsByDominantType() {
        // given: 요청자(남성)
        Member requester = createMember(Gender.MALE, true, ActivityStatus.ACTIVE);
        createSubmitResult(requester.getId(), TARGET_TYPE);

        // 포함 대상: 반대 성별(여성) · 공개 · 활성 · 대표 유형 일치
        Member includedA = createMember(Gender.FEMALE, true, ActivityStatus.ACTIVE);
        createSubmitResult(includedA.getId(), TARGET_TYPE);
        Member includedB = createMember(Gender.FEMALE, true, ActivityStatus.ACTIVE);
        createSubmitResult(includedB.getId(), TARGET_TYPE);

        // 제외 대상들
        Member differentType = createMember(Gender.FEMALE, true, ActivityStatus.ACTIVE);
        createSubmitResult(differentType.getId(), AnswerPersonalityType.RATIONAL_REALIST);

        Member sameGender = createMember(Gender.MALE, true, ActivityStatus.ACTIVE);
        createSubmitResult(sameGender.getId(), TARGET_TYPE);

        Member notPublic = createMember(Gender.FEMALE, false, ActivityStatus.ACTIVE);
        createSubmitResult(notPublic.getId(), TARGET_TYPE);

        Member dormant = createMember(Gender.FEMALE, true, ActivityStatus.DORMANT);
        createSubmitResult(dormant.getId(), TARGET_TYPE);

        Member blocked = createMember(Gender.FEMALE, true, ActivityStatus.ACTIVE);
        createSubmitResult(blocked.getId(), TARGET_TYPE);
        createBlock(requester.getId(), blocked.getId());

        em.flush();
        em.clear();

        // when
        List<Long> result = repository.findMemberIdsByDominantType(requester.getId(), TARGET_TYPE);

        // then: 최신 가입순(id desc) → 나중에 가입한 includedB가 먼저
        assertThat(result).containsExactly(includedB.getId(), includedA.getId());
    }

    private Member createMember(Gender gender, boolean isProfilePublic, ActivityStatus activityStatus) {
        Member member = Member.fromPhoneNumber(nextPhoneNumber());
        em.persist(member);
        member.updateProfile(MemberProfile.builder().gender(gender).build());
        if (isProfilePublic) {
            member.publishProfile();
        }
        em.flush();

        if (activityStatus == ActivityStatus.ACTIVE) {
            member.changeToActive();
        }
        if (activityStatus == ActivityStatus.DORMANT && member.isActive()) {
            member.changeToDormant();
        }
        em.flush();
        return member;
    }

    private void createSubmitResult(Long memberId, AnswerPersonalityType type) {
        DatingExamSubmitResult result = DatingExamSubmitResult.create(memberId);
        result.addCounts(Map.of(type, 1));
        em.persist(result);
        em.flush();
    }

    private void createBlock(Long blockerId, Long blockedId) {
        em.persist(Block.of(blockerId, blockedId));
        em.flush();
    }

    private String nextPhoneNumber() {
        return String.format("010%08d", phoneSeq++);
    }
}
