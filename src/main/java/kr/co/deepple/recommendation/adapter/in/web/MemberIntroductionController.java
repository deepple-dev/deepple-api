package kr.co.deepple.recommendation.adapter.in.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Set;
import kr.co.deepple.common.security.CurrentMemberId;
import kr.co.deepple.datingexam.application.port.in.SoulmateFinder;
import kr.co.deepple.datingexam.domain.AnswerPersonalityType;
import kr.co.deepple.recommendation.application.IntroductionQueryService;
import kr.co.deepple.recommendation.application.MemberIntroductionService;
import kr.co.deepple.recommendation.application.PersonalityIntroductionQueryService;
import kr.co.deepple.recommendation.application.PersonalityIntroductionUnlockService;
import kr.co.deepple.recommendation.application.TodayCardQueryService;
import kr.co.deepple.recommendation.application.TodayCardService;
import kr.co.deepple.recommendation.application.dto.MemberIntroductionCreateRequest;
import kr.co.deepple.recommendation.application.dto.MemberIntroductionProfileView;
import kr.co.deepple.recommendation.application.dto.MemberIntroductionProfileViews;
import kr.co.deepple.recommendation.application.dto.PersonalityIntroductionOpenStatusView;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "소개받고 싶은 이성 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/member/introduction")
public class MemberIntroductionController {

  private final TodayCardService todayCardService;
  private final TodayCardQueryService todayCardQueryService;
  private final IntroductionQueryService introductionQueryService;
  private final MemberIntroductionService memberIntroductionService;
  private final SoulmateFinder soulmateFinder;
  private final PersonalityIntroductionQueryService personalityIntroductionQueryService;
  private final PersonalityIntroductionUnlockService personalityIntroductionUnlockService;

  @Operation(summary = "다이아 등급 이성 조회")
  @GetMapping("/grade")
  public ResponseEntity<MemberIntroductionProfileViews> findDiamondGradeIntroductions(
      @CurrentMemberId long memberId) {
    List<MemberIntroductionProfileView> introductionProfileViews =
        introductionQueryService.findDiamondGradeIntroductions(memberId);
    return ResponseEntity.ok(new MemberIntroductionProfileViews(introductionProfileViews));
  }

  @Operation(summary = "취미가 같은 이성 조회")
  @GetMapping("/hobby")
  public ResponseEntity<MemberIntroductionProfileViews> findSameHobbyIntroductions(
      @CurrentMemberId long memberId) {
    List<MemberIntroductionProfileView> introductionProfileViews =
        introductionQueryService.findSameHobbyIntroductions(memberId);
    return ResponseEntity.ok(new MemberIntroductionProfileViews(introductionProfileViews));
  }

  @Operation(summary = "종교가 같은 이성 조회")
  @GetMapping("/religion")
  public ResponseEntity<MemberIntroductionProfileViews> findSameReligionIntroductions(
      @CurrentMemberId long memberId) {
    List<MemberIntroductionProfileView> introductionProfileViews =
        introductionQueryService.findSameReligionIntroductions(memberId);
    return ResponseEntity.ok(new MemberIntroductionProfileViews(introductionProfileViews));
  }

  @Operation(summary = "지역이 같은 이성 조회")
  @GetMapping("/city")
  public ResponseEntity<MemberIntroductionProfileViews> findSameCityIntroductions(
      @CurrentMemberId long memberId) {
    List<MemberIntroductionProfileView> introductionProfileViews =
        introductionQueryService.findSameCityIntroductions(memberId);
    return ResponseEntity.ok(new MemberIntroductionProfileViews(introductionProfileViews));
  }

  @Operation(summary = "최근에 가입한 이성 조회")
  @GetMapping("/recent")
  public ResponseEntity<MemberIntroductionProfileViews> findRecentlyJoinedIntroductions(
      @CurrentMemberId long memberId) {
    List<MemberIntroductionProfileView> introductionProfileViews =
        introductionQueryService.findRecentlyJoinedIntroductions(memberId);
    return ResponseEntity.ok(new MemberIntroductionProfileViews(introductionProfileViews));
  }

  @Operation(summary = "오늘의 카드 생성 및 조회")
  @PostMapping("/today-card")
  public ResponseEntity<MemberIntroductionProfileViews> createTodayCardIntroductions(
      @CurrentMemberId long memberId) {
    Set<Long> todayCardMemberIds = todayCardQueryService.findTodayCardMemberIds(memberId);
    todayCardService.createTodayCardIntroductions(memberId, todayCardMemberIds);
    List<MemberIntroductionProfileView> introductionProfileViews =
        todayCardQueryService.findTodayCardIntroductions(memberId, todayCardMemberIds);
    return ResponseEntity.ok(new MemberIntroductionProfileViews(introductionProfileViews));
  }

  @Operation(summary = "다이아 등급 이성 프로필 블러 해제")
  @PostMapping("/grade")
  public ResponseEntity<Void> createGradeIntroduction(
      @Valid @RequestBody MemberIntroductionCreateRequest request, @CurrentMemberId long memberId) {
    memberIntroductionService.createGradeIntroduction(memberId, request.introducedMemberId());
    return ResponseEntity.noContent().build();
  }

  @Operation(summary = "취미가 같은 이성 프로필 블러 해제")
  @PostMapping("/hobby")
  public ResponseEntity<Void> createHobbyIntroduction(
      @Valid @RequestBody MemberIntroductionCreateRequest request, @CurrentMemberId long memberId) {
    memberIntroductionService.createHobbyIntroduction(memberId, request.introducedMemberId());
    return ResponseEntity.noContent().build();
  }

