package kr.co.deepple.member.adapter.out.persistence;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import kr.co.deepple.member.application.port.out.MemberCommandRepository;
import kr.co.deepple.member.domain.KakaoId;
import kr.co.deepple.member.domain.Member;
import kr.co.deepple.member.domain.PhoneNumber;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
public class MemberCommandRepositoryImpl implements MemberCommandRepository {

  private final MemberCommandJpaRepository memberCommandJpaRepository;

  @Override
  public Member save(Member member) {
    return memberCommandJpaRepository.save(member);
  }

  @Override
  public Member saveAndFlush(Member member) {
    return memberCommandJpaRepository.saveAndFlush(member);
  }

  @Override
  public Optional<Member> findById(long id) {
    return memberCommandJpaRepository.findById(id);
  }

  @Override
  public Optional<Member> findByIdForUpdate(long id) {
    return memberCommandJpaRepository.findByIdForUpdate(id);
  }

  @Override
  public boolean existsByPhoneNumberAndIdNot(String phoneNumber, long id) {
    return memberCommandJpaRepository.existsByPhoneNumberAndIdNot(
        PhoneNumber.from(phoneNumber), id);
  }

  @Override
  public boolean existsByKakaoIdAndIdNot(String kakaoId, long id) {
    return memberCommandJpaRepository.existsByKakaoIdAndIdNot(KakaoId.from(kakaoId), id);
  }

  @Override
  public boolean existsById(long id) {
    return memberCommandJpaRepository.existsById(id);
  }

  @Override
  public List<Member> findAllById(Set<Long> ids) {
    return memberCommandJpaRepository.findAllById(ids);
  }

  @Override
  public List<Member> saveAll(List<Member> members) {
    return memberCommandJpaRepository.saveAll(members);
  }

  @Override
  public void deleteBefore(LocalDateTime dateTime) {
    memberCommandJpaRepository.deleteAllBefore(dateTime);
  }

  @Override
  @Transactional
  public void updateLastAccessedAt(long memberId, LocalDateTime accessedAt) {
    memberCommandJpaRepository.updateLastAccessedAt(memberId, accessedAt);
  }
}
