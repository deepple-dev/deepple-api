package kr.co.deepple.member.command.domain.member;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface MemberCommandRepository {
  Member save(Member member);

  Optional<Member> findById(long id);

  Optional<Member> findByIdForUpdate(long id);

  Optional<Member> findByPhoneNumber(String phoneNumber);

  boolean existsByPhoneNumberAndIdNot(String phoneNumber, long id);

  boolean existsByKakaoIdAndIdNot(String kakaoId, long id);

  boolean existsById(long id);

  List<Member> findAllById(Set<Long> memberIds);

  List<Member> saveAll(List<Member> members);

  void deleteBefore(LocalDateTime dateTime);

  void updateLastAccessedAt(long memberId, LocalDateTime accessedAt);
}
