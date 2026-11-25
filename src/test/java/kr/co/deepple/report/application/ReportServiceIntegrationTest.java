package kr.co.deepple.report.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;
import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.common.support.IntegrationTestSupport;
import kr.co.deepple.feed.application.port.out.FeedAuthorRepository;
import kr.co.deepple.feed.application.port.out.FeedCommentRepository;
import kr.co.deepple.feed.application.port.out.FeedRepository;
import kr.co.deepple.feed.domain.FeedAuthor;
import kr.co.deepple.feed.domain.FeedAuthorType;
import kr.co.deepple.feed.domain.FeedCommentStatus;
import kr.co.deepple.feed.domain.FeedErrorCode;
import kr.co.deepple.feed.domain.FeedStatus;
import kr.co.deepple.feed.fixture.FeedAuthorFixture;
import kr.co.deepple.feed.fixture.FeedCommentFixture;
import kr.co.deepple.feed.fixture.OfficialFeedFixture;
import kr.co.deepple.feed.fixture.TextFeedFixture;
import kr.co.deepple.member.application.port.out.MemberRepository;
import kr.co.deepple.member.fixture.MemberFixture;
import kr.co.deepple.report.application.dto.ReportCreateRequest;
import kr.co.deepple.report.application.port.out.ReportRepository;
import kr.co.deepple.report.domain.ReportErrorCode;
import kr.co.deepple.report.domain.ReportResult;
import kr.co.deepple.report.domain.ReportTargetType;
import kr.co.deepple.report.fixture.ReportFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;

@DisplayName("피드·댓글 신고 통합 테스트")
class ReportServiceIntegrationTest extends IntegrationTestSupport {

  @Autowired private ReportService reportService;

  @Autowired private ReportRepository reportRepository;

  @Autowired private FeedRepository feedRepository;

  @Autowired private FeedAuthorRepository feedAuthorRepository;

  @Autowired private FeedCommentRepository feedCommentRepository;

  @Autowired private MemberRepository memberRepository;

  private long writerMemberId;
  private List<Long> reporterIds;
  private FeedAuthor writer;
  private long feedId;

  @BeforeEach
  void setUp() {
    writerMemberId = saveMember();
    reporterIds = IntStream.range(0, 5).mapToObj(i -> saveMember()).toList();
    writer = feedAuthorRepository.save(activeMember(writerMemberId, "GOLD"));
    reporterIds.forEach(memberId -> feedAuthorRepository.save(activeMember(memberId, "GOLD")));
    feedId = saveFeed(writer, "GOLD");
  }

  @Test
  @DisplayName("피드 신고에 대상 종류와 피드 ID를 남긴다")
  void saveFeedReport() {
    // when
    reportService.report(request("FEED", feedId), reporterIds.get(0));

    // then
    assertThat(reportRepository.findAll())
        .singleElement()
        .satisfies(report -> {
          assertThat(report.getTargetType()).isEqualTo(ReportTargetType.FEED);
          assertThat(report.getTargetId()).isEqualTo(feedId);
          assertThat(report.getReporteeId()).isEqualTo(writerMemberId);
        });
  }

  @Test
  @DisplayName("서로 다른 회원 4명이 신고하면 그대로 두고, 5명째가 신고하면 피드를 숨긴다")
  void hideFeedWhenFiveMembersReport() {
    // given
    reportByMembers(4, "FEED", feedId);
    awaitEventProcessing();
    assertThat(feedStatus(feedId)).isEqualTo(FeedStatus.PUBLISHED);

    // when
    reportService.report(request("FEED", feedId), reporterIds.get(4));
    awaitEventProcessing();

    // then
    assertThat(feedStatus(feedId)).isEqualTo(FeedStatus.HIDDEN_BY_REPORT);
  }

  @Test
  @DisplayName("관리자가 기각한 신고도 5명에 센다")
  void countRejectedReports() {
    // given
    reporterIds
        .subList(0, 4)
        .forEach(reporterId ->
            reportRepository.save(
                ReportFixture.builder()
                    .reporterId(reporterId)
                    .reporteeId(writerMemberId)
                    .targetType(ReportTargetType.FEED)
                    .targetId(feedId)
                    .result(ReportResult.REJECTED)
                    .build()));

    // when
    reportService.report(request("FEED", feedId), reporterIds.get(4));
    awaitEventProcessing();

    // then
    assertThat(feedStatus(feedId)).isEqualTo(FeedStatus.HIDDEN_BY_REPORT);
  }

  @Test
  @DisplayName("서로 다른 회원 5명이 댓글을 신고하면 댓글을 숨긴다")
  void hideCommentWhenFiveMembersReport() {
    // given
    long commentId =
        feedCommentRepository
            .save(FeedCommentFixture.builder().feedId(feedId).authorId(writer.getId()).build())
            .getId();

    // when
    reportByMembers(5, "COMMENT", commentId);
    awaitEventProcessing();

    // then
    assertThat(feedCommentRepository.findById(commentId).orElseThrow().getStatus())
        .isEqualTo(FeedCommentStatus.HIDDEN_BY_REPORT);
  }

