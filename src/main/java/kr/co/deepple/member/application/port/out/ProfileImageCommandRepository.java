package kr.co.deepple.member.application.port.out;

import java.util.List;
import java.util.Optional;
import kr.co.deepple.member.domain.ProfileImage;

public interface ProfileImageCommandRepository {
  ProfileImage save(ProfileImage profileImage);

  boolean existsByMemberIdAndIsPrimary(long memberId);

  void saveAll(List<ProfileImage> profileImages);

  Optional<ProfileImage> findById(long id);

  void delete(ProfileImage profileImage);

  List<ProfileImage> findByMemberId(long memberId);

  void deleteByMemberId(long memberId);
}
