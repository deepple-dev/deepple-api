package kr.co.deepple.like.application;

import static kr.co.deepple.like.application.LikeMapper.toLikeLevel;
import static kr.co.deepple.mission.domain.ActionType.LIKE;

import kr.co.deepple.block.application.port.out.BlockRepository;
import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.like.application.dto.LikeSendRequest;
import kr.co.deepple.like.application.port.out.LikeCommandRepository;
import kr.co.deepple.like.domain.Like;
import kr.co.deepple.like.domain.LikeErrorCode;
import kr.co.deepple.like.event.LikeSentEvent;
import kr.co.deepple.member.application.port.out.MemberCommandRepository;
import kr.co.deepple.member.domain.Member;
import kr.co.deepple.member.domain.MemberErrorCode;
import kr.co.deepple.mission.application.MemberMissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LikeSendService {

  private final MemberMissionService memberMissionService;
  private final LikeCommandRepository likeCommandRepository;
  private final MemberCommandRepository memberCommandRepository;
  private final BlockRepository blockRepository;
  private final ApplicationEventPublisher eventPublisher;

  @Transactional
  public boolean send(long senderId, LikeSendRequest request) {
    long receiverId = request.receiverId();

    if (likeCommandRepository.existsBySenderIdAndReceiverId(senderId, receiverId)) {
      throw new DeeppleException(
          LikeErrorCode.LIKE_ALREADY_EXISTS, "senderId=" + senderId + ", receiverId=" + receiverId);
    }

    Member sender =
        memberCommandRepository
            .findById(senderId)
            .orElseThrow(() ->
                new DeeppleException(MemberErrorCode.MEMBER_NOT_FOUND, "memberId=" + senderId));
    Member receiver =
        memberCommandRepository
            .findById(receiverId)
            .orElseThrow(() ->
                new DeeppleException(MemberErrorCode.MEMBER_NOT_FOUND, "memberId=" + receiverId));
    validateMember(sender, receiver);

    Like like = Like.of(senderId, receiverId, toLikeLevel(request.likeLevel()));
    likeCommandRepository.save(like);

    boolean hasProcessedMission =
        memberMissionService.executeMissionsByAction(senderId, LIKE.name());
    eventPublisher.publishEvent(
        LikeSentEvent.of(senderId, sender.getProfile().getNickname().getValue(), receiverId));

    return hasProcessedMission;
  }

  private void validateMember(Member sender, Member receiver) {
    if (!receiver.isActive()) {
      throw new DeeppleException(
          LikeErrorCode.LIKE_RECEIVER_INACTIVE, "receiverId=" + receiver.getId());
    }
    if (sender.hasSameGender(receiver)) {
      throw new DeeppleException(
          LikeErrorCode.LIKE_SAME_GENDER,
          "senderId=" + sender.getId() + ", receiverId=" + receiver.getId());
    }
    if (blockRepository.existsByBlockerIdAndBlockedId(sender.getId(), receiver.getId())) {
      throw new DeeppleException(
          LikeErrorCode.LIKE_BLOCKED,
          "senderId=" + sender.getId() + ", receiverId=" + receiver.getId());
    }
    if (blockRepository.existsByBlockerIdAndBlockedId(receiver.getId(), sender.getId())) {
      throw new DeeppleException(
          LikeErrorCode.LIKE_BLOCKED,
          "senderId=" + sender.getId() + ", receiverId=" + receiver.getId());
    }
  }
}