  @Test
  @DisplayName("자동 차단이 반영된 뒤 같은 대상을 다시 신고해도 이미 신고했다고 거절한다")
  void rejectDuplicateReportAfterAutoBlock() {
    // given
    long reporterId = reporterIds.get(0);
    reportService.report(request("FEED", feedId), reporterId);
    awaitEventProcessing();

    // when & then
    assertThatThrownBy(() -> reportService.report(request("FEED", feedId), reporterId))
        .isInstanceOf(DeeppleException.class)
        .extracting("errorCode")
        .isEqualTo(ReportErrorCode.REPORT_ALREADY_EXISTS);
    assertThat(reportRepository.count()).isEqualTo(1L);
  }

  @Test
  @DisplayName("볼 수 없는 피드는 피드 없음으로 거절한다")
  void rejectInvisibleFeed() {
    // given
    long otherGradeFeedId = saveFeed(writer, "SILVER");

    // when & then
    assertThatThrownBy(
            () -> reportService.report(request("FEED", otherGradeFeedId), reporterIds.get(0)))
        .isInstanceOf(DeeppleException.class)
        .extracting("errorCode")
        .isEqualTo(FeedErrorCode.FEED_NOT_FOUND);
    assertThat(reportRepository.count()).isZero();
  }

  @Test
  @DisplayName("작성자가 reporteeId와 다르거나 관리자 피드면 신고 대상이 올바르지 않다고 거절한다")
  void rejectMismatchedTarget() {
    // given
    FeedAuthor official =
        feedAuthorRepository.save(FeedAuthorFixture.builder().memberId(99L).build());
    ReflectionTestUtils.setField(official, "authorType", FeedAuthorType.OFFICIAL);
    ReflectionTestUtils.setField(official, "memberId", null);
    feedAuthorRepository.save(official);
    long officialFeedId =
        feedRepository
            .save(
                OfficialFeedFixture.builder()
                    .authorId(official.getId())
                    .targetGrades(Set.of())
                    .build())
            .getId();
    ReportCreateRequest wrongReportee =
        new ReportCreateRequest(reporterIds.get(1), "FEED", feedId, "ETC", "");

    // when & then
    for (ReportCreateRequest request : List.of(wrongReportee, request("FEED", officialFeedId))) {
      assertThatThrownBy(() -> reportService.report(request, reporterIds.get(0)))
          .isInstanceOf(DeeppleException.class)
          .extracting("errorCode")
          .isEqualTo(ReportErrorCode.INVALID_REPORT_TARGET);
    }
    assertThat(reportRepository.count()).isZero();
  }

  @Test
  @DisplayName("삭제되거나 숨겨진 댓글은 댓글 없음으로 거절한다")
  void rejectInvisibleComment() {
    // given
    List<Long> invisibleCommentIds =
        List.of(
            feedCommentRepository
                .save(
                    FeedCommentFixture.builder()
                        .feedId(feedId)
                        .authorId(writer.getId())
                        .deletedAt(LocalDateTime.now())
                        .build())
                .getId(),
            feedCommentRepository
                .save(
                    FeedCommentFixture.builder()
                        .feedId(feedId)
                        .authorId(writer.getId())
                        .status(FeedCommentStatus.HIDDEN_BY_REPORT)
                        .build())
                .getId());

    // when & then
    for (long commentId : invisibleCommentIds) {
      assertThatThrownBy(
              () -> reportService.report(request("COMMENT", commentId), reporterIds.get(0)))
          .isInstanceOf(DeeppleException.class)
          .extracting("errorCode")
          .isEqualTo(FeedErrorCode.FEED_COMMENT_NOT_FOUND);
    }
    assertThat(reportRepository.count()).isZero();
  }

  private void reportByMembers(int count, String targetType, long targetId) {
    reporterIds
        .subList(0, count)
        .forEach(reporterId -> reportService.report(request(targetType, targetId), reporterId));
  }

  private FeedStatus feedStatus(long id) {
    return feedRepository.findById(id).orElseThrow().getStatus();
  }

  private long saveFeed(FeedAuthor author, String grade) {
    return feedRepository
        .save(
            TextFeedFixture.builder()
                .authorId(author.getId())
                .grade(grade)
                .status(FeedStatus.PUBLISHED)
                .publishedAt(LocalDateTime.now())
                .build())
        .getId();
  }

  private ReportCreateRequest request(String targetType, long targetId) {
    return new ReportCreateRequest(writerMemberId, targetType, targetId, "ETC", "");
  }

  private long saveMember() {
    return memberRepository.save(MemberFixture.builder().build()).getId();
  }

  private static FeedAuthor activeMember(long memberId, String grade) {
    return FeedAuthorFixture.builder()
        .memberId(memberId)
        .activityStatus("ACTIVE")
        .grade(grade)
        .build();
  }
}
