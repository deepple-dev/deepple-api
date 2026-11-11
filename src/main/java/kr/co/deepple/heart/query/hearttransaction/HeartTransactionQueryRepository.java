package kr.co.deepple.heart.query.hearttransaction;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import kr.co.deepple.heart.query.hearttransaction.condition.HeartTransactionSearchCondition;
import kr.co.deepple.heart.query.hearttransaction.view.HeartTransactionView;
import kr.co.deepple.heart.query.hearttransaction.view.QHeartTransactionView;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

import static kr.co.deepple.heart.command.domain.hearttransaction.QHeartTransaction.heartTransaction;

@Repository
@RequiredArgsConstructor
public class HeartTransactionQueryRepository {
    private final JPAQueryFactory queryFactory;

    public List<HeartTransactionView> findHeartTransactions(long memberId, HeartTransactionSearchCondition condition,
        int size) {
        if (size <= 0) {
            throw new IllegalArgumentException("Size must be greater than 0");
        }

        return queryFactory
            .select(new QHeartTransactionView(
                heartTransaction.id,
                heartTransaction.createdAt,
                heartTransaction.content,
                heartTransaction.heartAmount.amount
            ))
            .from(heartTransaction)
            .where(
                idLt(condition.lastId()),
                memberIdEq(memberId)
            )
            .orderBy(heartTransaction.id.desc())
            .limit(size)
            .fetch();
    }

    private BooleanExpression idLt(Long id) {
        return id != null ? heartTransaction.id.lt(id) : null;
    }

    private BooleanExpression memberIdEq(long memberId) {
        return heartTransaction.memberId.eq(memberId);
    }


}
