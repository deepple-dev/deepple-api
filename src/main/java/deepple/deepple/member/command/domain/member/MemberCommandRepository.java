package deepple.deepple.member.command.domain.member;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;


public interface MemberCommandRepository {
    Member save(Member member);

    Optional<Member> findById(Long id);

    Optional<Member> findByPhoneNumber(String phoneNumber);

    boolean existsByPhoneNumberAndIdNot(String phoneNumber, Long id);

    boolean existsByKakaoIdAndIdNot(String kakaoId, Long id);

    boolean existsById(Long id);

    List<Member> findAllById(Set<Long> memberIds);

    List<Member> saveAll(List<Member> members);

    void deleteBefore(LocalDateTime dateTime);

    List<Member> findAll();
}
