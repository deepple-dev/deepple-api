package kr.co.deepple.admin.command.application.warning;

import kr.co.deepple.admin.command.domain.warning.Warning;
import kr.co.deepple.admin.command.domain.warning.WarningCommandRepository;
import kr.co.deepple.admin.presentation.warning.WarningCreateRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WarningServiceTest {

    @Mock
    private WarningCommandRepository warningCommandRepository;

    @InjectMocks
    private WarningService warningService;

    @Test
    @DisplayName("issue() 호출 시 경고 저장 및 조회 로직이 실행된다")
    void issueShouldSaveWarningAndCountWarnings() {
        // given
        long adminId = 1L;
        long memberId = 2L;
        var request = new WarningCreateRequest(memberId, Set.of("INAPPROPRIATE_INTERVIEW"), true);

        when(warningCommandRepository.countByMemberIdAndIsCriticalTrue(memberId)).thenReturn(1L);

        // when
        warningService.issue(adminId, request);

        // then
        verify(warningCommandRepository).countByMemberIdAndIsCriticalTrue(memberId);
        verify(warningCommandRepository).save(any(Warning.class));
    }
}
