package kr.co.deepple.community.application;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import kr.co.deepple.block.application.port.in.FindBlockedMemberIdsUseCase;
import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.community.adapter.out.persistence.AdminSelfIntroductionQueryRepository;
import kr.co.deepple.community.adapter.out.persistence.SelfIntroductionQueryRepository;
import kr.co.deepple.community.application.dto.AdminSelfIntroductionSearchCondition;
import kr.co.deepple.community.application.dto.AdminSelfIntroductionView;
import kr.co.deepple.community.application.dto.MemberBasicInfo;
import kr.co.deepple.community.application.dto.RawAdminSelfIntroductionView;
import kr.co.deepple.community.application.dto.RawSelfIntroductionView;
import kr.co.deepple.community.application.dto.SelfIntroductionSearchCondition;
import kr.co.deepple.community.application.dto.SelfIntroductionSearchRequest;
import kr.co.deepple.community.application.dto.SelfIntroductionSummaryView;
import kr.co.deepple.community.application.dto.SelfIntroductionSummaryViews;
import kr.co.deepple.community.application.dto.SelfIntroductionView;
import kr.co.deepple.community.domain.CommunityErrorCode;
import kr.co.deepple.datingexam.application.port.in.FindDominantPersonalityTypesUseCase;
import kr.co.deepple.like.application.port.in.FindLikeLevelUseCase;
import kr.co.deepple.member.application.port.in.FindMemberIdsUseCase;
import kr.co.deepple.member.application.port.in.FindMemberPreviewsUseCase;
import kr.co.deepple.member.application.port.in.FindMemberProfilesUseCase;
import kr.co.deepple.member.application.port.in.MemberPreview;
import kr.co.deepple.member.application.port.in.MemberProfileSummary;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class SelfIntroductionQueryService {

  private static final int PAGE_SIZE = 10;
  private static final int INITIAL_FETCH_SIZE = 50;
  private static final int MAX_FETCH_SIZE = 1_000;

  private final SelfIntroductionQueryRepository selfIntroductionQueryRepository;
  private final AdminSelfIntroductionQueryRepository adminSelfIntroductionQueryRepository;
  private final FindBlockedMemberIdsUseCase findBlockedMemberIdsUseCase;
  private final FindMemberPreviewsUseCase findMemberPreviewsUseCase;
  private final FindMemberProfilesUseCase findMemberProfilesUseCase;
  private final FindMemberIdsUseCase findMemberIdsUseCase;
  private final FindDominantPersonalityTypesUseCase findDominantPersonalityTypesUseCase;
  private final FindLikeLevelUseCase findLikeLevelUseCase;

  public SelfIntroductionSummaryViews findSelfIntroductions(
      long memberId, SelfIntroductionSearchRequest searchRequest) {
    SelfIntroductionSearchCondition condition = toSearchCondition(searchRequest);
    Set<Long> blockedIds = findBlockedMemberIdsUseCase.findBlockedMemberIds(memberId);

    List<RawSelfIntroductionView> matched = new ArrayList<>();
    Map<Long, MemberPreview> authors = new HashMap<>();
    Long cursor = searchRequest.lastId();
    int fetchSize = INITIAL_FETCH_SIZE;
    while (matched.size() < PAGE_SIZE) {
      List<RawSelfIntroductionView> raws =
          selfIntroductionQueryRepository.findSelfIntroductions(
              memberId, cursor, blockedIds, fetchSize);
      Map<Long, MemberPreview> previews =
          findMemberPreviewsUseCase.findMemberPreviews(memberIdsOf(raws));
      for (RawSelfIntroductionView raw : raws) {
        if (matched.size() == PAGE_SIZE) {
          break;
        }
        cursor = raw.id();
        MemberPreview author = previews.get(raw.memberId());
        if (author != null && matches(condition, author)) {
          matched.add(raw);
          authors.put(raw.memberId(), author);
        }
      }
      if (raws.size() < fetchSize) {
        break;
      }
      fetchSize = Math.min(fetchSize * 2, MAX_FETCH_SIZE);
    }
    return toSummaryViews(matched, authors);
  }

  public SelfIntroductionSummaryViews findMySelfIntroductions(long memberId, Long lastId) {
    List<RawSelfIntroductionView> raws =
        selfIntroductionQueryRepository.findMySelfIntroductions(memberId, lastId, PAGE_SIZE);
    Map<Long, MemberPreview> authors =
        findMemberPreviewsUseCase.findMemberPreviews(memberIdsOf(raws));
    return toSummaryViews(
        raws.stream().filter(raw -> authors.containsKey(raw.memberId())).toList(), authors);
  }

  public SelfIntroductionView getSelfIntroduction(long id, long memberId) {
    RawSelfIntroductionView raw =
        selfIntroductionQueryRepository
            .findSelfIntroduction(id)
            .orElseThrow(() ->
                new DeeppleException(
                    CommunityErrorCode.SELF_INTRODUCTION_NOT_FOUND, "selfIntroductionId=" + id));
    long authorId = raw.memberId();
    MemberProfileSummary author =
        findMemberProfilesUseCase.findMemberProfiles(Set.of(authorId)).get(authorId);
    if (author == null) {
      throw new DeeppleException(
          CommunityErrorCode.SELF_INTRODUCTION_NOT_FOUND,
          "selfIntroductionId=" + id + ", authorId=" + authorId);
    }
    String personalityType =
        findDominantPersonalityTypesUseCase
            .findDominantPersonalityTypes(Set.of(authorId))
            .get(authorId);
    return new SelfIntroductionView(
        new MemberBasicInfo(
            author.memberId(),
            author.nickname(),
            author.age(),
            author.city(),
            author.district(),
            author.mbti(),
            author.hobbies(),
            author.gender(),
            personalityType),
        findLikeLevelUseCase.findLikeLevel(memberId, authorId).orElse(null),
        raw.title(),
        raw.content(),
        raw.imageUrl(),
        selfIntroductionQueryRepository.findProfileExchangeStatus(memberId, authorId).orElse(null),
        raw.createdAt());
  }

  public Page<AdminSelfIntroductionView> findAdminSelfIntroductions(
      AdminSelfIntroductionSearchCondition condition, Pageable pageable) {
    String nickname = blankToNull(condition.nickname());
    String phoneNumber = blankToNull(condition.phoneNumber());
    Set<Long> memberIds = null;
    if (nickname != null || phoneNumber != null) {
      memberIds = findMemberIdsUseCase.findMemberIds(nickname, phoneNumber);
      if (memberIds.isEmpty()) {
        return Page.empty(pageable);
      }
    }
    Page<RawAdminSelfIntroductionView> page =
        adminSelfIntroductionQueryRepository.findSelfIntroductions(condition, memberIds, pageable);
    Map<Long, MemberPreview> authors =
        findMemberPreviewsUseCase.findMemberPreviews(
            page.getContent().stream()
                .map(RawAdminSelfIntroductionView::memberId)
                .collect(Collectors.toSet()));
    return page.map(raw -> {
      MemberPreview author = authors.get(raw.memberId());
      return AdminSelfIntroductionView.of(
          raw, author == null ? null : author.nickname(), author == null ? null : author.gender());
    });
  }

  private SelfIntroductionSearchCondition toSearchCondition(SelfIntroductionSearchRequest request) {
    return new SelfIntroductionSearchCondition(
        request.preferredCities() == null
            ? Set.of()
            : request.preferredCities().stream()
                .map(city -> city.toUpperCase(Locale.ROOT))
                .collect(Collectors.toSet()),
        request.fromAge(),
        request.toAge(),
        request.gender() == null ? null : request.gender().toUpperCase(Locale.ROOT));
  }

  private boolean matches(SelfIntroductionSearchCondition condition, MemberPreview author) {
    if (condition.gender() != null && !condition.gender().equals(author.gender())) {
      return false;
    }
    if (!condition.preferredCities().isEmpty()
        && (author.city() == null || !condition.preferredCities().contains(author.city()))) {
      return false;
    }
    if (condition.fromAge() == null && condition.toAge() == null) {
      return true;
    }
    Integer age = author.age();
    if (age == null) {
      return false;
    }
    return (condition.fromAge() == null || age >= condition.fromAge())
        && (condition.toAge() == null || age <= condition.toAge());
  }

  private SelfIntroductionSummaryViews toSummaryViews(
      List<RawSelfIntroductionView> raws, Map<Long, MemberPreview> authors) {
    Map<Long, String> personalityTypes =
        findDominantPersonalityTypesUseCase.findDominantPersonalityTypes(memberIdsOf(raws));
    return new SelfIntroductionSummaryViews(
        raws.stream()
            .map(raw -> {
              MemberPreview author = authors.get(raw.memberId());
              return new SelfIntroductionSummaryView(
                  raw.id(),
                  author.nickname(),
                  author.yearOfBirth(),
                  raw.title(),
                  raw.content(),
                  raw.imageUrl(),
                  raw.createdAt(),
                  personalityTypes.get(raw.memberId()));
            })
            .toList());
  }

  private Set<Long> memberIdsOf(List<RawSelfIntroductionView> raws) {
    return raws.stream().map(RawSelfIntroductionView::memberId).collect(Collectors.toSet());
  }

  private String blankToNull(String value) {
    return value == null || value.isBlank() ? null : value;
  }
}
