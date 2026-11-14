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
import kr.co.deepple.member.application.port.in.FindMemberSummaryUseCase;
import kr.co.deepple.member.application.port.in.MemberSummary;
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
  private final FindMemberSummaryUseCase findMemberSummaryUseCase;
  private final BlockRepository blockRepository;
  private final ApplicationEventPublisher eventPublisher;

  @Transactional
  public boolean send(long senderId, LikeSendRequest request) {
    long receiverId = request.receiverId();

    if (likeCommandRepository.existsBySenderIdAndReceiverId(senderId, receiverId)) {
      throw new DeeppleException(
          LikeErrorCode.LIKE_ALREADY_EXISTS, "senderId=" + senderId + ", receiverId=" + receiverId);
    }

    MemberSummary sender = findMemberSummaryUseCase.getMemberSummary(senderId);
    MemberSummary receiver = findMemberSummaryUseCase.getMemberSummary(receiverId);
    validateMember(sender, receiver);

    Like like = Like.of(senderId, receiverId, toLikeLevel(request.likeLevel()));
    likeCommandRepository.save(like);

    boolean hasProcessedMission =
        memberMissionService.executeMissionsByAction(senderId, LIKE.name());
    eventPublisher.publishEvent(LikeSentEvent.of(senderId, sender.requireNickname(), receiverId));

    return hasProcessedMission;
  }

  private void validateMember(MemberSummary sender, MemberSummary receiver) {
    if (!receiver.isActive()) {
      throw new DeeppleException(
          LikeErrorCode.LIKE_RECEIVER_INACTIVE, "receiverId=" + receiver.memberId());
    }
    if (sender.requireGender().equals(receiver.requireGender())) {
      throw new DeeppleException(
          LikeErrorCode.LIKE_SAME_GENDER,
          "senderId=" + sender.memberId() + ", receiverId=" + receiver.memberId());
    }
    if (blockRepository.existsByBlockerIdAndBlockedId(sender.memberId(), receiver.memberId())) {
      throw new DeeppleException(
          LikeErrorCode.LIKE_BLOCKED,
          "senderId=" + sender.memberId() + ", receiverId=" + receiver.memberId());
    }
    if (blockRepository.existsByBlockerIdAndBlockedId(receiver.memberId(), sender.memberId())) {
      throw new DeeppleException(
          LikeErrorCode.LIKE_BLOCKED,
          "senderId=" + sender.memberId() + ", receiverId=" + receiver.memberId());
    }
  }
}
