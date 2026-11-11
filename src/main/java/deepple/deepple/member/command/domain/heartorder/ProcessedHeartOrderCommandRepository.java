package deepple.deepple.member.command.domain.heartorder;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessedHeartOrderCommandRepository extends JpaRepository<ProcessedHeartOrder, Long> {
    boolean existsByOperationTypeAndOrderId(ProcessedHeartOperation operationType, long orderId);
}
