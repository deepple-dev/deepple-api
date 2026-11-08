package deepple.deepple.block.application;

import deepple.deepple.block.application.required.BlockRepository;
import deepple.deepple.block.domain.Block;
import deepple.deepple.block.domain.BlockErrorCode;
import deepple.deepple.block.fixture.BlockFixture;
import deepple.deepple.common.exception.DeeppleException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BlockModifyServiceTest {

    @InjectMocks
    private BlockModifyService blockModifyService;

    @Mock
    private BlockRepository blockRepository;

    @Nested
    @DisplayName("차단을 생성할 때")
    class CreateBlockTest {
        @Test
        @DisplayName("이미 차단된 멤버면 BLOCK_ALREADY_EXISTS 예외를 던진다.")
        void throwsBlockAlreadyExistsWhenBlockAlreadyExists() {
            // given
            Long blockerId = 1L;
            Long blockedId = 2L;

            when(blockRepository.existsByBlockerIdAndBlockedId(blockerId, blockedId)).thenReturn(true);

            // when
            DeeppleException result = assertThrows(DeeppleException.class, () -> {
                blockModifyService.createBlock(blockerId, blockedId);
            });

            // then
            assertThat(result.getErrorCode()).isEqualTo(BlockErrorCode.BLOCK_ALREADY_EXISTS);
        }

        @Test
        @DisplayName("이미 차단된 멤버가 아니면 차단을 생성하고 저장한다.")
        void createsBlockWhenNotAlreadyBlocked() {
            // given
            Long blockerId = 1L;
            Long blockedId = 2L;

            when(blockRepository.existsByBlockerIdAndBlockedId(blockerId, blockedId)).thenReturn(false);

            Block block = BlockFixture.builder().blockerId(blockerId).blockedId(blockedId).build();

            try (MockedStatic<Block> blockMockedStatic = mockStatic(Block.class)) {
                blockMockedStatic.when(() -> Block.of(blockerId, blockedId)).thenReturn(block);

                when(blockRepository.save(block)).thenReturn(block);

                // when
                blockModifyService.createBlock(blockerId, blockedId);

                // then
                blockMockedStatic.verify(() -> Block.of(blockerId, blockedId), times(1));
                verify(blockRepository, times(1)).save(block);
            }
        }
    }

    @Nested
    @DisplayName("차단이 없을 때만 생성할 때")
    class CreateBlockIfAbsentTest {
        @Test
        @DisplayName("이미 차단된 멤버면 예외 없이 저장하지 않는다.")
        void skipsWhenBlockAlreadyExists() {
            // given
            long blockerId = 1L;
            long blockedId = 2L;

            when(blockRepository.existsByBlockerIdAndBlockedId(blockerId, blockedId)).thenReturn(true);

            // when
            blockModifyService.createBlockIfAbsent(blockerId, blockedId);

            // then
            verify(blockRepository, never()).save(any());
        }

        @Test
        @DisplayName("이미 차단된 멤버가 아니면 차단을 저장한다.")
        void createsBlockWhenNotAlreadyBlocked() {
            // given
            long blockerId = 1L;
            long blockedId = 2L;

            when(blockRepository.existsByBlockerIdAndBlockedId(blockerId, blockedId)).thenReturn(false);

            // when
            blockModifyService.createBlockIfAbsent(blockerId, blockedId);

            // then
            verify(blockRepository, times(1)).save(any(Block.class));
        }
    }
}
