package kr.co.deepple.member.command.application.introduction;

import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.member.command.domain.MemberErrorCode;
import kr.co.deepple.member.command.domain.introduction.MemberIdeal;
import kr.co.deepple.member.command.domain.introduction.MemberIdealCommandRepository;
import kr.co.deepple.member.command.domain.introduction.vo.AgeRange;
import kr.co.deepple.member.command.domain.member.*;
import kr.co.deepple.member.presentation.introduction.dto.MemberIdealUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MemberIdealService {
    private final MemberIdealCommandRepository memberIdealCommandRepository;

    @Transactional
    public void initIfAbsent(long memberId) {
        if (memberIdealCommandRepository.existsByMemberId(memberId)) {
            return;
        }
        MemberIdeal memberIdeal = MemberIdeal.init(memberId);
        memberIdealCommandRepository.save(memberIdeal);
    }

    @Transactional
    public void update(MemberIdealUpdateRequest request, long memberId) {
        MemberIdeal memberIdeal = getMemberIdealByMemberId(memberId);
        AgeRange ageRange = AgeRange.of(request.minAge(), request.maxAge());
        Set<Hobby> hobbies = request.hobbies().stream().map(Hobby::from).collect(Collectors.toSet());
        Set<City> cities = request.cities().stream().map(City::from).collect(Collectors.toSet());
        Religion religion = Religion.from(request.religion());
        SmokingStatus smokingStatus = SmokingStatus.from(request.smokingStatus());
        DrinkingStatus drinkingStatus = DrinkingStatus.from(request.drinkingStatus());
        memberIdeal.update(ageRange, hobbies, cities, religion, smokingStatus, drinkingStatus);
    }

    private MemberIdeal getMemberIdealByMemberId(long memberId) {
        return memberIdealCommandRepository.findByMemberId(memberId)
            .orElseThrow(() -> new DeeppleException(MemberErrorCode.MEMBER_IDEAL_NOT_FOUND, "memberId=" + memberId));
    }
}
