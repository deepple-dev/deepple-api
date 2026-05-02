package deepple.deepple.member.command.application.profileimage;

import deepple.deepple.common.infra.s3.S3Uploader;
import deepple.deepple.member.command.application.profileImage.ProfileImageService;
import deepple.deepple.member.command.application.profileImage.exception.ExceedProfileImageCountException;
import deepple.deepple.member.command.application.profileImage.exception.InvalidProfileImageExtensionException;
import deepple.deepple.member.command.domain.profileImage.ProfileImageCommandRepository;
import deepple.deepple.member.presentation.profileimage.dto.ProfileImageUploadRequest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class ProfileImageServiceTest {

    @InjectMocks
    private ProfileImageService profileImageService;

    @Mock
    private ProfileImageCommandRepository profileImageCommandRepository;

    @Mock
    private S3Uploader s3Uploader;

    @Nested
    @DisplayName("pre-signed URL 테스트")
    class GetPresignedUrl {

        @Test
        @DisplayName("이미지 확장자가 허용되지 않는 경우, 예외 발생")
        void throwExceptionWhenFileExtensionIsNotIncluded_invalidExtension() {
            // Given
            String fileName = "error.txt";
            Long userId = 1L;

            // When & Then
            Assertions.assertThatThrownBy(() -> profileImageService.getPresignedUrl(fileName, userId))
                .isInstanceOf(InvalidProfileImageExtensionException.class);
        }
    }

    @Nested
    @DisplayName("프로필 이미지 엔티티 저장 테스트")
    class saveProfileImage {

        @Test
        @DisplayName("프로필 이미지 저장 요청 개수가 6개를 초과하는 경우, 예외 발생.")
        void throwExceptionWhenRequestSizeIsOver6() {
            // Given
            Long memberId = 1L;

            List<ProfileImageUploadRequest> requests = List.of(
                new ProfileImageUploadRequest("url1"), new ProfileImageUploadRequest("url2"),
                new ProfileImageUploadRequest("url3"), new ProfileImageUploadRequest("url4"),
                new ProfileImageUploadRequest("url5"), new ProfileImageUploadRequest("url6"),
                new ProfileImageUploadRequest("url7")
            );

            // When & Then
            Assertions.assertThatThrownBy(() -> profileImageService.save(memberId, requests))
                .isInstanceOf(ExceedProfileImageCountException.class);
        }

        void uploadImage() {
            // Given
            Long memberId = 1L;
            List<ProfileImageUploadRequest> request = List.of(
                new ProfileImageUploadRequest("https://bucket.s3.region.amazonaws.com/TEMP_IMAGE_1.jpeg"),
                new ProfileImageUploadRequest("https://bucket.s3.region.amazonaws.com/TEMP_IMAGE_1.jpeg"),
                new ProfileImageUploadRequest("https://bucket.s3.region.amazonaws.com/TEMP_IMAGE_1.jpeg"),
                new ProfileImageUploadRequest("https://bucket.s3.region.amazonaws.com/TEMP_IMAGE_1.jpeg"),
                new ProfileImageUploadRequest("https://bucket.s3.region.amazonaws.com/TEMP_IMAGE_1.jpeg"),
                new ProfileImageUploadRequest("https://bucket.s3.region.amazonaws.com/TEMP_IMAGE_2.jpeg"),
                new ProfileImageUploadRequest("https://bucket.s3.region.amazonaws.com/TEMP_IMAGE_3.jpeg"),
                new ProfileImageUploadRequest("https://bucket.s3.region.amazonaws.com/TEMP_IMAGE_4.jpeg")
            );
            // When
            profileImageService.save(memberId, request);

            // Then
            verify(profileImageCommandRepository).deleteByMemberId(memberId);
            verify(profileImageCommandRepository).saveAll(anyList());
        }
    }
}
