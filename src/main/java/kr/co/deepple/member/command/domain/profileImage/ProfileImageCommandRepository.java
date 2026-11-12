package kr.co.deepple.member.command.domain.profileImage;

import java.util.List;
import java.util.Optional;

public interface ProfileImageCommandRepository {
  ProfileImage save(ProfileImage profileImage);

  boolean existsByMemberIdAndIsPrimary(long memberId);

  void saveAll(List<ProfileImage> profileImages);

  Optional<ProfileImage> findById(long id);

  void delete(ProfileImage profileImage);

  List<ProfileImage> findByMemberId(long memberId);

  void deleteByMemberId(long memberId);
}
