package kr.co.deepple.like.command.application;

import kr.co.deepple.block.application.port.out.BlockRepository;
import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.like.command.domain.Like;
import kr.co.deepple.like.command.domain.LikeCommandRepository;
import kr.co.deepple.like.command.domain.LikeErrorCode;
import kr.co.deepple.like.presentation.LikeLevelRequest;
import kr.co.deepple.like.presentation.LikeSendRequest;
import kr.co.deepple.member.command.domain.member.ActivityStatus;
import kr.co.deepple.member.command.domain.member.Gender;
import kr.co.deepple.member.command.domain.member.MemberCommandRepository;
import kr.co.deepple.member.fixture.MemberFixture;
import kr.co.deepple.mission.command.application.memberMission.MemberMissionService;
import kr.co.deepple.mission.command.domain.mission.ActionType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LikeSendServiceTest {

    @Mock
    LikeCommandRepository likeCommandRepository;

    @Mock
    MemberCommandRepository memberCommandRepository;

    @Mock
    MemberMissionService memberMissionService;

    @Mock
    BlockRepository blockRepository;

    @InjectMocks
    LikeSendService likeSendService;

    @Test
    @DisplayName("좋아요를 성공적으로 보낸다.")
    void sendLikeSuccessfully() {
        // given
        var senderId = 1L;
        var receiverId = 2L;
        var likeLevel = LikeLevelRequest.INTERESTED;
        var request = new LikeSendRequest(receiverId, likeLevel);

        when(likeCommandRepository.existsBySenderIdAndReceiverId(senderId, receiverId)).thenReturn(false);

        var sender = MemberFixture.builder().id(senderId).nickname("nickname").gender(Gender.MALE).build();
        var receiver = MemberFixture.builder().id(receiverId).gender(Gender.FEMALE).build();

        when(memberCommandRepository.findById(senderId)).thenReturn(Optional.of(sender));
        when(memberCommandRepository.findById(receiverId)).thenReturn(Optional.of(receiver));

        when(blockRepository.existsByBlockerIdAndBlockedId(senderId, receiverId)).thenReturn(false);
        when(blockRepository.existsByBlockerIdAndBlockedId(receiverId, senderId)).thenReturn(false);

        boolean expectedMissionProcessed = true;
        when(memberMissionService.executeMissionsByAction(senderId, ActionType.LIKE.name()))
            .thenReturn(expectedMissionProcessed);

        // when
        boolean hasProcessedMission = likeSendService.send(senderId, request);

        // then
        verify(likeCommandRepository).save(any(Like.class));
        verify(memberMissionService).executeMissionsByAction(senderId, ActionType.LIKE.name());
        assertThat(hasProcessedMission).isEqualTo(expectedMissionProcessed);
    }

    @Test
    @DisplayName("이미 좋아요를 보낸 경우 예외가 발생한다.")
    void throwExceptionWhenLikeAlreadyExists() {
        // given
        var senderId = 1L;
        var receiverId = 2L;
        var likeLevel = LikeLevelRequest.INTERESTED;
        var request = new LikeSendRequest(receiverId, likeLevel);

        when(likeCommandRepository.existsBySenderIdAndReceiverId(senderId, receiverId)).thenReturn(true);

        // when & then
        assertThatThrownBy(() -> likeSendService.send(senderId, request))
            .isInstanceOf(DeeppleException.class)
            .hasFieldOrPropertyWithValue("errorCode", LikeErrorCode.LIKE_ALREADY_EXISTS);
    }

    @Test
    @DisplayName("상대가 비활성 상태인 경우 예외가 발생한다.")
    void throwExceptionWhenReceiverInactive() {
        // given
        var senderId = 1L;
        var receiverId = 2L;
        var likeLevel = LikeLevelRequest.INTERESTED;
        var request = new LikeSendRequest(receiverId, likeLevel);

        when(likeCommandRepository.existsBySenderIdAndReceiverId(senderId, receiverId)).thenReturn(false);

        var sender = MemberFixture.builder().id(senderId).build();
        var receiver = MemberFixture.builder().id(receiverId).activityStatus(ActivityStatus.DORMANT).build();

        when(memberCommandRepository.findById(senderId)).thenReturn(Optional.of(sender));
        when(memberCommandRepository.findById(receiverId)).thenReturn(Optional.of(receiver));

        // when && then
        assertThatThrownBy(() -> likeSendService.send(senderId, request))
            .isInstanceOf(DeeppleException.class)
            .hasFieldOrPropertyWithValue("errorCode", LikeErrorCode.LIKE_RECEIVER_INACTIVE);
    }

    @Test
    @DisplayName("상대와 같은 성별이면 예외가 발생한다.")
    void throwExceptionWhenSameGender() {
        // given
        var senderId = 1L;
        var receiverId = 2L;
        var likeLevel = LikeLevelRequest.INTERESTED;
        var request = new LikeSendRequest(receiverId, likeLevel);

        when(likeCommandRepository.existsBySenderIdAndReceiverId(senderId, receiverId)).thenReturn(false);

        var sender = MemberFixture.builder().id(senderId).gender(Gender.MALE).build();
        var receiver = MemberFixture.builder().id(receiverId).gender(Gender.MALE).build();

        when(memberCommandRepository.findById(senderId)).thenReturn(Optional.of(sender));
        when(memberCommandRepository.findById(receiverId)).thenReturn(Optional.of(receiver));

        // when && then
        assertThatThrownBy(() -> likeSendService.send(senderId, request))
            .isInstanceOf(DeeppleException.class)
            .hasFieldOrPropertyWithValue("errorCode", LikeErrorCode.LIKE_SAME_GENDER);
    }

    @Test
    @DisplayName("상대를 차단한 경우 예외가 발생한다.")
    void throwExceptionWhenSenderBlockedReceiver() {
        // given
        var senderId = 1L;
        var receiverId = 2L;
        var likeLevel = LikeLevelRequest.INTERESTED;
        var request = new LikeSendRequest(receiverId, likeLevel);

        when(likeCommandRepository.existsBySenderIdAndReceiverId(senderId, receiverId)).thenReturn(false);

        var sender = MemberFixture.builder().id(senderId).gender(Gender.MALE).build();
        var receiver = MemberFixture.builder().id(receiverId).gender(Gender.FEMALE).build();

        when(memberCommandRepository.findById(senderId)).thenReturn(Optional.of(sender));
        when(memberCommandRepository.findById(receiverId)).thenReturn(Optional.of(receiver));

        when(blockRepository.existsByBlockerIdAndBlockedId(senderId, receiverId)).thenReturn(true);

        // when && then
        assertThatThrownBy(() -> likeSendService.send(senderId, request))
            .isInstanceOf(DeeppleException.class)
            .hasFieldOrPropertyWithValue("errorCode", LikeErrorCode.LIKE_BLOCKED);
    }

    @Test
    @DisplayName("상대에게 차단당한 경우 예외가 발생한다.")
    void throwExceptionWhenReceiverBlockedSender() {
        // given
        var senderId = 1L;
        var receiverId = 2L;
        var likeLevel = LikeLevelRequest.INTERESTED;
        var request = new LikeSendRequest(receiverId, likeLevel);

        when(likeCommandRepository.existsBySenderIdAndReceiverId(senderId, receiverId)).thenReturn(false);

        var sender = MemberFixture.builder().id(senderId).gender(Gender.MALE).build();
        var receiver = MemberFixture.builder().id(receiverId).gender(Gender.FEMALE).build();

        when(memberCommandRepository.findById(senderId)).thenReturn(Optional.of(sender));
        when(memberCommandRepository.findById(receiverId)).thenReturn(Optional.of(receiver));

        when(blockRepository.existsByBlockerIdAndBlockedId(senderId, receiverId)).thenReturn(false);
        when(blockRepository.existsByBlockerIdAndBlockedId(receiverId, senderId)).thenReturn(true);

        // when && then
        assertThatThrownBy(() -> likeSendService.send(senderId, request))
            .isInstanceOf(DeeppleException.class)
            .hasFieldOrPropertyWithValue("errorCode", LikeErrorCode.LIKE_BLOCKED);
    }
}
