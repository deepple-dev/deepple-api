package kr.co.deepple.datingexam.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verifyNoInteractions;

import java.util.List;
import java.util.Map;
import kr.co.deepple.datingexam.application.port.out.DatingExamSubmitResultRepository;
import kr.co.deepple.datingexam.domain.AnswerPersonalityType;
import kr.co.deepple.datingexam.fixture.DatingExamSubmitResultFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FindDominantPersonalityTypesServiceTest {

  @Mock private DatingExamSubmitResultRepository datingExamSubmitResultRepository;

  @InjectMocks private FindDominantPersonalityTypesService findDominantPersonalityTypesService;

  @Test
  @DisplayName("연애고사 결과가 있는 회원의 대표 성격 유형 이름을 회원 번호별로 돌려준다.")
  void findDominantPersonalityTypes() {
    given(datingExamSubmitResultRepository.findByMemberIdIn(List.of(1L, 2L, 3L)))
        .willReturn(
            List.of(
                DatingExamSubmitResultFixture.builder()
                    .memberId(1L)
                    .counts(Map.of(AnswerPersonalityType.STIMULATING_ADVENTURER, 3))
                    .build(),
                DatingExamSubmitResultFixture.builder()
                    .memberId(2L)
                    .counts(Map.of(AnswerPersonalityType.RATIONAL_REALIST, 2))
                    .build()));

    Map<Long, String> types =
        findDominantPersonalityTypesService.findDominantPersonalityTypes(List.of(1L, 2L, 3L));

    assertThat(types)
        .containsOnly(Map.entry(1L, "STIMULATING_ADVENTURER"), Map.entry(2L, "RATIONAL_REALIST"));
  }

  @Test
  @DisplayName("회원 번호가 없으면 조회하지 않고 빈 결과를 돌려준다.")
  void returnEmptyWhenNoMemberIds() {
    assertThat(findDominantPersonalityTypesService.findDominantPersonalityTypes(List.of()))
        .isEmpty();
    verifyNoInteractions(datingExamSubmitResultRepository);
  }
}
