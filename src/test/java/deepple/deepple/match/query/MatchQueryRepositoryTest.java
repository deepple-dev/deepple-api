package deepple.deepple.match.query;

import deepple.deepple.common.MockEventsExtension;
import deepple.deepple.common.config.QueryDslConfig;
import deepple.deepple.match.command.domain.match.Match;
import deepple.deepple.match.command.domain.match.MatchContactType;
import deepple.deepple.match.command.domain.match.MatchType;
import deepple.deepple.match.command.domain.match.vo.Message;
import deepple.deepple.member.command.domain.member.District;
import deepple.deepple.member.command.domain.member.Member;
import deepple.deepple.member.command.domain.member.vo.MemberProfile;
import deepple.deepple.member.command.domain.member.vo.Nickname;
import deepple.deepple.member.command.domain.member.vo.Region;
import deepple.deepple.member.command.domain.profileImage.ProfileImage;
import deepple.deepple.member.command.domain.profileImage.vo.ImageUrl;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

import java.util.ArrayList;
import java.util.List;

@DataJpaTest
@Import({QueryDslConfig.class, MatchQueryRepository.class})
@ExtendWith(MockEventsExtension.class)
public class MatchQueryRepositoryTest {
    @Autowired
    private MatchQueryRepository matchQueryRepository;

    @Autowired
    private TestEntityManager em;

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
            Match match = Match
                .request(targetMember.getId(), responder.getId(), Message.from("Hello"), "알림용이름", MatchType.MATCH,
                    MatchContactType.PHONE_NUMBER);

            if (i % 2 == 0) {
                match.approve(Message.from("Hi"), "알림용이름", MatchContactType.PHONE_NUMBER);
            }
            em.persist(match);
        }

        // When
        List<MatchView> matchViews = matchQueryRepository.findSentMatches(targetMember.getId(), null);

        // Then
        Assertions.assertThat(matchViews.size()).isEqualTo(9);

        for (int i = 0; i < matchViews.size(); i++) {
            MatchView matchView = matchViews.get(i);
            Long opponentId = matchView.opponentId();
            Member member = members.stream().filter(m -> m.getId().equals(opponentId)).findFirst().orElse(null);
            ProfileImage profileImage = memberProfileImages.stream()
                .filter(p -> p.getMemberId().equals(opponentId))
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
            Match match = Match
                .request(requester.getId(), targetMember.getId(), Message.from("Hello"), "알림용이름", MatchType.MATCH,
                    MatchContactType.PHONE_NUMBER);

            if (i % 2 == 0) {
                match.approve(Message.from("Hi"), "알림용이름", MatchContactType.PHONE_NUMBER);
            }
            em.persist(match);
        }

        // When
        List<MatchView> matchViews = matchQueryRepository.findReceiveMatches(targetMember.getId(), null);

        // Then
        Assertions.assertThat(matchViews.size()).isEqualTo(9);

        for (int i = 0; i < matchViews.size(); i++) {
            MatchView matchView = matchViews.get(i);
            Long opponentId = matchView.opponentId();
            Member member = members.stream().filter(m -> m.getId().equals(opponentId)).findFirst().orElse(null);
            ProfileImage profileImage = memberProfileImages.stream()
                .filter(p -> p.getMemberId().equals(opponentId))
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

        Match readMatch = Match.request(requester.getId(), readResponder.getId(), Message.from("Hello"),
            "알림용이름", MatchType.MATCH, MatchContactType.PHONE_NUMBER);
        readMatch.read(readResponder.getId());
        em.persist(readMatch);

        Match unreadMatch = Match.request(requester.getId(), unreadResponder.getId(), Message.from("Hello"),
            "알림용이름", MatchType.MATCH, MatchContactType.PHONE_NUMBER);
        em.persist(unreadMatch);

        // when
        List<MatchView> matchViews = matchQueryRepository.findSentMatches(requester.getId(), null);

        // then
        MatchView readView = matchViews.stream()
            .filter(v -> v.opponentId() == readResponder.getId()).findFirst().orElseThrow();
        MatchView unreadView = matchViews.stream()
            .filter(v -> v.opponentId() == unreadResponder.getId()).findFirst().orElseThrow();
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
        var member = Member.fromPhoneNumber(phone);
        em.persist(member);
        var profile = MemberProfile.builder()
            .nickname(Nickname.from(nickname))
            .region(Region.of(district))
            .yearOfBirth(1995)
            .build();
        member.updateProfile(profile);
        return member;
    }

    private ProfileImage createProfileImage(long memberId, String url) {
        return ProfileImage.builder()
            .memberId(memberId)
            .imageUrl(ImageUrl.from(url))
            .isPrimary(true)
            .order(1)
            .build();
    }
}
