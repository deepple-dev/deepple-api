package deepple.deepple.member.command.infra.member;

import deepple.deepple.member.command.domain.member.Member;
import deepple.deepple.member.command.domain.member.MemberCommandRepository;
import deepple.deepple.member.command.domain.member.vo.KakaoId;
import deepple.deepple.member.command.domain.member.vo.PhoneNumber;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class MemberCommandRepositoryImpl implements MemberCommandRepository {

    private final MemberCommandJpaRepository memberCommandJpaRepository;

    @Override
    public Member save(Member member) {
        return memberCommandJpaRepository.save(member);
    }

    @Override
    public Optional<Member> findById(Long id) {
        return memberCommandJpaRepository.findById(id);
    }

    @Override
    public Optional<Member> findByPhoneNumber(String phoneNumber) {
        return memberCommandJpaRepository.findByPhoneNumber(PhoneNumber.from(phoneNumber));
    }

    @Override
    public boolean existsByPhoneNumberAndIdNot(String phoneNumber, Long id) {
        return memberCommandJpaRepository.existsByPhoneNumberAndIdNot(PhoneNumber.from(phoneNumber), id);
    }

    @Override
    public boolean existsByKakaoIdAndIdNot(String kakaoId, Long id) {
        return memberCommandJpaRepository.existsByKakaoIdAndIdNot(KakaoId.from(kakaoId), id);
    }

    @Override
    public boolean existsById(Long id) {
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
    public void deleteBefore(final LocalDateTime dateTime) {
        memberCommandJpaRepository.deleteAllBefore(dateTime);
    }

    @Override
    public List<Member> findAll() {
        memberCommandJpaRepository.findAll();
    }
}
