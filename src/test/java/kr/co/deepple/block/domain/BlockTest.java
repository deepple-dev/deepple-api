package kr.co.deepple.block.domain;

import kr.co.deepple.common.exception.DeeppleException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BlockTest {

    @Nested
    @DisplayName("차단을 생성할 때")
    class CreateBlockTest {

        @Test
        @DisplayName("차단하는 id와 차단당하는 id가 같으면 SELF_BLOCK 예외를 던진다.")
        void throwsSelfBlockWhenBlockerIdEqualsBlockedId() {
            // given
            Long blockerId = 1L;
            Long blockedId = blockerId;

            // when
            DeeppleException result = assertThrows(DeeppleException.class, () -> {
                Block.of(blockerId, blockedId);
            });

            // then
            assertThat(result.getErrorCode()).isEqualTo(BlockErrorCode.SELF_BLOCK);
        }

        @Test
        @DisplayName("차단하는 id가 null이면 NullPointerException 예외를 던진다.")
        void throwsNullPointerExceptionWhenBlockerIdIsNull() {
            // given
            Long blockerId = null;
            Long blockedId = 1L;

            // when
            NullPointerException result = assertThrows(NullPointerException.class, () -> {
                Block.of(blockerId, blockedId);
            });

            // then
            assertThat(result).isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("차단당하는 id가 null이면 NullPointerException 예외를 던진다.")
        void throwsNullPointerExceptionWhenBlockedIdIsNull() {
            // given
            Long blockerId = 1L;
            Long blockedId = null;

            // when
            NullPointerException result = assertThrows(NullPointerException.class, () -> {
                Block.of(blockerId, blockedId);
            });

            // then
            assertThat(result).isInstanceOf(NullPointerException.class);
        }
    }
}