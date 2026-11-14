package kr.co.deepple.recommendation.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Optional;
import kr.co.deepple.recommendation.application.port.out.MemberIntroductionRepository;
import kr.co.deepple.recommendation.domain.IntroductionType;
import kr.co.deepple.recommendation.fixture.MemberIntroductionFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FindIntroductionTypeServiceTest {

  @Mock private MemberIntroductionRepository memberIntroductionRepository;

  @InjectMocks private FindIntroductionTypeService findIntroductionTypeService;

  @Test
  @DisplayName("소개받은 기록이 있으면 소개 종류를 문자열로 돌려준다.")
  void findTypeOfIntroduction() {
    when(memberIntroductionRepository.findByMemberIdAndIntroducedMemberId(1L, 2L))
        .thenReturn(
            Optional.of(
                MemberIntroductionFixture.builder().type(IntroductionType.SOULMATE).build()));

    assertThat(findIntroductionTypeService.findIntroductionType(1L, 2L)).contains("SOULMATE");
  }

  @Test
  @DisplayName("소개받은 기록이 없으면 비어 있다.")
  void returnEmptyWhenNotIntroduced() {
    when(memberIntroductionRepository.findByMemberIdAndIntroducedMemberId(1L, 2L))
        .thenReturn(Optional.empty());

    assertThat(findIntroductionTypeService.findIntroductionType(1L, 2L)).isEmpty();
  }
}