  @Operation(summary = "종교가 같은 이성 프로필 블러 해제")
  @PostMapping("/religion")
  public ResponseEntity<Void> createReligionIntroduction(
      @Valid @RequestBody MemberIntroductionCreateRequest request, @CurrentMemberId long memberId) {
    memberIntroductionService.createReligionIntroduction(memberId, request.introducedMemberId());
    return ResponseEntity.noContent().build();
  }

  @Operation(summary = "지역이 같은 이성 프로필 블러 해제")
  @PostMapping("/city")
  public ResponseEntity<Void> createCityIntroduction(
      @Valid @RequestBody MemberIntroductionCreateRequest request, @CurrentMemberId long memberId) {
    memberIntroductionService.createCityIntroduction(memberId, request.introducedMemberId());
    return ResponseEntity.noContent().build();
  }

  @Operation(summary = "최근에 가입한 이성 프로필 블러 해제")
  @PostMapping("/recent")
  public ResponseEntity<Void> createRecentIntroduction(
      @Valid @RequestBody MemberIntroductionCreateRequest request, @CurrentMemberId long memberId) {
    memberIntroductionService.createRecentIntroduction(memberId, request.introducedMemberId());
    return ResponseEntity.noContent().build();
  }

  @Operation(summary = "소울 메이트 이성 조회")
  @GetMapping("/soulmate")
  public ResponseEntity<MemberIntroductionProfileViews> findSoulmateIntroductions(
      @CurrentMemberId long memberId) {
    Set<Long> soulmateMemberIds = soulmateFinder.findSoulmateIds(memberId);
    List<MemberIntroductionProfileView> introductionProfileViews =
        introductionQueryService.findMemberIntroductionProfileViews(memberId, soulmateMemberIds);
    return ResponseEntity.ok(new MemberIntroductionProfileViews(introductionProfileViews));
  }

  @Operation(summary = "소울 메이트 이성 프로필 블러 해제")
  @PostMapping("/soulmate")
  public ResponseEntity<Void> createSoulmateIntroduction(
      @Valid @RequestBody MemberIntroductionCreateRequest request, @CurrentMemberId long memberId) {
    memberIntroductionService.createSoulmateIntroduction(memberId, request.introducedMemberId());
    return ResponseEntity.noContent().build();
  }

  @Operation(summary = "연애고사에서 같은 답안을 제출한 이성 프로필 블러 해제")
  @PostMapping("/same-answer")
  public ResponseEntity<Void> createSameAnswerIntroduction(
      @Valid @RequestBody MemberIntroductionCreateRequest request, @CurrentMemberId long memberId) {
    memberIntroductionService.createSameAnswerIntroduction(memberId, request.introducedMemberId());
    return ResponseEntity.noContent().build();
  }

  @Operation(summary = "이상형 이성 조회")
  @GetMapping("/ideal")
  public ResponseEntity<MemberIntroductionProfileViews> findIdealIntroductions(
      @CurrentMemberId long memberId) {
    List<MemberIntroductionProfileView> introductionProfileViews =
        introductionQueryService.findIdealIntroductions(memberId);
    return ResponseEntity.ok(new MemberIntroductionProfileViews(introductionProfileViews));
  }

  @Operation(summary = "이상형 이성 프로필 블러 해제")
  @PostMapping("/ideal")
  public ResponseEntity<Void> createIdealIntroduction(
      @Valid @RequestBody MemberIntroductionCreateRequest request, @CurrentMemberId long memberId) {
    memberIntroductionService.createIdealIntroduction(memberId, request.introducedMemberId());
    return ResponseEntity.noContent().build();
  }

  @Operation(summary = "유형별 이상형 오픈 상태 조회 (오픈한 유형, 오픈 시각)")
  @GetMapping("/personality-status")
  public ResponseEntity<PersonalityIntroductionOpenStatusView>
      findPersonalityIntroductionOpenStatus(@CurrentMemberId long memberId) {
    PersonalityIntroductionOpenStatusView openStatusView =
        personalityIntroductionQueryService
            .findOpenStatus(memberId)
            .orElseGet(() -> new PersonalityIntroductionOpenStatusView(null, null));
    return ResponseEntity.ok(openStatusView);
  }

  @Operation(summary = "유형별 이상형 조회 (하루 1회 오픈, 최대 3명)")
  @GetMapping("/{personalityType}")
  public ResponseEntity<MemberIntroductionProfileViews> findPersonalityTypeIntroductions(
      @PathVariable AnswerPersonalityType personalityType, @CurrentMemberId long memberId) {
    List<MemberIntroductionProfileView> introductionProfileViews =
        personalityIntroductionQueryService.open(memberId, personalityType);
    return ResponseEntity.ok(new MemberIntroductionProfileViews(introductionProfileViews));
  }

  @Operation(summary = "유형별 이상형 프로필 상세 언락 (첫 상대 무료, 이후 하트 차감)")
  @PostMapping("/{personalityType}/{targetMemberId}")
  public ResponseEntity<Void> unlockPersonalityTypeIntroduction(
      @PathVariable AnswerPersonalityType personalityType,
      @PathVariable long targetMemberId,
      @CurrentMemberId long memberId) {
    personalityIntroductionUnlockService.unlock(memberId, personalityType, targetMemberId);
    return ResponseEntity.noContent().build();
  }
}
