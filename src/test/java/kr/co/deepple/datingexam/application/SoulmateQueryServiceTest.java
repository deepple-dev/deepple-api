package kr.co.deepple.datingexam.application;

import kr.co.deepple.datingexam.application.port.out.SoulmateQueryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SoulmateQueryServiceTest {

    @InjectMocks
    private SoulmateQueryService soulmateQueryService;

    @Mock
    private SoulmateQueryRepository soulmateQueryRepository;

    @Nested
    @DisplayName("소울 메이트 아이디 목록을 조회할 때,")
    class FindSoulmateIds {
        @Test
        @DisplayName("soulmateQueryRepository에서 조회된 아이디 목록을 반환한다.")
        void findSoulmateIds_Success() {
            // Given
            Long memberId = 1L;
            Set<Long> expectedSoulmateIds = Set.of(3L, 4L, 5L);
            when(soulmateQueryRepository.findSameAnswerMemberIds(memberId)).thenReturn(expectedSoulmateIds);

            // When
            Set<Long> soulmateIds = soulmateQueryService.findSoulmateIds(memberId);

            // Then
            assertThat(soulmateIds).isEqualTo(expectedSoulmateIds);
        }
    }
}