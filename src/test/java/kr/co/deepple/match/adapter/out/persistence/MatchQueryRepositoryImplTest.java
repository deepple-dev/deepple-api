package kr.co.deepple.match.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import kr.co.deepple.common.support.RepositoryTestSupport;
import kr.co.deepple.match.application.dto.RawMatchView;
import kr.co.deepple.match.domain.Match;
import kr.co.deepple.match.domain.MatchContactType;
import kr.co.deepple.match.domain.MatchStatus;
import kr.co.deepple.match.fixture.MatchFixture;
import kr.co.deepple.member.domain.District;
import kr.co.deepple.member.domain.Member;
import kr.co.deepple.member.domain.ProfileImage;
import kr.co.deepple.member.fixture.MemberFixture;
import kr.co.deepple.member.fixture.ProfileImageFixture;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

public class MatchQueryRepositoryImplTest extends RepositoryTestSupport {

  @Autowired private MatchQueryRepositoryImpl matchQueryRepository;

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
    List<RawMatchView> matchViews =
        matchQueryRepository.findSentMatches(targetMember.getId(), null, Set.of());

    // Then
    Assertions.assertThat(matchViews.size()).isEqualTo(9);

    for (int i = 0; i < matchViews.size(); i++) {
      RawMatchView matchView = matchViews.get(i);
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
    List<RawMatchView> matchViews =
        matchQueryRepository.findReceiveMatches(targetMember.getId(), null, Set.of());

    // Then
    Assertions.assertThat(matchViews.size()).isEqualTo(9);

    for (int i = 0; i < matchViews.size(); i++) {
      RawMatchView matchView = matchViews.get(i);
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
    List<RawMatchView> matchViews =
        matchQueryRepository.findSentMatches(requester.getId(), null, Set.of());

    // then
    RawMatchView readView =
        matchViews.stream()
            .filter(v -> v.opponentId() == readResponder.getId())
            .findFirst()
            .orElseThrow();
    RawMatchView unreadView =
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

  @Test
  @DisplayName("차단한 회원과 주고받은 메세지는 보낸·받은 목록에서 빠지고, 내 메세지와 상대 메세지가 각자의 자리에 들어간다.")
  void excludeBlockedOpponentsAndMapMessages() {
    // given
    Member me = em.persist(MemberFixture.builder().build());
    Member opponent = em.persist(MemberFixture.builder().build());
    Member blocked = em.persist(MemberFixture.builder().build());
    Member blockedRequester = em.persist(MemberFixture.builder().build());

    em.persist(
        MatchFixture.builder()
            .requesterId(me.getId())
            .responderId(opponent.getId())
            .requestMessage("내가 보낸 요청")
            .responseMessage("상대의 응답")
            .build());
    em.persist(
        MatchFixture.builder()
            .requesterId(me.getId())
            .responderId(blocked.getId())
            .requestMessage("차단한 회원에게 보낸 요청")
            .responseMessage("차단한 회원의 응답")
            .build());
    em.persist(
        MatchFixture.builder()
            .requesterId(blockedRequester.getId())
            .responderId(me.getId())
            .requestMessage("차단한 회원이 보낸 요청")
            .responseMessage("내가 한 응답")
            .build());
    Set<Long> blockedIds = Set.of(blocked.getId(), blockedRequester.getId());

    // when
    List<RawMatchView> sent = matchQueryRepository.findSentMatches(me.getId(), null, blockedIds);
    List<RawMatchView> received =
        matchQueryRepository.findReceiveMatches(me.getId(), null, blockedIds);

    // then
    assertThat(sent).extracting(RawMatchView::opponentId).containsExactly(opponent.getId());
    assertThat(sent.getFirst().myMessage()).isEqualTo("내가 보낸 요청");
    assertThat(sent.getFirst().opponentMessage()).isEqualTo("상대의 응답");
    assertThat(received).extracting(RawMatchView::opponentId).containsExactly(opponent.getId());
    assertThat(received.getFirst().myMessage()).isEqualTo("내가 보낸 요청");
    assertThat(received.getFirst().opponentMessage()).isEqualTo("상대의 응답");
  }
}
