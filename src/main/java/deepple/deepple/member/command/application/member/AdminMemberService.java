package deepple.deepple.member.command.application.member;

import deepple.deepple.member.command.application.member.exception.MemberNotFoundException;
import deepple.deepple.member.command.domain.member.Grade;
import deepple.deepple.member.command.domain.member.Member;
import deepple.deepple.member.command.domain.member.MemberCommandRepository;
import deepple.deepple.member.presentation.member.dto.AdminMemberSettingUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    }

    private Member getMember(long memberId) {
        return memberCommandRepository.findById(memberId).orElseThrow(MemberNotFoundException::new);
    }
}
