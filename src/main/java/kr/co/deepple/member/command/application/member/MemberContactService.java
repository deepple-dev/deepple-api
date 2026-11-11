package kr.co.deepple.member.command.application.member;

import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.member.command.domain.MemberErrorCode;
import kr.co.deepple.member.command.domain.member.Member;
import kr.co.deepple.member.command.domain.member.MemberCommandRepository;
import kr.co.deepple.member.command.domain.member.vo.KakaoId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberContactService {

    private final MemberCommandRepository memberCommandRepository;

    @Transactional
    public void updateKakaoId(Long memberId, String kakaoId) {
        validateKakaoId(kakaoId, memberId);
        Member member = getMemberById(memberId);
        member.changePrimaryContactTypeToKakao(KakaoId.from(kakaoId));
    }

    @Transactional
    public void updatePhoneNumber(Long memberId, String phoneNumber) {
        validatePhoneNumber(phoneNumber, memberId);
        Member member = getMemberById(memberId);
        member.changePrimaryContactTypeToPhoneNumber(phoneNumber);
    }

    private void validatePhoneNumber(String phoneNumber, Long memberId) {
        if (memberCommandRepository.existsByPhoneNumberAndIdNot(phoneNumber, memberId)) {
            throw new DeeppleException(MemberErrorCode.PHONE_NUMBER_ALREADY_EXISTS, "memberId=" + memberId);
        }
    }

    private void validateKakaoId(String kakaoId, Long memberId) {
        if (memberCommandRepository.existsByKakaoIdAndIdNot(kakaoId, memberId)) {
            throw new DeeppleException(MemberErrorCode.KAKAO_ID_ALREADY_EXISTS, "memberId=" + memberId);
        }
    }

    private Member getMemberById(Long memberId) {
        return memberCommandRepository.findById(memberId)
            .orElseThrow(() -> new DeeppleException(MemberErrorCode.MEMBER_NOT_FOUND, "memberId=" + memberId));
    }
}
