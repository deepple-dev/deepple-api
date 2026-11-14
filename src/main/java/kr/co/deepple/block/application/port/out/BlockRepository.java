package kr.co.deepple.block.application.port.out;

import java.util.Set;
import kr.co.deepple.block.domain.Block;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface BlockRepository extends JpaRepository<Block, Long> {
  boolean existsByBlockerIdAndBlockedId(long blockerId, long blockedId);

  @Query("SELECT b.blockedId FROM Block b WHERE b.blockerId = :blockerId")
  Set<Long> findBlockedIdsByBlockerId(long blockerId);

  @Query("SELECT b.blockerId FROM Block b WHERE b.blockedId = :blockedId")
  Set<Long> findBlockerIdsByBlockedId(long blockedId);
}
