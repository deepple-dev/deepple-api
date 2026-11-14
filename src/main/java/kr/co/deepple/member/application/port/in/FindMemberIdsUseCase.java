package kr.co.deepple.member.application.port.in;

import java.util.Set;

public interface FindMemberIdsUseCase {
  Set<Long> findMemberIds(String nicknameKeyword, String phoneNumber);
}
