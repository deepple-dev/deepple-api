package kr.co.deepple.member.adapter.out.persistence;

import java.util.List;
import kr.co.deepple.common.support.RepositoryTestSupport;
import kr.co.deepple.member.application.dto.ProfileImageView;
import kr.co.deepple.member.domain.ProfileImage;
import kr.co.deepple.member.fixture.ProfileImageFixture;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

public class ProfileImageQueryRepositoryImplTest extends RepositoryTestSupport {

  @Autowired private ProfileImageQueryRepositoryImpl profileImageQueryRepository;

  @Autowired private TestEntityManager entityManager;

  @Test
  @DisplayName("자신의 프로필 이미지를 조회.")
  void findMyProfileImages() {
    // Given
    Long memberId = 1L;
    ProfileImage profileImage1 =
        ProfileImageFixture.builder()
            .memberId(memberId)
            .imageKey("images/1/a.webp")
            .primary(true)
            .order(1)
            .build();

    ProfileImage profileImage2 =
        ProfileImageFixture.builder()
            .memberId(memberId)
            .imageKey("images/1/b.webp")
            .primary(false)
            .order(2)
            .build();

    entityManager.persist(profileImage1);
    entityManager.persist(profileImage2);
    entityManager.flush();

    // When
    List<ProfileImageView> profileImageViews = profileImageQueryRepository.findByMemberId(memberId);

    // Then
    Assertions.assertThat(profileImageViews.size()).isEqualTo(2);

    ProfileImageView profileImageOrder1 = profileImageViews.getFirst();
    Assertions.assertThat(profileImageOrder1.id()).isEqualTo(profileImage1.getId());
    Assertions.assertThat(profileImageOrder1.url())
        .isEqualTo("https://deepple-test.s3.ap-northeast-2.amazonaws.com/images/1/a.webp");
    Assertions.assertThat(profileImageOrder1.isPrimary()).isEqualTo(profileImage1.isPrimary());
    Assertions.assertThat(profileImageOrder1.order()).isEqualTo(profileImage1.getOrder());

    ProfileImageView profileImageOrder2 = profileImageViews.getLast();
    Assertions.assertThat(profileImageOrder2.id()).isEqualTo(profileImage2.getId());
    Assertions.assertThat(profileImageOrder2.url())
        .isEqualTo("https://deepple-test.s3.ap-northeast-2.amazonaws.com/images/1/b.webp");
    Assertions.assertThat(profileImageOrder2.isPrimary()).isEqualTo(profileImage2.isPrimary());
    Assertions.assertThat(profileImageOrder2.order()).isEqualTo(profileImage2.getOrder());
  }
}
