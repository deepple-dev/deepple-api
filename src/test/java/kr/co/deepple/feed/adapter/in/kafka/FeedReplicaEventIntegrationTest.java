package kr.co.deepple.feed.adapter.in.kafka;

import static org.assertj.core.api.Assertions.assertThat;

import kr.co.deepple.block.event.BlockCreatedEvent;
import kr.co.deepple.common.support.IntegrationTestSupport;
import kr.co.deepple.datingexam.event.DominantPersonalityTypeChangedEvent;
import kr.co.deepple.feed.application.port.out.FeedAuthorRepository;
import kr.co.deepple.feed.application.port.out.FeedBlockRepository;
import kr.co.deepple.feed.domain.FeedAuthor;
import kr.co.deepple.member.event.MemberActivityStatusChangedEvent;
import kr.co.deepple.member.event.MemberGradeChangedEvent;
import kr.co.deepple.member.event.MemberPrimaryProfileImageChangedEvent;
import kr.co.deepple.member.event.MemberProfileChangedEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@DisplayName("피드 사본 이벤트 Kafka 전달 통합 테스트")
class FeedReplicaEventIntegrationTest extends IntegrationTestSupport {

  private static final long MEMBER_ID = 1L;
  private static final long BLOCKED_ID = 2L;

  @Autowired private FeedAuthorRepository feedAuthorRepository;

  @Autowired private FeedBlockRepository feedBlockRepository;

  @Test
  @DisplayName("member·datingexam 토픽의 이벤트로 작성자 사본을 채운다")
  void syncAuthorFromKafka() {
    // when
    publishEvent(MemberProfileChangedEvent.of(MEMBER_ID, "진저", "FEMALE", 1998, "SEOUL", "ENFP"));
    publishEvent(
        MemberPrimaryProfileImageChangedEvent.of(MEMBER_ID, "https://cdn.example.com/1.webp"));
    publishEvent(MemberActivityStatusChangedEvent.of(MEMBER_ID, "ACTIVE"));
    publishEvent(MemberGradeChangedEvent.of(MEMBER_ID, "GOLD"));
    publishEvent(DominantPersonalityTypeChangedEvent.of(MEMBER_ID, "GROWING_RUNNING_MATE"));
    awaitEventProcessing();

    // then
    FeedAuthor author = feedAuthorRepository.findByMemberId(MEMBER_ID).orElseThrow();
    assertThat(author.getNickname()).isEqualTo("진저");
    assertThat(author.getProfileImageUrl()).isEqualTo("https://cdn.example.com/1.webp");
    assertThat(author.getActivityStatus()).isEqualTo("ACTIVE");
    assertThat(author.getGrade()).isEqualTo("GOLD");
    assertThat(author.getPersonalityType()).isEqualTo("GROWING_RUNNING_MATE");
  }

  @Test
  @DisplayName("같은 차단 이벤트가 두 번 와도 차단 사본은 하나만 남는다")
  void syncBlockOnce() {
    // given
    BlockCreatedEvent event = BlockCreatedEvent.of(MEMBER_ID, BLOCKED_ID);

    // when
    publishEvent(event);
    publishEvent(event);
    awaitEventProcessing();

    // then
    assertThat(feedBlockRepository.findAll())
        .singleElement()
        .satisfies(block -> {
          assertThat(block.getBlockerId()).isEqualTo(MEMBER_ID);
          assertThat(block.getBlockedId()).isEqualTo(BLOCKED_ID);
        });
  }
}
