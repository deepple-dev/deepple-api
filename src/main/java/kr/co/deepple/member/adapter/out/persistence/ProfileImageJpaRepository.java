package kr.co.deepple.member.adapter.out.persistence;

import java.util.List;
import kr.co.deepple.member.domain.ProfileImage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfileImageJpaRepository extends JpaRepository<ProfileImage, Long> {
  boolean existsByMemberIdAndIsPrimary(long memberId, boolean isPrimary);

  void delete(ProfileImage profileImage);

  List<ProfileImage> findByMemberId(long memberId);

  void deleteAllByMemberId(long memberId);
}
