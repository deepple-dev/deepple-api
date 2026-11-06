package deepple.deepple.member.command.application.member;

import deepple.deepple.common.exception.DeeppleException;
import deepple.deepple.member.command.domain.MemberErrorCode;
import deepple.deepple.member.command.domain.member.Grade;
import deepple.deepple.member.command.domain.member.Member;
import deepple.deepple.member.command.domain.member.MemberCommandRepository;
import deepple.deepple.member.presentation.member.dto.AdminMemberSettingUpdateRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminMemberService {

    private final MemberCommandRepository memberCommandRepository;

    @Transactional
    public void updateMemberSetting(long memberId, AdminMemberSettingUpdateRequest request) {
        Grade grade = Grade.from(request.grade());
        Member member = getMember(memberId);
        member.updateSetting(
            grade,
            request.isVip(),
            request.isPushNotificationEnabled()
        );
        log.atInfo().addKeyValue("memberId", memberId).log("관리자 회원 설정 변경");
    }

    private Member getMember(long memberId) {
        return memberCommandRepository.findById(memberId)
            .orElseThrow(() -> new DeeppleException(MemberErrorCode.MEMBER_NOT_FOUND, "memberId=" + memberId));
    }
}
