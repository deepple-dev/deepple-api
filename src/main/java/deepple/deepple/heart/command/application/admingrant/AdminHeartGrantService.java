package deepple.deepple.heart.command.application.admingrant;

import deepple.deepple.heart.command.domain.hearttransaction.vo.HeartAmount;
import deepple.deepple.heart.presentation.hearttransaction.AdminHeartGrantRequest;
import deepple.deepple.member.command.application.member.exception.MemberNotFoundException;
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
        Member member = memberCommandRepository.findById(request.memberId())
            .orElseThrow(MemberNotFoundException::new);

        HeartAmount heartAmount = HeartAmount.from(request.amount());
        member.gainAdminGrantedHeart(heartAmount, adminId, request.reason());

        log.info("관리자(id: {})가 멤버(id: {})에게 하트 {}개 지급. 사유: {}",
            adminId, request.memberId(), request.amount(), request.reason());
    }
}
