package kr.co.deepple.member.application.port.out;

import java.util.List;
import kr.co.deepple.member.domain.ProfileImage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfileImageRepository extends JpaRepository<ProfileImage, Long> {
  List<ProfileImage> findByMemberId(long memberId);

  void deleteByMemberId(long memberId);
}
