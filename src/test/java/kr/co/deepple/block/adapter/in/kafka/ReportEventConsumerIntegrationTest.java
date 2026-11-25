package kr.co.deepple.block.adapter.in.kafka;

import static org.assertj.core.api.Assertions.assertThat;

import kr.co.deepple.block.application.port.out.BlockRepository;
import kr.co.deepple.common.support.IntegrationTestSupport;
import kr.co.deepple.member.application.port.out.MemberRepository;
import kr.co.deepple.member.domain.Member;
import kr.co.deepple.member.fixture.MemberFixture;
import kr.co.deepple.report.event.ReportCreatedEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@DisplayName("block report 토픽 소비자 통합 테스트")
class ReportEventConsumerIntegrationTest extends IntegrationTestSupport {

  @Autowired private BlockRepository blockRepository;

  @Autowired private MemberRepository memberRepository;

  @Test
  @DisplayName("같은 신고 이벤트를 두 번 받아도 차단은 하나만 만든다")
  void createBlockOnceWhenSameReportCreatedEventConsumedTwice() {
    // given
    Member reporter = memberRepository.save(MemberFixture.builder().build());
    Member reportee = memberRepository.save(MemberFixture.builder().build());
    ReportCreatedEvent event = ReportCreatedEvent.of(reporter.getId(), reportee.getId());

    // when
    publishEvent(event);
    publishEvent(event);

    // then
    awaitEventProcessing();
    assertThat(blockRepository.count()).isEqualTo(1L);
  }
}
