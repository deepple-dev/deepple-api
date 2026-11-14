package kr.co.deepple.member.application;

import java.util.Set;
import kr.co.deepple.member.application.port.in.FindMemberIdsUseCase;
import kr.co.deepple.member.application.port.out.MemberQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FindMemberIdsService implements FindMemberIdsUseCase {

  private final MemberQueryRepository memberQueryRepository;

  @Override
  @Transactional(readOnly = true)
  public Set<Long> findMemberIds(String nicknameKeyword, String phoneNumber) {
    if (nicknameKeyword == null && phoneNumber == null) {
      throw new IllegalArgumentException("닉네임과 전화번호 중 하나는 있어야 합니다.");
    }
    return memberQueryRepository.findIdsByNicknameAndPhoneNumber(nicknameKeyword, phoneNumber);
  }
}
