package kr.co.deepple.member.command.infra.profileImage;

import kr.co.deepple.member.command.domain.profileImage.ProfileImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface ProfileImageJpaRepository extends JpaRepository<ProfileImage, Long> {
    boolean existsByMemberIdAndIsPrimary(Long memberId, boolean isPrimary);

    void delete(ProfileImage profileImage);

    List<ProfileImage> findByMemberId(Long memberId);

    void deleteAllByMemberId(Long memberId);
}
