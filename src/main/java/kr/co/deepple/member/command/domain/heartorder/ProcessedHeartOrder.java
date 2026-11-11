package kr.co.deepple.member.command.domain.heartorder;

import kr.co.deepple.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Entity
@Table(
    name = "processed_heart_orders",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_processed_heart_orders_operation_type_order_id",
        columnNames = {"operationType", "orderId"}
    )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProcessedHeartOrder extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(50)")
    private ProcessedHeartOperation operationType;

    @Column(nullable = false)
    private long orderId;

    private ProcessedHeartOrder(@NonNull ProcessedHeartOperation operationType, long orderId) {
        this.operationType = operationType;
        this.orderId = orderId;
    }

    public static ProcessedHeartOrder of(ProcessedHeartOperation operationType, long orderId) {
        return new ProcessedHeartOrder(operationType, orderId);
    }
}
