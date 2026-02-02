package deepple.deepple.member.query.introduction.application;

import deepple.deepple.member.command.domain.member.Grade;
import deepple.deepple.member.query.introduction.intra.IntroductionQueryRepository;
import deepple.deepple.member.query.introduction.intra.MemberIntroductionProfileQueryResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class IntroductionQueryService {
    private final IntroductionMemberIdFetcher introductionMemberIdFetcher;
    private final IntroductionQueryRepository introductionQueryRepository;

    public List<MemberIntroductionProfileView> findDiamondGradeIntroductions(long memberId) {
        Set<Long> introductionMemberIds = getDiamondGradeIntroductionMemberIds(memberId);
        List<MemberIntroductionProfileQueryResult> memberIntroductionProfileQueryResults =
            introductionQueryRepository.findAllMemberIntroductionProfileQueryResultByMemberIds(memberId,
                introductionMemberIds);
        return MemberIntroductionProfileViewMapper.mapWithDefaultTag(memberIntroductionProfileQueryResults);
    }

    public List<MemberIntroductionProfileView> findSameHobbyIntroductions(long memberId) {
        Set<Long> introductionMemberIds = getSameHobbyIntroductionMemberIds(memberId);
        List<MemberIntroductionProfileQueryResult> memberIntroductionProfileQueryResults =
            introductionQueryRepository.findAllMemberIntroductionProfileQueryResultByMemberIds(memberId,
                introductionMemberIds);
        return MemberIntroductionProfileViewMapper.mapWithSameHobbyTag(memberIntroductionProfileQueryResults);
    }

    public List<MemberIntroductionProfileView> findSameReligionIntroductions(long memberId) {
        Set<Long> introductionMemberIds = getSameReligionIntroductionMemberIds(memberId);
        List<MemberIntroductionProfileQueryResult> memberIntroductionProfileQueryResults =
            introductionQueryRepository.findAllMemberIntroductionProfileQueryResultByMemberIds(memberId,
                introductionMemberIds);
        return MemberIntroductionProfileViewMapper.mapWithSameReligionTag(memberIntroductionProfileQueryResults);
    }

    public List<MemberIntroductionProfileView> findSameCityIntroductions(long memberId) {
        Set<Long> introductionMemberIds = getSameCityIntroductionMemberIds(memberId);
        List<MemberIntroductionProfileQueryResult> memberIntroductionProfileQueryResults =
            introductionQueryRepository.findAllMemberIntroductionProfileQueryResultByMemberIds(memberId,
                introductionMemberIds);
        return MemberIntroductionProfileViewMapper.mapWithDefaultTag(memberIntroductionProfileQueryResults);
    }

    public List<MemberIntroductionProfileView> findRecentlyJoinedIntroductions(long memberId) {
        Set<Long> introductionMemberIds = getRecentlyJoinedIntroductionMemberIds(memberId);
        List<MemberIntroductionProfileQueryResult> memberIntroductionProfileQueryResults =
            introductionQueryRepository.findAllMemberIntroductionProfileQueryResultByMemberIds(memberId,
                introductionMemberIds);
        return MemberIntroductionProfileViewMapper.mapWithDefaultTag(memberIntroductionProfileQueryResults);
    }

    public List<MemberIntroductionProfileView> findMemberIntroductionProfileViews(long memberId, Set<Long> memberIds) {
        List<MemberIntroductionProfileQueryResult> memberIntroductionProfileQueryResults =
            introductionQueryRepository.findAllMemberIntroductionProfileQueryResultByMemberIds(memberId,
                memberIds);
        return MemberIntroductionProfileViewMapper.mapWithDefaultTag(memberIntroductionProfileQueryResults);
    }

    public List<MemberIntroductionProfileView> findIdealIntroductions(long memberId) {
        Set<Long> introductionMemberIds = getIdealIntroductionMemberIds(memberId);
        List<MemberIntroductionProfileQueryResult> memberIntroductionProfileQueryResults =
            introductionQueryRepository.findAllMemberIntroductionProfileQueryResultByMemberIds(memberId,
                introductionMemberIds);
        return MemberIntroductionProfileViewMapper.mapWithDefaultTag(memberIntroductionProfileQueryResults);
    }

    private Set<Long> getDiamondGradeIntroductionMemberIds(long memberId) {
        return introductionMemberIdFetcher.fetch(
            memberId,
            IntroductionCacheKeyPrefix.DIAMOND,
            Grade.DIAMOND,
            (excluded, ideal, member, grade) -> IntroductionSearchCondition.ofGrade(excluded, ideal,
                member.getGender().getOpposite(), grade)
        );
    }

    private Set<Long> getSameHobbyIntroductionMemberIds(long memberId) {
        return introductionMemberIdFetcher.fetch(
            memberId,
            IntroductionCacheKeyPrefix.SAME_HOBBY,
            null,
            (excluded, ideal, member, unused) ->
                IntroductionSearchCondition.ofHobbies(excluded, ideal, member.getGender().getOpposite(),
                    member.getProfile().getHobbies())
        );
    }

    private Set<Long> getSameReligionIntroductionMemberIds(long memberId) {
        return introductionMemberIdFetcher.fetch(
            memberId,
            IntroductionCacheKeyPrefix.SAME_RELIGION,
            null,
            (excluded, ideal, member, unused) ->
                IntroductionSearchCondition.ofReligion(excluded, ideal, member.getGender().getOpposite(),
                    member.getProfile().getReligion())
        );
    }

    private Set<Long> getSameCityIntroductionMemberIds(long memberId) {
        return introductionMemberIdFetcher.fetch(
            memberId,
            IntroductionCacheKeyPrefix.SAME_CITY,
            null,
            (excluded, ideal, member, unused) ->
                IntroductionSearchCondition.ofCity(excluded, ideal, member.getGender().getOpposite(),
                    member.getProfile().getRegion().getCity())
        );
    }

    private Set<Long> getRecentlyJoinedIntroductionMemberIds(long memberId) {
        return introductionMemberIdFetcher.fetch(
            memberId,
            IntroductionCacheKeyPrefix.RECENTLY_JOINED,
            null,
            (excluded, ideal, member, unused) ->
                IntroductionSearchCondition.ofJoinDate(excluded, ideal, member.getGender().getOpposite(),
                    getRecentlyJoinedCutoffDate())
        );
    }

    private Set<Long> getIdealIntroductionMemberIds(long memberId) {
        return introductionMemberIdFetcher.fetch(
            memberId,
            IntroductionCacheKeyPrefix.IDEAL,
            null,
            (excluded, ideal, member, unused) ->
                IntroductionSearchCondition.ofIdeal(excluded, ideal, member.getGender().getOpposite())
        );
    }

    private LocalDateTime getRecentlyJoinedCutoffDate() {
        return LocalDate.now().minusDays(3).atStartOfDay();
    }
}
