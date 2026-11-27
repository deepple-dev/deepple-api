package kr.co.deepple.conversation.adapter.in.web;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;
import kr.co.deepple.auth.application.port.out.TokenProvider;
import kr.co.deepple.block.application.port.out.BlockRepository;
import kr.co.deepple.block.fixture.BlockFixture;
import kr.co.deepple.common.enums.Role;
import kr.co.deepple.common.support.IntegrationTestSupport;
import kr.co.deepple.community.application.port.out.ProfileExchangeRepository;
import kr.co.deepple.community.domain.ProfileExchangeStatus;
import kr.co.deepple.community.fixture.ProfileExchangeFixture;
import kr.co.deepple.match.application.port.out.MatchRepository;
import kr.co.deepple.match.domain.MatchContactType;
import kr.co.deepple.match.domain.MatchStatus;
import kr.co.deepple.match.fixture.MatchFixture;
import kr.co.deepple.member.application.port.out.MemberRepository;
import kr.co.deepple.member.domain.ActivityStatus;
import kr.co.deepple.member.domain.Gender;
import kr.co.deepple.member.domain.Member;
import kr.co.deepple.member.fixture.MemberFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

class ConversationControllerIntegrationTest extends IntegrationTestSupport {

  @Autowired private MockMvcTester mockMvc;

  @Autowired private TokenProvider tokenProvider;

  @Autowired private MemberRepository memberRepository;

  @Autowired private MatchRepository matchRepository;

  @Autowired private ProfileExchangeRepository profileExchangeRepository;

  @Autowired private BlockRepository blockRepository;

  private Member me;
  private Member sender;
  private Member exchanger;
  private Member matched;

  @BeforeEach
  void setUp() {
    me = memberRepository.save(MemberFixture.builder().gender(Gender.MALE).nickname("나").build());
    sender =
        memberRepository.save(
            MemberFixture.builder()
                .gender(Gender.FEMALE)
                .activityStatus(ActivityStatus.ACTIVE)
                .nickname("보낸이")
                .build());
    exchanger =
        memberRepository.save(MemberFixture.builder().gender(Gender.FEMALE).nickname("교환").build());
    matched =
        memberRepository.save(
            MemberFixture.builder()
                .gender(Gender.FEMALE)
                .nickname("매칭")
                .kakaoId("matchedKakao")
                .build());
  }

  @Test
  @DisplayName("대화 목록은 상대 한 명당 한 줄로, 마지막 활동이 최근인 순서이며 차단한 상대는 뺀다.")
  void listConversationsByRecentActivity() {
    matchRepository.save(
        MatchFixture.builder()
            .requesterId(sender.getId())
            .responderId(me.getId())
            .requestMessage("처음 뵙겠습니다")
            .build());
    profileExchangeRepository.save(
        ProfileExchangeFixture.builder()
            .requesterId(exchanger.getId())
            .responderId(me.getId())
            .status(ProfileExchangeStatus.WAITING)
            .build());
    matchRepository.save(
        MatchFixture.builder()
            .requesterId(matched.getId())
            .responderId(me.getId())
            .requestMessage("안녕하세요")
            .responseMessage("반가워요")
            .status(MatchStatus.MATCHED)
            .requesterContactType(MatchContactType.KAKAO)
            .responderContactType(MatchContactType.PHONE_NUMBER)
            .build());
    Member blocked = memberRepository.save(MemberFixture.builder().gender(Gender.FEMALE).build());
    matchRepository.save(
        MatchFixture.builder().requesterId(blocked.getId()).responderId(me.getId()).build());
    blockRepository.save(
        BlockFixture.builder().blockerId(me.getId()).blockedId(blocked.getId()).build());

    MvcTestResult result = get("/conversations");

    assertThat(result).hasStatusOk();
    assertThat(result)
        .bodyJson()
        .extractingPath("$.conversations[*].memberId")
        .isEqualTo(
            List.of(
                Math.toIntExact(matched.getId()),
                Math.toIntExact(exchanger.getId()),
                Math.toIntExact(sender.getId())));
    assertThat(result)
        .bodyJson()
        .extractingPath("$.conversations[2].needsMyAction")
        .isEqualTo(true);
    assertThat(result)
        .bodyJson()
        .extractingPath("$.conversations[2].counterpartMessage")
        .isEqualTo("처음 뵙겠습니다");
    assertThat(result)
        .bodyJson()
        .extractingPath("$.conversations[1].lastEvent.type")
        .isEqualTo("PROFILE_EXCHANGE_REQUESTED");
    assertThat(result)
        .bodyJson()
        .extractingPath("$.conversations[0].lastEvent.type")
        .isEqualTo("MESSAGE_ACCEPTED");
    assertThat(result).bodyJson().extractingPath("$.hasMore").isEqualTo(false);
  }

