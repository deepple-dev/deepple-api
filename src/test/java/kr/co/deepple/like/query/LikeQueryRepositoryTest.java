package kr.co.deepple.like.query;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import kr.co.deepple.block.domain.Block;
import kr.co.deepple.block.fixture.BlockFixture;
import kr.co.deepple.common.support.RepositoryTestSupport;
import kr.co.deepple.like.command.domain.Like;
import kr.co.deepple.like.command.domain.LikeLevel;
import kr.co.deepple.like.fixture.LikeFixture;
import kr.co.deepple.member.domain.District;
import kr.co.deepple.member.domain.Member;
import kr.co.deepple.member.domain.ProfileImage;
import kr.co.deepple.member.fixture.MemberFixture;
import kr.co.deepple.member.fixture.ProfileImageFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

class LikeQueryRepositoryTest extends RepositoryTestSupport {

  private static final int NUMBER_OF_PEOPLE = 30;
  private static final int PAGE_SIZE = 13;

  @Autowired private LikeQueryRepository likeQueryRepository;

  @Autowired private TestEntityManager em;

  private List<Member> senders;
  private List<ProfileImage> senderProfileImages;
  private List<Member> receivers;
  private List<ProfileImage> receiverProfileImages;
  private List<Like> likes;

  @BeforeEach
  void setUp() {
    createSenders();
    createSenderProfileImages();

    createReceivers();
    createReceiverProfileImages();

    createSenderToReceiverLikes();
    createReceiverToSenderLikes();

    em.flush();
    em.clear();

    likes.sort(Comparator.comparing(Like::getId).reversed());
  }

  @Test
  @DisplayName("보낸 좋아요 목록을 조회한다.")
  void findSentLikes() {
    // given
    var senderId = senders.getFirst().getId();
    var expectedLikes =
        likes.stream()
            .filter(like -> like.getSenderId() == senderId)
            .sorted(Comparator.comparing(Like::getId).reversed())
            .limit(PAGE_SIZE)
            .toList();

    var profileImageUrlMap =
        receiverProfileImages.stream()
            .collect(Collectors.toMap(ProfileImage::getMemberId, ProfileImage::getUrl));
    var receiverProfileMap =
        receivers.stream().collect(Collectors.toMap(Member::getId, Member::getProfile));

    // when
    var firstPage = likeQueryRepository.findSentLikes(senderId, null);

    // then
    assertThat(firstPage).hasSize(expectedLikes.size());

    for (int i = 0; i < expectedLikes.size(); i++) {
      var likeView = firstPage.get(i);
      var expectedLike = expectedLikes.get(i);

      assertThat(likeView.likeId()).isEqualTo(expectedLike.getId());
      assertThat(likeView.opponentId()).isEqualTo(expectedLike.getReceiverId());

      var expectedUrl = profileImageUrlMap.get(expectedLike.getReceiverId());
      assertThat(likeView.profileImageUrl()).isEqualTo(expectedUrl);

      var receiverProfile = receiverProfileMap.get(expectedLike.getReceiverId());
      assertThat(likeView.nickname()).isEqualTo(receiverProfile.getNickname().getValue());
      assertThat(likeView.city()).isEqualTo(receiverProfile.getRegion().getCity().toString());
      assertThat(likeView.yearOfBirth()).isEqualTo(receiverProfile.getYearOfBirth().getValue());

      boolean expectedMutual = expectedLike.getSenderId() % 2 != 0;
      assertThat(likeView.isMutualLike()).isEqualTo(expectedMutual);
    }
  }

