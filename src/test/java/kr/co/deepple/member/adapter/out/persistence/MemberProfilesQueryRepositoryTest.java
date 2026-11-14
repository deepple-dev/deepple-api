package kr.co.deepple.member.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Set;
import kr.co.deepple.common.support.RepositoryTestSupport;
import kr.co.deepple.member.application.port.in.MemberProfileSummary;
import kr.co.deepple.member.domain.AgeConverter;
import kr.co.deepple.member.domain.District;
import kr.co.deepple.member.domain.Gender;
import kr.co.deepple.member.domain.Hobby;
import kr.co.deepple.member.domain.Mbti;
import kr.co.deepple.member.domain.Member;
import kr.co.deepple.member.domain.Religion;
import kr.co.deepple.member.fixture.MemberFixture;
import kr.co.deepple.member.fixture.ProfileImageFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

class MemberProfilesQueryRepositoryTest extends RepositoryTestSupport {

  @Autowired private MemberQueryRepositoryImpl memberQueryRepository;

  @Autowired private TestEntityManager entityManager;

  @Test
  @DisplayName("닉네임·대표 사진·나이·성별·지역·MBTI·종교·취미를 회원 번호별로 조회하고, 다른 회원은 포함하지 않는다.")
  void findProfilesOfMembers() {
    Member withAll =
        entityManager.persist(
            MemberFixture.builder()
                .nickname("프로필있음")
                .gender(Gender.FEMALE)
                .yearOfBirth(1995)
                .district(District.GANGBUK_GU)
                .mbti(Mbti.ENFJ)
                .religion(Religion.NONE)
                .hobbies(Set.of(Hobby.ANIMATION, Hobby.BOARD_GAMES))
                .build());
    Member plain = entityManager.persist(MemberFixture.builder().hobbies(Set.of()).build());
    Member other = entityManager.persist(MemberFixture.builder().build());
    entityManager.persist(
        ProfileImageFixture.builder()
            .memberId(withAll.getId())
            .imageUrl("https://example.com/primary.jpg")
            .primary(true)
            .build());
    entityManager.persist(
        ProfileImageFixture.builder()
            .memberId(withAll.getId())
            .imageUrl("https://example.com/second.jpg")
            .order(2)
            .primary(false)
            .build());
    entityManager.flush();
    entityManager.clear();

    List<MemberProfileSummary> profiles =
        memberQueryRepository.findProfilesByMemberIds(List.of(withAll.getId(), plain.getId()));

    assertThat(profiles)
        .extracting(MemberProfileSummary::memberId)
        .containsExactlyInAnyOrder(withAll.getId(), plain.getId())
        .doesNotContain(other.getId());
    MemberProfileSummary profile =
        profiles.stream().filter(p -> p.memberId() == withAll.getId()).findFirst().orElseThrow();
    assertThat(profile.nickname()).isEqualTo("프로필있음");
    assertThat(profile.profileImageUrl()).isEqualTo("https://example.com/primary.jpg");
    assertThat(profile.age()).isEqualTo(AgeConverter.toAge(1995));
    assertThat(profile.gender()).isEqualTo("FEMALE");
    assertThat(profile.city()).isEqualTo(District.GANGBUK_GU.getCity().name());
    assertThat(profile.district()).isEqualTo("GANGBUK_GU");
    assertThat(profile.mbti()).isEqualTo("ENFJ");
    assertThat(profile.religion()).isEqualTo("NONE");
    assertThat(profile.hobbies()).containsExactlyInAnyOrder("ANIMATION", "BOARD_GAMES");
    MemberProfileSummary plainProfile =
        profiles.stream().filter(p -> p.memberId() == plain.getId()).findFirst().orElseThrow();
    assertThat(plainProfile.profileImageUrl()).isNull();
    assertThat(plainProfile.hobbies()).isEmpty();
  }

  @Test
  @DisplayName("대표 사진이 여러 장이어도 회원마다 프로필을 하나만 돌려준다.")
  void findOneProfileWhenPrimaryImagesDuplicated() {
    Member member =
        entityManager.persist(
            MemberFixture.builder().hobbies(Set.of(Hobby.ANIMATION, Hobby.BOARD_GAMES)).build());
    entityManager.persist(
        ProfileImageFixture.builder()
            .memberId(member.getId())
            .imageUrl("https://example.com/first.jpg")
            .order(1)
            .primary(true)
            .build());
    entityManager.persist(
        ProfileImageFixture.builder()
            .memberId(member.getId())
            .imageUrl("https://example.com/second.jpg")
            .order(2)
            .primary(true)
            .build());
    entityManager.flush();
    entityManager.clear();

    List<MemberProfileSummary> profiles =
        memberQueryRepository.findProfilesByMemberIds(List.of(member.getId()));

    assertThat(profiles).hasSize(1);
    assertThat(profiles.getFirst().profileImageUrl())
        .isIn("https://example.com/first.jpg", "https://example.com/second.jpg");
    assertThat(profiles.getFirst().hobbies()).containsExactlyInAnyOrder("ANIMATION", "BOARD_GAMES");
  }
}
