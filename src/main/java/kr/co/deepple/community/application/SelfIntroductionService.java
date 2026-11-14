package kr.co.deepple.community.application;

import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.common.infra.s3.S3Uploader;
import kr.co.deepple.common.infra.s3.dto.PresignedUrlResponse;
import kr.co.deepple.community.application.dto.SelfIntroductionWriteRequest;
import kr.co.deepple.community.application.port.out.SelfIntroductionCommandRepository;
import kr.co.deepple.community.domain.CommunityErrorCode;
import kr.co.deepple.community.domain.SelfIntroduction;
import kr.co.deepple.community.event.SelfIntroductionFirstWrittenEvent;
import kr.co.deepple.member.application.port.in.FindMemberSummaryUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SelfIntroductionService {

  private static final String IMAGE_PATH_PREFIX = "self-introduction";

  private final SelfIntroductionCommandRepository selfIntroductionCommandRepository;
  private final FindMemberSummaryUseCase findMemberSummaryUseCase;
  private final S3Uploader s3Uploader;
  private final ApplicationEventPublisher eventPublisher;

  @Transactional
  public boolean write(SelfIntroductionWriteRequest request, long memberId) {
    validateMemberId(memberId);
    boolean isFirstSelfIntroduction = isFirstSelfIntroduction(memberId);
    SelfIntroduction selfIntroduction =
        SelfIntroduction.write(memberId, request.title(), request.content(), request.imageUrl());
    selfIntroductionCommandRepository.save(selfIntroduction);
    if (isFirstSelfIntroduction) {
      eventPublisher.publishEvent(SelfIntroductionFirstWrittenEvent.from(memberId));
    }
    return isFirstSelfIntroduction;
  }

  private boolean isFirstSelfIntroduction(long memberId) {
    return !selfIntroductionCommandRepository.existsByMemberId(memberId);
  }

  @Transactional
  public void update(SelfIntroductionWriteRequest request, long memberId, long id) {
    validateMemberId(memberId);
    SelfIntroduction selfIntroduction = getSelfIntroductionById(id);
    validateSelfIntroductionAuthor(selfIntroduction.getMemberId(), memberId);

    selfIntroduction.update(request.title(), request.content(), request.imageUrl());
  }

  @Transactional
  public void delete(long id, long memberId) {
    validateMemberId(memberId);
    validateSelfIntroductionAuthor(getSelfIntroductionById(id).getMemberId(), memberId);
    selfIntroductionCommandRepository.deleteById(id);
  }

  public PresignedUrlResponse getPresignedUrl(String fileName, long memberId) {
    validateMemberId(memberId);
    return s3Uploader.getPresignedUrl(fileName, memberId, IMAGE_PATH_PREFIX);
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
    return selfIntroductionCommandRepository
        .findById(id)
        .orElseThrow(() ->
            new DeeppleException(
                CommunityErrorCode.SELF_INTRODUCTION_NOT_FOUND, "selfIntroductionId=" + id));
  }

  private void validateMemberId(long memberId) {
    findMemberSummaryUseCase.getMemberSummary(memberId);
  }
}