  @Test
  @DisplayName("받은 좋아요 목록을 조회한다.")
  void findReceivedLikes() {
    // given
    var receiverId = receivers.getFirst().getId();
    var expectedLikes =
        likes.stream()
            .filter(like -> like.getReceiverId() == receiverId)
            .sorted(Comparator.comparing(Like::getId).reversed())
            .limit(PAGE_SIZE)
            .toList();

    var profileImageUrlMap =
        senderProfileImages.stream()
            .collect(Collectors.toMap(ProfileImage::getMemberId, ProfileImage::getUrl));
    var senderProfileMap =
        senders.stream().collect(Collectors.toMap(Member::getId, Member::getProfile));

    // when
    var firstPage = likeQueryRepository.findReceivedLikes(receiverId, null);

    // then
    assertThat(firstPage).hasSize(expectedLikes.size());

    for (int i = 0; i < expectedLikes.size(); i++) {
      var likeView = firstPage.get(i);
      var expectedLike = expectedLikes.get(i);

      assertThat(likeView.likeId()).isEqualTo(expectedLike.getId());
      assertThat(likeView.opponentId()).isEqualTo(expectedLike.getSenderId());

      var expectedUrl = profileImageUrlMap.get(expectedLike.getSenderId());
      assertThat(likeView.profileImageUrl()).isEqualTo(expectedUrl);

      var senderProfile = senderProfileMap.get(expectedLike.getSenderId());
      assertThat(likeView.nickname()).isEqualTo(senderProfile.getNickname().getValue());
      assertThat(likeView.city()).isEqualTo(senderProfile.getRegion().getCity().toString());
      assertThat(likeView.yearOfBirth()).isEqualTo(senderProfile.getYearOfBirth().getValue());

      boolean expectedMutual = expectedLike.getSenderId() % 2 != 0;
      assertThat(likeView.isMutualLike()).isEqualTo(expectedMutual);
    }
  }

  @Test
  @DisplayName("내가 차단한 멤버는 제외하고 보낸 좋아요 목록을 조회한다.")
  void findSentLikes_excludeBlockedMembers() {
    // given
    var senderId = senders.getFirst().getId();
    var blockedMemberId = receivers.get(0).getId();
    var blockerMemberId = receivers.get(1).getId();

    // 멤버 차단
    createBlock(senderId, blockedMemberId);
    createBlock(blockerMemberId, senderId);

    var expectedLikes =
        likes.stream()
            .filter(like -> like.getSenderId() == senderId)
            .filter(like -> like.getReceiverId() != blockedMemberId)
            .sorted(Comparator.comparing(Like::getId).reversed())
            .limit(PAGE_SIZE)
            .toList();

    // when
    var firstPage = likeQueryRepository.findSentLikes(senderId, null);

    // then
    assertThat(firstPage).hasSize(expectedLikes.size());
    for (var likeView : firstPage) {
      assertThat(likeView.opponentId()).isNotEqualTo(blockedMemberId);
    }
  }

  @Test
  @DisplayName("내가 차단한 멤버는 제외하고 받은 좋아요 목록을 조회한다.")
  void findReceivedLikes_excludeBlockedMembers() {
    // given
    var receiverId = receivers.getFirst().getId();
    var blockedMemberId = senders.get(0).getId();
    var blockerMemberId = senders.get(1).getId();

    // 멤버 차단
    createBlock(receiverId, blockedMemberId);
    createBlock(blockerMemberId, receiverId);

    var expectedLikes =
        likes.stream()
            .filter(like -> like.getReceiverId() == receiverId)
            .filter(like -> like.getSenderId() != blockedMemberId)
            .sorted(Comparator.comparing(Like::getId).reversed())
            .limit(PAGE_SIZE)
            .toList();

    // when
    var firstPage = likeQueryRepository.findReceivedLikes(receiverId, null);

    // then
    assertThat(firstPage).hasSize(expectedLikes.size());
    for (var likeView : firstPage) {
      assertThat(likeView.opponentId()).isNotEqualTo(blockedMemberId);
    }
  }

  @Test
  @DisplayName("보낸 좋아요 목록 페이지네이션이 데이터 누락 없이 동작한다.")
  void findSentLikesPaginationWithoutDataLoss() {
    // given
    var senderId = senders.getFirst().getId();
    var allLikes =
        likes.stream()
            .filter(like -> like.getSenderId() == senderId)
            .sorted(Comparator.comparing(Like::getId).reversed())
            .toList();

    var fetchedLikes = new ArrayList<RawLikeView>();

    // when
    Long lastLikeId = null;
    while (true) {
      var page = likeQueryRepository.findSentLikes(senderId, lastLikeId);
      if (page.isEmpty()) {
        break;
      }
      fetchedLikes.addAll(page);
      lastLikeId = page.getLast().likeId();
    }

    // then
    assertThat(fetchedLikes).hasSize(allLikes.size());

    for (int i = 0; i < allLikes.size(); i++) {
      assertThat(fetchedLikes.get(i).likeId()).isEqualTo(allLikes.get(i).getId());
    }
  }

