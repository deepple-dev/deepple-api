package kr.co.deepple.member.command.application.introduction;

import kr.co.deepple.member.command.domain.introduction.IntroductionType;
import kr.co.deepple.member.command.domain.introduction.MemberIntroduction;
import kr.co.deepple.member.command.domain.introduction.MemberIntroductionCommandRepository;
import kr.co.deepple.member.command.domain.member.Member;
import kr.co.deepple.member.command.domain.member.MemberCommandRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TodayCardService {
    private final MemberCommandRepository memberCommandRepository;
    private final MemberIntroductionCommandRepository memberIntroductionCommandRepository;

    @Transactional
    public void createTodayCardIntroductions(long memberId, Set<Long> todayCardMemberIds) {
        Set<Long> introductionTargetMemberIds = getIntroductionTargetMemberIds(memberId, todayCardMemberIds);
        if (introductionTargetMemberIds.isEmpty()) {
            return;
        }
        createIntroductions(memberId, introductionTargetMemberIds);
    }

    private Set<Long> getIntroductionTargetMemberIds(long memberId, Set<Long> todayCardMemberIds) {
        Set<Long> memberIntroductions = memberIntroductionCommandRepository
            .findAllByMemberIdAndIntroducedMemberIdIn(memberId, todayCardMemberIds)
            .stream().map(MemberIntroduction::getIntroducedMemberId).collect(Collectors.toSet());

        final Set<Long> introductionTargetMemberIds = todayCardMemberIds.stream()
            .filter(introducedMemberId -> !memberIntroductions.contains(introducedMemberId))
            .collect(Collectors.toSet());

        if (introductionTargetMemberIds.isEmpty()) {
            return introductionTargetMemberIds;
        }

        List<Member> introductionTargetMembers = memberCommandRepository.findAllById(introductionTargetMemberIds);

        return introductionTargetMembers.stream()
            .filter(Member::isActive)
            .map(Member::getId)
            .collect(Collectors.toSet());
    }

    private void createIntroductions(long memberId, Set<Long> todayCardMemberIds) {
        List<MemberIntroduction> memberIntroductions = todayCardMemberIds.stream()
            .map(introducedMemberId -> MemberIntroduction.of(memberId, introducedMemberId, IntroductionType.TODAY_CARD))
            .toList();
        memberIntroductionCommandRepository.saveAll(memberIntroductions);
    }
}
