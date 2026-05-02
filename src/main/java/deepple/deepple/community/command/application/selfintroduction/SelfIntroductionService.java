package deepple.deepple.community.command.application.selfintroduction;

import deepple.deepple.common.infra.s3.S3Uploader;
import deepple.deepple.common.infra.s3.dto.PresignedUrlResponse;
import deepple.deepple.community.command.application.selfintroduction.exception.NotSelfIntroductionAuthorException;
import deepple.deepple.community.command.application.selfintroduction.exception.SelfIntroductionNotFoundException;
import deepple.deepple.community.command.domain.selfintroduction.SelfIntroduction;
import deepple.deepple.community.command.domain.selfintroduction.SelfIntroductionCommandRepository;
import deepple.deepple.community.presentation.selfintroduction.dto.SelfIntroductionWriteRequest;
import deepple.deepple.member.command.application.member.exception.MemberNotFoundException;
import deepple.deepple.member.command.domain.member.MemberCommandRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SelfIntroductionService {
    private static final String IMAGE_PATH_PREFIX = "self-introduction";

    private final SelfIntroductionCommandRepository selfIntroductionCommandRepository;
    private final MemberCommandRepository memberCommandRepository;
    private final S3Uploader s3Uploader;

    @Transactional
    public void write(SelfIntroductionWriteRequest request, Long memberId) {
        validateMemberId(memberId);
        SelfIntroduction selfIntroduction = SelfIntroduction.write(memberId, request.title(), request.content(),
            request.imageUrl());
        selfIntroductionCommandRepository.save(selfIntroduction);
    }

    @Transactional
    public void update(SelfIntroductionWriteRequest request, Long memberId, Long id) {
        validateMemberId(memberId);
        SelfIntroduction selfIntroduction = getSelfIntroductionById(id);
        validateSelfIntroductionAuthor(selfIntroduction.getMemberId(), memberId);

        selfIntroduction.update(request.title(), request.content(), request.imageUrl());
    }

    @Transactional
    public void delete(Long id, Long memberId) {
        validateMemberId(memberId);
        validateSelfIntroductionAuthor(getSelfIntroductionById(id).getMemberId(), memberId);
        selfIntroductionCommandRepository.deleteById(id);
    }

    public PresignedUrlResponse getPresignedUrl(String fileName, Long memberId) {
        validateMemberId(memberId);
        return s3Uploader.getPresignedUrl(fileName, memberId, IMAGE_PATH_PREFIX);
    }

    @Transactional
    public void changeOpenStatus(Long id, boolean open) {
        SelfIntroduction selfIntroduction = getSelfIntroductionById(id);
        if (open) {
            selfIntroduction.open();
        } else {
            selfIntroduction.close();
        }
    }

    private void validateSelfIntroductionAuthor(Long memberIdFromSelfIntroduction, Long memberId) {
        if (!memberIdFromSelfIntroduction.equals(memberId)) {
            throw new NotSelfIntroductionAuthorException();
        }
    }

    private SelfIntroduction getSelfIntroductionById(Long id) {
        return selfIntroductionCommandRepository.findById(id).orElseThrow(SelfIntroductionNotFoundException::new);
    }

    private void validateMemberId(Long memberId) {
        memberCommandRepository.findById(memberId).orElseThrow(MemberNotFoundException::new);
    }
}
