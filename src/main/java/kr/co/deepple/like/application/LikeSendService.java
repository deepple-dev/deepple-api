package kr.co.deepple.like.application;

import static kr.co.deepple.like.application.LikeMapper.toLikeLevel;

import kr.co.deepple.block.application.port.in.CheckBlockUseCase;
import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.like.application.dto.LikeSendRequest;
import kr.co.deepple.like.application.port.out.LikeRepository;
import kr.co.deepple.like.domain.Like;
import kr.co.deepple.like.domain.LikeErrorCode;
import kr.co.deepple.like.event.LikeSentEvent;
import kr.co.deepple.member.application.port.in.FindMemberSummaryUseCase;
import kr.co.deepple.member.application.port.in.MemberSummary;
import kr.co.deepple.mission.application.port.in.ExecuteMissionUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LikeSendService {

  private static final String LIKE_MISSION_ACTION = "LIKE";

  private final ExecuteMissionUseCase executeMissionUseCase;
  private final LikeRepository likeRepository;
  private final FindMemberSummaryUseCase findMemberSummaryUseCase;
  private final CheckBlockUseCase checkBlockUseCase;
  private final ApplicationEventPublisher eventPublisher;

  @Transactional
  public boolean send(long senderId, LikeSendRequest request) {
    long receiverId = request.receiverId();

    if (likeRepository.existsBySenderIdAndReceiverId(senderId, receiverId)) {
      throw new DeeppleException(
          LikeErrorCode.LIKE_ALREADY_EXISTS, "senderId=" + senderId + ", receiverId=" + receiverId);
    }

    MemberSummary sender = findMemberSummaryUseCase.getMemberSummary(senderId);
    MemberSummary receiver = findMemberSummaryUseCase.getMemberSummary(receiverId);
    validateMember(sender, receiver);

    Like like = Like.of(senderId, receiverId, toLikeLevel(request.likeLevel()));
    likeRepository.save(like);

    boolean hasProcessedMission =
        executeMissionUseCase.executeMissionsByAction(senderId, LIKE_MISSION_ACTION);
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
    if (checkBlockUseCase.isBlocked(sender.memberId(), receiver.memberId())) {
      throw new DeeppleException(
          LikeErrorCode.LIKE_BLOCKED,
          "senderId=" + sender.memberId() + ", receiverId=" + receiver.memberId());
    }
    if (checkBlockUseCase.isBlocked(receiver.memberId(), sender.memberId())) {
      throw new DeeppleException(
          LikeErrorCode.LIKE_BLOCKED,
          "senderId=" + sender.memberId() + ", receiverId=" + receiver.memberId());
    }
  }
}
