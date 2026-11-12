package kr.co.deepple.member.command.infra.profileImage;

import java.util.List;
import java.util.Optional;
import kr.co.deepple.member.command.domain.profileImage.ProfileImage;
import kr.co.deepple.member.command.domain.profileImage.ProfileImageCommandRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ProfileImageCommandRepositoryImpl implements ProfileImageCommandRepository {

  private final ProfileImageJpaRepository profileImageJpaRepository;

  @Override
  public ProfileImage save(ProfileImage profileImage) {
    return profileImageJpaRepository.save(profileImage);
  }

  @Override
  public boolean existsByMemberIdAndIsPrimary(long memberId) {
    return profileImageJpaRepository.existsByMemberIdAndIsPrimary(memberId, true);
  }

  @Override
  public void saveAll(List<ProfileImage> profileImages) {
    profileImageJpaRepository.saveAll(profileImages);
  }

  @Override
  public Optional<ProfileImage> findById(long id) {
    return profileImageJpaRepository.findById(id);
  }

  @Override
  public void delete(ProfileImage profileImage) {
    profileImageJpaRepository.delete(profileImage);
  }

  @Override
  public java.util.List<ProfileImage> findByMemberId(long memberId) {
    return profileImageJpaRepository.findByMemberId(memberId);
  }

  @Override
  public void deleteByMemberId(long memberId) {
    profileImageJpaRepository.deleteAllByMemberId(memberId);
  }
}
