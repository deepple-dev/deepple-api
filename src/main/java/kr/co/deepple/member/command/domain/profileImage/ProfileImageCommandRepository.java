package kr.co.deepple.member.command.domain.profileImage;

import java.util.List;
import java.util.Optional;

public interface ProfileImageCommandRepository {
    ProfileImage save(ProfileImage profileImage);

    boolean existsByMemberIdAndIsPrimary(Long memberId);

    void saveAll(List<ProfileImage> profileImages);

    Optional<ProfileImage> findById(Long id);

    void delete(ProfileImage profileImage);

    List<ProfileImage> findByMemberId(Long memberId);

    void deleteByMemberId(Long memberId);
}
