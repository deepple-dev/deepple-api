package deepple.deepple.community.command.domain.selfintroduction;

import deepple.deepple.community.command.domain.selfintroduction.exception.InvalidSelfIntroductionContentException;
import deepple.deepple.community.command.domain.selfintroduction.exception.InvalidSelfIntroductionTitleException;
import deepple.deepple.member.command.domain.profileImage.exception.InvalidImageUrlException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class SelfIntroductionTest {

    @Test
    @DisplayName("멤버 아이디와 제목이 존재하고, 셀프 소개의 내용이 30자 이상인 경우, 정상 동작.")
    void writeSelfIntroduction() {
        // Given
        Long memberId = 1L;
        String title = "셀프 소개 제목";
        String content = "셀프 소개 내용이 공백 포함하여 최소 30자 이상이어야 합니다.";

        // When
        SelfIntroduction selfIntroduction = SelfIntroduction.write(memberId, title, content, null);

        // Then
        assertThat(selfIntroduction).isNotNull();
        assertThat(selfIntroduction.getMemberId()).isEqualTo(memberId);
        assertThat(selfIntroduction.getContent()).isEqualTo(content);
        assertThat(selfIntroduction.getImageUrl()).isNull();
    }

    @Test
    @DisplayName("이미지 URL이 포함된 셀프 소개를 생성한다.")
    void writeSelfIntroductionWithImage() {
        // Given
        Long memberId = 1L;
        String title = "셀프 소개 제목";
        String content = "셀프 소개 내용이 공백 포함하여 최소 30자 이상이어야 합니다.";
        String imageUrl = "https://example.com/image.jpg";

        // When
        SelfIntroduction selfIntroduction = SelfIntroduction.write(memberId, title, content, imageUrl);

        // Then
        assertThat(selfIntroduction.getImageUrl()).isEqualTo(imageUrl);
    }

    @Nested
    @DisplayName("셀프 소개 생성 실패 테스트")
    class Fail {
        @Test
        @DisplayName("멤버 아이디가 null인 경우, 예외 발생")
        void throwExceptionWhenMemberIdIsNull() {
            // Given
            Long memberId = null;
            String title = "셀프 소개 제목";
            String content = "셀프 소개 내용.";

            // When & Then
            assertThatThrownBy(() -> SelfIntroduction.write(memberId, title, content, null))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("셀프 소개의 제목이 null인 경우, 예외 발생")
        void throwsExceptionWhenTitleIsNull() {
            // Given
            Long memberId = 1L;
            String title = null;
            String content = "셀프 소개 내용.";

            // When & Then
            assertThatThrownBy(() -> SelfIntroduction.write(memberId, title, content, null))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("셀프 소개의 제목이 빈 문자열이면, 예외 발생")
        void throwsExceptionWhenTitleIsBlank() {
            // Given
            Long memberId = 1L;
            String title = " ";
            String content = "셀프 소개 내용.";

            // When & Then
            assertThatThrownBy(() -> SelfIntroduction.write(memberId, title, content, null))
                .isInstanceOf(InvalidSelfIntroductionTitleException.class);
        }

        @Test
        @DisplayName("셀프 소개의 내용이 null인 경우, 예외 발생")
        void throwExceptionWhenContentIsNull() {
            // Given
            Long memberId = 1L;
            String title = "셀프 소개 제목";
            String content = null;

            // When & Then
            assertThatThrownBy(() -> SelfIntroduction.write(memberId, title, content, null))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("셀프 소개의 내용이 30자 미만인 경우, 예외 발생")
        void throwExceptionWhenContentIsLessThen30() {
            // Given
            Long memberId = 1L;
            String title = "셀프 소개 제목";
            String content = "30자 이하.";

            // When & Then
            assertThatThrownBy(() -> SelfIntroduction.write(memberId, title, content, null))
                .isInstanceOf(InvalidSelfIntroductionContentException.class);
        }

        @Test
        @DisplayName("이미지 URL이 빈 문자열이면, 예외 발생")
        void throwExceptionWhenImageUrlIsBlank() {
            // Given
            Long memberId = 1L;
            String title = "셀프 소개 제목";
            String content = "셀프 소개 내용이 공백 포함하여 최소 30자 이상이어야 합니다.";
            String imageUrl = "";

            // When & Then
            assertThatThrownBy(() -> SelfIntroduction.write(memberId, title, content, imageUrl))
                .isInstanceOf(InvalidImageUrlException.class);
        }
    }

    @DisplayName("셀프 소개의 공개 여부 변경 테스트")
    @Nested
    class Update {
        @Test
        @DisplayName("셀프 소개를 비공개로 변경한다.")
        void updateClose() {
            // Given
            Long memberId = 1L;
            String title = "셀프 소개 제목";
            String content = "셀프 소개 내용이 공백 포함하여 최소 30자 이상이어야 합니다.";
            SelfIntroduction selfIntroduction = SelfIntroduction.write(memberId, title, content, null);

            // When
            selfIntroduction.close();

            // Then
            assertThat(selfIntroduction.isOpened()).isFalse();
        }

        @Test
        @DisplayName("셀프 소개를 공개로 변경한다.")
        void updateOpen() {
            // Given
            Long memberId = 1L;
            String title = "셀프 소개 제목";
            String content = "셀프 소개 내용이 공백 포함하여 최소 30자 이상이어야 합니다.";
            SelfIntroduction selfIntroduction = SelfIntroduction.write(memberId, title, content, null);

            // When
            selfIntroduction.open();

            // Then
            assertThat(selfIntroduction.isOpened()).isTrue();
        }

        @Test
        @DisplayName("셀프 소개에 이미지를 추가한다.")
        void addImage() {
            // Given
            String content = "셀프 소개 내용이 공백 포함하여 최소 30자 이상이어야 합니다.";
            SelfIntroduction selfIntroduction = SelfIntroduction.write(1L, "title", content, null);
            String newImageUrl = "https://example.com/image.jpg";

            // When
            selfIntroduction.update("title", content, newImageUrl);

            // Then
            assertThat(selfIntroduction.getImageUrl()).isEqualTo(newImageUrl);
        }

        @Test
        @DisplayName("셀프 소개의 이미지를 제거한다.")
        void removeImage() {
            // Given
            String content = "셀프 소개 내용이 공백 포함하여 최소 30자 이상이어야 합니다.";
            SelfIntroduction selfIntroduction = SelfIntroduction.write(1L, "title", content,
                "https://example.com/image.jpg");

            // When
            selfIntroduction.update("title", content, null);

            // Then
            assertThat(selfIntroduction.getImageUrl()).isNull();
        }

        @Test
        @DisplayName("셀프 소개의 이미지를 다른 URL로 교체한다.")
        void replaceImage() {
            // Given
            String content = "셀프 소개 내용이 공백 포함하여 최소 30자 이상이어야 합니다.";
            SelfIntroduction selfIntroduction = SelfIntroduction.write(1L, "title", content,
                "https://example.com/old.jpg");
            String newImageUrl = "https://example.com/new.jpg";

            // When
            selfIntroduction.update("title", content, newImageUrl);

            // Then
            assertThat(selfIntroduction.getImageUrl()).isEqualTo(newImageUrl);
        }
    }
}