  @Test
  @DisplayName("메시지 상세는 타임라인과 진행 중인 매칭, 매칭됐을 때 상대 연락처를 준다.")
  void getConversationDetail() {
    matchRepository.save(
        MatchFixture.builder()
            .requesterId(matched.getId())
            .responderId(me.getId())
            .requestMessage("안녕하세요")
            .responseMessage("반가워요")
            .status(MatchStatus.MATCHED)
            .requesterContactType(MatchContactType.KAKAO)
            .responderContactType(MatchContactType.PHONE_NUMBER)
            .build());

    MvcTestResult result = get("/conversations/" + matched.getId());

    assertThat(result).hasStatusOk();
    assertThat(result).bodyJson().extractingPath("$.nickname").isEqualTo("매칭");
    assertThat(result)
        .bodyJson()
        .extractingPath("$.events[*].type")
        .isEqualTo(List.of("MESSAGE_SENT", "MESSAGE_ACCEPTED"));
    assertThat(result).bodyJson().extractingPath("$.match.status").isEqualTo("MATCHED");
    assertThat(result).bodyJson().extractingPath("$.contact.contactType").isEqualTo("KAKAO");
    assertThat(result).bodyJson().extractingPath("$.contact.contact").isEqualTo("matchedKakao");
  }

  @Test
  @DisplayName("매칭된 상대가 활동 중이 아니면 연락처를 내려 주지 않는다.")
  void hideContactWhenCounterpartInactive() {
    Member dormant =
        memberRepository.save(
            MemberFixture.builder()
                .gender(Gender.FEMALE)
                .activityStatus(ActivityStatus.DORMANT)
                .kakaoId("dormantKakao")
                .build());
    matchRepository.save(
        MatchFixture.builder()
            .requesterId(dormant.getId())
            .responderId(me.getId())
            .responseMessage("반가워요")
            .status(MatchStatus.MATCHED)
            .requesterContactType(MatchContactType.KAKAO)
            .responderContactType(MatchContactType.PHONE_NUMBER)
            .build());

    MvcTestResult result = get("/conversations/" + dormant.getId());

    assertThat(result).hasStatusOk();
    assertThat(result).bodyJson().extractingPath("$.contact").isNull();
    assertThat(result).bodyJson().extractingPath("$.isCounterpartActive").isEqualTo(false);
  }

  @Test
  @DisplayName("주고받은 것이 없는 상대의 메시지 상세는 404를 응답한다.")
  void notFoundWithoutRelation() {
    MvcTestResult result = get("/conversations/" + sender.getId());

    assertThat(result).hasStatus(HttpStatus.NOT_FOUND);
    assertThat(result).bodyJson().extractingPath("$.code").isEqualTo("CONVERSATION_NOT_FOUND");
  }

  private MvcTestResult get(String uri) {
    return mockMvc
        .get()
        .uri(uri)
        .header(
            HttpHeaders.AUTHORIZATION,
            "Bearer " + tokenProvider.createAccessToken(me.getId(), Role.MEMBER, Instant.now()))
        .exchange();
  }
}
