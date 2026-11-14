package kr.co.deepple.recommendation.application;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import kr.co.deepple.datingexam.application.port.in.SoulmateFinder;
import kr.co.deepple.member.application.port.in.FindMemberProfilesUseCase;
import kr.co.deepple.member.application.port.in.MemberProfileSummary;
import kr.co.deepple.recommendation.adapter.out.persistence.IntroductionQueryRepository;
import kr.co.deepple.recommendation.application.dto.IntroductionSearchCondition;
import kr.co.deepple.recommendation.application.dto.MemberIntroductionProfileQueryResult;
import kr.co.deepple.recommendation.application.dto.MemberIntroductionProfileView;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class IntroductionQueryService {

  private static final String DIAMOND_GRADE = "DIAMOND";

  private final IntroductionMemberIdFetcher introductionMemberIdFetcher;
  private final IntroductionQueryRepository introductionQueryRepository;
  private final FindMemberProfilesUseCase findMemberProfilesUseCase;
  private final SoulmateFinder soulmateFinder;

  public List<MemberIntroductionProfileView> findDiamondGradeIntroductions(long memberId) {
    Set<Long> introductionMemberIds = getDiamondGradeIntroductionMemberIds(memberId);
    List<MemberIntroductionProfileQueryResult> memberIntroductionProfileQueryResults =
        introductionQueryRepository.findAllMemberIntroductionProfileQueryResultByMemberIds(
            memberId, introductionMemberIds);
    return MemberIntroductionProfileViewMapper.mapWithDefaultTag(
        memberIntroductionProfileQueryResults);
  }

  public List<MemberIntroductionProfileView> findSameHobbyIntroductions(long memberId) {
    Set<Long> introductionMemberIds = getSameHobbyIntroductionMemberIds(memberId);
    List<MemberIntroductionProfileQueryResult> memberIntroductionProfileQueryResults =
        introductionQueryRepository.findAllMemberIntroductionProfileQueryResultByMemberIds(
            memberId, introductionMemberIds);
    return MemberIntroductionProfileViewMapper.mapWithSameHobbyTag(
        memberIntroductionProfileQueryResults);
  }

  public List<MemberIntroductionProfileView> findSameReligionIntroductions(long memberId) {
    Set<Long> introductionMemberIds = getSameReligionIntroductionMemberIds(memberId);
    List<MemberIntroductionProfileQueryResult> memberIntroductionProfileQueryResults =
        introductionQueryRepository.findAllMemberIntroductionProfileQueryResultByMemberIds(
            memberId, introductionMemberIds);
    return MemberIntroductionProfileViewMapper.mapWithSameReligionTag(
        memberIntroductionProfileQueryResults);
  }

  public List<MemberIntroductionProfileView> findSameCityIntroductions(long memberId) {
    Set<Long> introductionMemberIds = getSameCityIntroductionMemberIds(memberId);
    List<MemberIntroductionProfileQueryResult> memberIntroductionProfileQueryResults =
        introductionQueryRepository.findAllMemberIntroductionProfileQueryResultByMemberIds(
            memberId, introductionMemberIds);
    return MemberIntroductionProfileViewMapper.mapWithDefaultTag(
        memberIntroductionProfileQueryResults);
  }

  public List<MemberIntroductionProfileView> findRecentlyJoinedIntroductions(long memberId) {
    Set<Long> introductionMemberIds = getRecentlyJoinedIntroductionMemberIds(memberId);
    List<MemberIntroductionProfileQueryResult> memberIntroductionProfileQueryResults =
        introductionQueryRepository.findAllMemberIntroductionProfileQueryResultByMemberIds(
            memberId, introductionMemberIds);
    return MemberIntroductionProfileViewMapper.mapWithDefaultTag(
        memberIntroductionProfileQueryResults);
  }

  public List<MemberIntroductionProfileView> findMemberIntroductionProfileViews(
      long memberId, Set<Long> memberIds) {
    List<MemberIntroductionProfileQueryResult> memberIntroductionProfileQueryResults =
        introductionQueryRepository.findAllMemberIntroductionProfileQueryResultByMemberIds(
            memberId, memberIds);
    return MemberIntroductionProfileViewMapper.mapWithDefaultTag(
        memberIntroductionProfileQueryResults);
  }

  public List<MemberIntroductionProfileView> findSoulmateIntroductions(long memberId) {
    return findMemberIntroductionProfileViews(memberId, soulmateFinder.findSoulmateIds(memberId));
  }

  public List<MemberIntroductionProfileView> findIdealIntroductions(long memberId) {
    Set<Long> introductionMemberIds = getIdealIntroductionMemberIds(memberId);
    List<MemberIntroductionProfileQueryResult> memberIntroductionProfileQueryResults =
        introductionQueryRepository.findAllMemberIntroductionProfileQueryResultByMemberIds(
            memberId, introductionMemberIds);
    return MemberIntroductionProfileViewMapper.mapWithDefaultTag(
        memberIntroductionProfileQueryResults);
  }

  private Set<Long> getDiamondGradeIntroductionMemberIds(long memberId) {
    return introductionMemberIdFetcher.fetch(
        memberId,
        IntroductionCacheKeyPrefix.DIAMOND,
        DIAMOND_GRADE,
        (excluded, ideal, member, grade) ->
            IntroductionSearchCondition.ofGrade(excluded, ideal, member.oppositeGender(), grade));
  }

  private Set<Long> getSameHobbyIntroductionMemberIds(long memberId) {
    return introductionMemberIdFetcher.fetch(
        memberId,
        IntroductionCacheKeyPrefix.SAME_HOBBY,
        null,
        (excluded, ideal, member, unused) ->
            IntroductionSearchCondition.ofHobbies(
                excluded, ideal, member.oppositeGender(), getProfile(member.memberId()).hobbies()));
  }

  private Set<Long> getSameReligionIntroductionMemberIds(long memberId) {
    return introductionMemberIdFetcher.fetch(
        memberId,
        IntroductionCacheKeyPrefix.SAME_RELIGION,
        null,
        (excluded, ideal, member, unused) ->
            IntroductionSearchCondition.ofReligion(
                excluded,
                ideal,
                member.oppositeGender(),
                getProfile(member.memberId()).religion()));
  }

  private Set<Long> getSameCityIntroductionMemberIds(long memberId) {
    return introductionMemberIdFetcher.fetch(
        memberId,
        IntroductionCacheKeyPrefix.SAME_CITY,
        null,
        (excluded, ideal, member, unused) ->
            IntroductionSearchCondition.ofCity(
                excluded, ideal, member.oppositeGender(), member.city()));
  }

  private Set<Long> getRecentlyJoinedIntroductionMemberIds(long memberId) {
    return introductionMemberIdFetcher.fetch(
        memberId,
        IntroductionCacheKeyPrefix.RECENTLY_JOINED,
        null,
        (excluded, ideal, member, unused) ->
            IntroductionSearchCondition.ofJoinDate(
                excluded, ideal, member.oppositeGender(), getRecentlyJoinedCutoffDate()));
  }

  private Set<Long> getIdealIntroductionMemberIds(long memberId) {
    return introductionMemberIdFetcher.fetch(
        memberId,
        IntroductionCacheKeyPrefix.IDEAL,
        null,
        (excluded, ideal, member, unused) ->
            IntroductionSearchCondition.ofIdeal(excluded, ideal, member.oppositeGender()));
  }

  private MemberProfileSummary getProfile(long memberId) {
    MemberProfileSummary profile =
        findMemberProfilesUseCase.findMemberProfiles(List.of(memberId)).get(memberId);
    if (profile == null) {
      throw new IllegalStateException("프로필을 찾을 수 없는 회원입니다. memberId=" + memberId);
    }
    return profile;
  }

  private LocalDateTime getRecentlyJoinedCutoffDate() {
    return LocalDate.now().minusDays(3).atStartOfDay();
  }
}
