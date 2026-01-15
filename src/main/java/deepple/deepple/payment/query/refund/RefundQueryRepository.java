package deepple.deepple.payment.query.refund;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import deepple.deepple.payment.query.refund.condition.RefundSearchCondition;
import deepple.deepple.payment.query.refund.view.QRefundView;
import deepple.deepple.payment.query.refund.view.RefundView;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

import static deepple.deepple.payment.command.domain.refund.QRefund.refund;

@Repository
@RequiredArgsConstructor
public class RefundQueryRepository {
    private final JPAQueryFactory queryFactory;

    public Page<RefundView> findPage(RefundSearchCondition condition, Pageable pageable) {
        List<RefundView> views = queryFactory
            .select(new QRefundView(
                refund.id,
                refund.memberId,
                refund.transactionId,
                refund.refundDetail.productId,
                refund.refundDetail.quantity,
                refund.refundDetail.refundAmount,
                refund.paymentMethod,
                refund.notificationType,
                refund.refundedAt
            ))
            .from(refund)
            .where(
                memberIdEq(condition.memberId()),
                refundedAtGoe(condition.refundedDateGoe()),
                refundedAtLoe(condition.refundedDateLoe())
            )
            .orderBy(refund.refundedAt.desc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        Long totalCount = queryFactory
            .select(refund.count())
            .from(refund)
            .where(
                memberIdEq(condition.memberId()),
                refundedAtGoe(condition.refundedDateGoe()),
                refundedAtLoe(condition.refundedDateLoe())
            )
            .fetchOne();

        return new PageImpl<>(views, pageable, totalCount != null ? totalCount : 0L);
    }

    private BooleanExpression memberIdEq(Long memberId) {
        return memberId != null ? refund.memberId.eq(memberId) : null;
    }

    private BooleanExpression refundedAtGoe(LocalDate refundedDateGoe) {
        return refundedDateGoe != null ? refund.refundedAt.goe(refundedDateGoe.atStartOfDay()) : null;
    }

    private BooleanExpression refundedAtLoe(LocalDate refundedDateLoe) {
        return refundedDateLoe != null ? refund.refundedAt.lt(
            refundedDateLoe.plusDays(1).atStartOfDay()) : null;
    }
}