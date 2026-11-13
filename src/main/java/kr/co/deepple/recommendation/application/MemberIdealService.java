package kr.co.deepple.recommendation.application;

import java.util.Set;
import java.util.stream.Collectors;
import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.member.command.domain.MemberErrorCode;
import kr.co.deepple.member.command.domain.member.City;
import kr.co.deepple.member.command.domain.member.DrinkingStatus;
import kr.co.deepple.member.command.domain.member.Hobby;
import kr.co.deepple.member.command.domain.member.Religion;
import kr.co.deepple.member.command.domain.member.SmokingStatus;
import kr.co.deepple.recommendation.application.dto.MemberIdealUpdateRequest;
import kr.co.deepple.recommendation.application.port.out.MemberIdealCommandRepository;
import kr.co.deepple.recommendation.domain.AgeRange;
import kr.co.deepple.recommendation.domain.MemberIdeal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    return memberIdealCommandRepository
        .findByMemberId(memberId)
        .orElseThrow(() ->
            new DeeppleException(MemberErrorCode.MEMBER_IDEAL_NOT_FOUND, "memberId=" + memberId));
  }
}
