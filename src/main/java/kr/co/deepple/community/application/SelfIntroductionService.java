package kr.co.deepple.community.application;

import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.community.application.dto.SelfIntroductionWriteRequest;
import kr.co.deepple.community.application.port.out.SelfIntroductionRepository;
import kr.co.deepple.community.domain.CommunityErrorCode;
import kr.co.deepple.community.domain.SelfIntroduction;
import kr.co.deepple.community.event.SelfIntroductionFirstWrittenEvent;
import kr.co.deepple.image.application.port.in.FindImageUseCase;
import kr.co.deepple.member.application.port.in.FindMemberSummaryUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SelfIntroductionService {

  private final SelfIntroductionRepository selfIntroductionRepository;
  private final FindMemberSummaryUseCase findMemberSummaryUseCase;
  private final FindImageUseCase findImageUseCase;
  private final ApplicationEventPublisher eventPublisher;

  @Transactional
  public boolean write(SelfIntroductionWriteRequest request, long memberId) {
    validateMemberId(memberId);
    boolean isFirstSelfIntroduction = isFirstSelfIntroduction(memberId);
    String imageKey = findImageKey(request.imageId(), memberId);
    SelfIntroduction selfIntroduction =
        SelfIntroduction.write(
            memberId, request.title(), request.content(), request.imageId(), imageKey);
    selfIntroductionRepository.save(selfIntroduction);
    if (isFirstSelfIntroduction) {
      eventPublisher.publishEvent(SelfIntroductionFirstWrittenEvent.from(memberId));
    }
    return isFirstSelfIntroduction;
  }

  private boolean isFirstSelfIntroduction(long memberId) {
    return !selfIntroductionRepository.existsByMemberId(memberId);
  }

  @Transactional
  public void update(SelfIntroductionWriteRequest request, long memberId, long id) {
    validateMemberId(memberId);
    SelfIntroduction selfIntroduction = getSelfIntroductionById(id);
    validateSelfIntroductionAuthor(selfIntroduction.getMemberId(), memberId);

    String imageKey = findImageKey(request.imageId(), memberId);
    selfIntroduction.update(request.title(), request.content(), request.imageId(), imageKey);
  }

  @Transactional
  public void delete(long id, long memberId) {
    validateMemberId(memberId);
    validateSelfIntroductionAuthor(getSelfIntroductionById(id).getMemberId(), memberId);
    selfIntroductionRepository.deleteById(id);
  }

  @Transactional
  public void changeOpenStatus(long id, boolean open) {
    SelfIntroduction selfIntroduction = getSelfIntroductionById(id);
    if (open) {
      selfIntroduction.open();
    } else {
      selfIntroduction.close();
    }
  }

  private void validateSelfIntroductionAuthor(long memberIdFromSelfIntroduction, long memberId) {
    if (memberIdFromSelfIntroduction != memberId) {
      throw new DeeppleException(
          CommunityErrorCode.SELF_INTRODUCTION_ACCESS_DENIED,
          "authorId=" + memberIdFromSelfIntroduction + ", memberId=" + memberId);
    }
  }

  private SelfIntroduction getSelfIntroductionById(long id) {
    return selfIntroductionRepository
        .findById(id)
        .orElseThrow(() ->
            new DeeppleException(
                CommunityErrorCode.SELF_INTRODUCTION_NOT_FOUND, "selfIntroductionId=" + id));
  }

  private String findImageKey(Long imageId, long memberId) {
    if (imageId == null) {
      return null;
    }
    return findImageUseCase.getMemberImage(imageId, memberId).imageKey();
  }

  private void validateMemberId(long memberId) {
    findMemberSummaryUseCase.getMemberSummary(memberId);
  }
}
