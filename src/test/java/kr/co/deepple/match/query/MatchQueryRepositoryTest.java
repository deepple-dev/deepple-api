package kr.co.deepple.match.query;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import kr.co.deepple.common.support.RepositoryTestSupport;
import kr.co.deepple.match.command.domain.match.Match;
import kr.co.deepple.match.command.domain.match.MatchContactType;
import kr.co.deepple.match.command.domain.match.MatchStatus;
import kr.co.deepple.match.fixture.MatchFixture;
import kr.co.deepple.member.command.domain.member.District;
import kr.co.deepple.member.command.domain.member.Member;
import kr.co.deepple.member.command.domain.profileImage.ProfileImage;
import kr.co.deepple.member.fixture.MemberFixture;
import kr.co.deepple.member.fixture.ProfileImageFixture;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

public class MatchQueryRepositoryTest extends RepositoryTestSupport {

  @Autowired private MatchQueryRepository matchQueryRepository;

  @Autowired private TestEntityManager em;

  private List<Member> members;
  private List<ProfileImage> memberProfileImages;

  @BeforeEach
  void setUp() {
    createMembers();
    createProfileImages();
  }

  @Test
  @DisplayName("보낸 매칭 메세지를 조회한다.")
  void findSentMatches() {
    // given
    Member targetMember = members.get(0);

    for (int i = 0; i < 10; i++) {
      Member responder = members.get(i);
      if (responder.getId().equals(targetMember.getId())) {
        continue;
      }
      MatchFixture matchFixture =
          MatchFixture.builder()
              .requesterId(targetMember.getId())
              .responderId(responder.getId())
              .requestMessage("Hello");

      if (i % 2 == 0) {
        matchFixture
            .status(MatchStatus.MATCHED)
            .responseMessage("Hi")
            .responderContactType(MatchContactType.PHONE_NUMBER);
      }
      em.persist(matchFixture.build());
    }

    // When
    List<MatchView> matchViews = matchQueryRepository.findSentMatches(targetMember.getId(), null);

    // Then
    Assertions.assertThat(matchViews.size()).isEqualTo(9);

    for (int i = 0; i < matchViews.size(); i++) {
      MatchView matchView = matchViews.get(i);
      Long opponentId = matchView.opponentId();
      Member member =
          members.stream().filter(m -> m.getId().equals(opponentId)).findFirst().orElse(null);
      ProfileImage profileImage =
          memberProfileImages.stream()
              .filter(p -> p.getMemberId() == opponentId)
              .findFirst()
              .orElse(null);
      Assertions.assertThat(member).isNotNull();
      Assertions.assertThat(profileImage).isNotNull();
      Assertions.assertThat(matchView.profileImageUrl()).isEqualTo(profileImage.getUrl());
    }
  }

  @Test
  @DisplayName("받은 매칭 메세지를 조회한다.")
  void findReceiveMatches() {
    // given
    Member targetMember = members.get(0);

    for (int i = 0; i < 10; i++) {
      Member requester = members.get(i);
      if (requester.getId().equals(targetMember.getId())) {
        continue;
      }
      MatchFixture matchFixture =
          MatchFixture.builder()
              .requesterId(requester.getId())
              .responderId(targetMember.getId())
              .requestMessage("Hello");

      if (i % 2 == 0) {
        matchFixture
            .status(MatchStatus.MATCHED)
            .responseMessage("Hi")
            .responderContactType(MatchContactType.PHONE_NUMBER);
      }
      em.persist(matchFixture.build());
    }

    // When
    List<MatchView> matchViews =
        matchQueryRepository.findReceiveMatches(targetMember.getId(), null);

    // Then
    Assertions.assertThat(matchViews.size()).isEqualTo(9);

    for (int i = 0; i < matchViews.size(); i++) {
      MatchView matchView = matchViews.get(i);
      Long opponentId = matchView.opponentId();
      Member member =
          members.stream().filter(m -> m.getId().equals(opponentId)).findFirst().orElse(null);
      ProfileImage profileImage =
          memberProfileImages.stream()
              .filter(p -> p.getMemberId() == opponentId)
              .findFirst()
              .orElse(null);
      Assertions.assertThat(member).isNotNull();
      Assertions.assertThat(profileImage).isNotNull();
      Assertions.assertThat(matchView.profileImageUrl()).isEqualTo(profileImage.getUrl());
    }
  }

  @Test
  @DisplayName("보낸 매칭 메세지 조회 시, 응답자가 읽은 요청은 readAt이 채워지고 안 읽은 요청은 null이다.")
  void findSentMatchesWithReadAt() {
    // given
    Member requester = members.get(0);
    Member readResponder = members.get(1);
    Member unreadResponder = members.get(2);

    Match readMatch =
        MatchFixture.builder()
            .requesterId(requester.getId())
            .responderId(readResponder.getId())
            .requestMessage("Hello")
            .readByResponderAt(LocalDateTime.now())
            .build();
    em.persist(readMatch);

    Match unreadMatch =
        MatchFixture.builder()
            .requesterId(requester.getId())
            .responderId(unreadResponder.getId())
            .requestMessage("Hello")
            .build();
    em.persist(unreadMatch);

    // when
    List<MatchView> matchViews = matchQueryRepository.findSentMatches(requester.getId(), null);

    // then
    MatchView readView =
        matchViews.stream()
            .filter(v -> v.opponentId() == readResponder.getId())
            .findFirst()
            .orElseThrow();
    MatchView unreadView =
        matchViews.stream()
            .filter(v -> v.opponentId() == unreadResponder.getId())
            .findFirst()
            .orElseThrow();
    Assertions.assertThat(readView.readAt()).isNotNull();
    Assertions.assertThat(unreadView.readAt()).isNull();
  }

  private void createMembers() {
    members = new ArrayList<>();
    for (int i = 0; i < 10; i++) {
      Member member = createMember("0101234567" + i, "member" + i, District.ANDONG_SI);
      members.add(member);
    }
  }

  private void createProfileImages() {
    memberProfileImages = new ArrayList<>();
    for (int i = 0; i < 10; i++) {
      ProfileImage profileImage = createProfileImage(members.get(i).getId(), "profileImage" + i);
      memberProfileImages.add(profileImage);
      em.persist(profileImage);
    }
  }

  private Member createMember(String phone, String nickname, District district) {
    Member member =
        MemberFixture.builder()
            .phoneNumber(phone)
            .nickname(nickname)
            .district(district)
            .yearOfBirth(1995)
            .build();
    em.persist(member);
    return member;
  }

  private ProfileImage createProfileImage(long memberId, String url) {
    return ProfileImageFixture.builder().memberId(memberId).imageUrl(url).build();
  }
}
