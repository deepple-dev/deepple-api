package deepple.deepple.heart.command.application.admingrant;

import deepple.deepple.common.exception.DeeppleException;
import deepple.deepple.heart.presentation.hearttransaction.AdminHeartGrantRequest;
import deepple.deepple.member.command.domain.MemberErrorCode;
import deepple.deepple.member.command.domain.member.Member;
import deepple.deepple.member.command.domain.member.MemberCommandRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminHeartGrantService {

    private final MemberCommandRepository memberCommandRepository;

    @Transactional
    public void grant(long adminId, AdminHeartGrantRequest request) {
        Member member = memberCommandRepository.findByIdForUpdate(request.memberId())
            .orElseThrow(() -> new DeeppleException(
                MemberErrorCode.MEMBER_NOT_FOUND,
                "memberId=" + request.memberId()
            ));

        member.gainAdminGrantedHeart(request.amount(), adminId, request.reason());

        log.atInfo()
            .addKeyValue("memberId", request.memberId())
            .addKeyValue("amount", request.amount())
            .log("관리자 하트 지급");
    }
}