  @Test
  @DisplayName("받은 좋아요 목록 페이지네이션이 데이터 누락 없이 동작한다.")
  void findReceivedLikesPaginationWithoutDataLoss() {
    // given
    var receiverId = receivers.getFirst().getId();
    var allLikes =
        likes.stream()
            .filter(like -> like.getReceiverId() == receiverId)
            .sorted(Comparator.comparing(Like::getId).reversed())
            .toList();

    var fetchedLikes = new ArrayList<RawLikeView>();

    // when
    Long lastLikeId = null;
    while (true) {
      var page = likeQueryRepository.findReceivedLikes(receiverId, lastLikeId);
      if (page.isEmpty()) {
        break;
      }
      fetchedLikes.addAll(page);
      lastLikeId = page.getLast().likeId();
    }

    // then
    assertThat(fetchedLikes).hasSize(allLikes.size());

    for (int i = 0; i < allLikes.size(); i++) {
      assertThat(fetchedLikes.get(i).likeId()).isEqualTo(allLikes.get(i).getId());
    }
  }

  private void createSenders() {
    senders = new ArrayList<>();
    for (int i = 0; i < NUMBER_OF_PEOPLE; i++) {
      var sender = createMember(String.format("010%04d1234", i), "sender" + i, District.GWANAK_GU);
      senders.add(sender);
    }
  }

  private void createSenderProfileImages() {
    senderProfileImages = new ArrayList<>();
    for (int i = 0; i < NUMBER_OF_PEOPLE; i++) {
      if (i % 2 == 0) {
        continue;
      }
      var senderImage = createProfileImage(senders.get(i).getId(), "/sender" + i + ".jpg");
      senderProfileImages.add(senderImage);
      em.persist(senderImage);
    }
  }

  private void createReceivers() {
    receivers = new ArrayList<>();
    for (int i = 0; i < NUMBER_OF_PEOPLE; i++) {
      var receiver =
          createMember(String.format("0101234%04d", i), "receiver" + i, District.GANGNAM_GU);
      receivers.add(receiver);
      em.persist(receiver);
    }
  }

  private void createReceiverProfileImages() {
    receiverProfileImages = new ArrayList<>();
    for (int i = 0; i < NUMBER_OF_PEOPLE; i++) {
      if (i % 2 == 1) {
        continue;
      }
      var receiverImage = createProfileImage(receivers.get(i).getId(), "/receiver" + i + ".jpg");
      receiverProfileImages.add(receiverImage);
      em.persist(receiverImage);
    }
  }

  private void createSenderToReceiverLikes() {
    likes = new ArrayList<>();
    for (Member sender : senders) {
      for (Member receiver : receivers) {
        var like =
            LikeFixture.builder()
                .senderId(sender.getId())
                .receiverId(receiver.getId())
                .level(LikeLevel.INTERESTED)
                .build();
        likes.add(like);
        em.persist(like);
      }
    }
  }

  private void createReceiverToSenderLikes() {
    for (Member receiver : receivers) {
      for (Member sender : senders) {
        if (sender.getId() % 2 == 0) {
          continue;
        }
        var like =
            LikeFixture.builder()
                .senderId(receiver.getId())
                .receiverId(sender.getId())
                .level(LikeLevel.HIGHLY_INTERESTED)
                .build();
        likes.add(like);
        em.persist(like);
      }
    }
  }

  private Member createMember(String phone, String nickname, District district) {
    var member =
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
    return ProfileImageFixture.builder()
        .memberId(memberId)
        .imageUrl(url)
        .primary(true)
        .order(1)
        .build();
  }

  private void createBlock(long blockerId, long blockedMemberId) {
    Block block = BlockFixture.builder().blockerId(blockerId).blockedId(blockedMemberId).build();
    em.persist(block);
    em.flush();
  }
}
