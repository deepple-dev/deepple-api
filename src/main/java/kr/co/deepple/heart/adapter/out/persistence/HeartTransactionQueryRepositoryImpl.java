package kr.co.deepple.heart.adapter.out.persistence;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import kr.co.deepple.heart.application.dto.HeartTransactionSearchCondition;
import kr.co.deepple.heart.application.dto.HeartTransactionView;
import kr.co.deepple.heart.application.dto.QHeartTransactionView;
import kr.co.deepple.heart.application.port.out.HeartTransactionQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

import static kr.co.deepple.heart.domain.QHeartTransaction.heartTransaction;

@Repository
@RequiredArgsConstructor
public class HeartTransactionQueryRepositoryImpl implements HeartTransactionQueryRepository {
    private final JPAQueryFactory queryFactory;

    @Override
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
