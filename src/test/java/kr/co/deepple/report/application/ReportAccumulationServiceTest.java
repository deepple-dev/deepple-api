package kr.co.deepple.report.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import kr.co.deepple.report.application.port.out.ReportRepository;
import kr.co.deepple.report.domain.ReportTargetType;
import kr.co.deepple.report.event.ReportAccumulatedEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class ReportAccumulationServiceTest {

  private static final long FEED_ID = 100L;

  @Mock private ReportRepository reportRepository;

  @Mock private ApplicationEventPublisher eventPublisher;

  @InjectMocks private ReportAccumulationService reportAccumulationService;

  @ParameterizedTest
  @ValueSource(longs = {5L, 6L})
  @DisplayName("신고자가 5명 이상이면 신고 누적 이벤트를 발행한다.")
  void publishWhenReachingThreshold(long reporterCount) {
    // given
    when(reportRepository.countByTargetTypeAndTargetId(ReportTargetType.FEED, FEED_ID))
        .thenReturn(reporterCount);

    // when
    reportAccumulationService.checkAccumulation(ReportTargetType.FEED, FEED_ID);

    // then
    ArgumentCaptor<Object> event = ArgumentCaptor.forClass(Object.class);
    verify(eventPublisher).publishEvent(event.capture());
    assertThat(event.getValue())
        .isInstanceOfSatisfying(
            ReportAccumulatedEvent.class,
            accumulated -> {
              assertThat(accumulated.targetType()).isEqualTo("FEED");
              assertThat(accumulated.targetId()).isEqualTo(FEED_ID);
            });
  }

  @ParameterizedTest
  @ValueSource(longs = {1L, 4L})
  @DisplayName("신고자가 5명보다 적으면 이벤트를 발행하지 않는다.")
  void notPublishBelowThreshold(long reporterCount) {
    // given
    when(reportRepository.countByTargetTypeAndTargetId(ReportTargetType.FEED, FEED_ID))
        .thenReturn(reporterCount);

    // when
    reportAccumulationService.checkAccumulation(ReportTargetType.FEED, FEED_ID);

    // then
    verify(eventPublisher, never()).publishEvent(any(Object.class));
  }
}
