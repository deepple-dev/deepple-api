package deepple.deepple.member.command.application.member;

import deepple.deepple.member.command.application.member.exception.MemberNotFoundException;
import deepple.deepple.member.command.domain.member.Grade;
import deepple.deepple.member.command.domain.member.Member;
import deepple.deepple.member.command.domain.member.MemberCommandRepository;
import deepple.deepple.member.presentation.member.dto.AdminMemberSettingUpdateRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminMemberServiceTest {

    @InjectMocks
    AdminMemberService adminMemberService;

    @Mock
    MemberCommandRepository memberCommandRepository;

    @Test
    @DisplayName("관리자 회원 설정 업데이트 테스트")
    void updateMemberSetting() {
        // given
        long memberId = 1L;
        AdminMemberSettingUpdateRequest request = new AdminMemberSettingUpdateRequest(
            Grade.SILVER.name(), true, true
        );
        final Member member = mock(Member.class);
        when(memberCommandRepository.findById(memberId)).thenReturn(Optional.of(member));

        // when
        adminMemberService.updateMemberSetting(memberId, request);

        // then
        verify(member).updateSetting(Grade.from(request.grade()), request.isVip(), request.isPushNotificationEnabled());
    }

    @Test
    @DisplayName("회원 설정 업데이트 시 회원이 존재하지 않을 경우 예외 발생 테스트")
    void updateMemberSetting_MemberNotFound() {
        // given
        long memberId = 1L;
        AdminMemberSettingUpdateRequest request = new AdminMemberSettingUpdateRequest(
            Grade.SILVER.name(), true, true
        );
        when(memberCommandRepository.findById(memberId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> adminMemberService.updateMemberSetting(memberId, request))
            .isInstanceOf(MemberNotFoundException.class);
    }
}
