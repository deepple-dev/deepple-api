package kr.co.deepple.heart.adapter.out.persistence;

import static kr.co.deepple.heart.domain.QHeartTransaction.heartTransaction;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import kr.co.deepple.heart.application.dto.AdminHeartTransactionSearchCondition;
import kr.co.deepple.heart.application.dto.AdminHeartTransactionView;
import kr.co.deepple.heart.application.dto.QAdminHeartTransactionView;
import kr.co.deepple.heart.application.port.out.AdminHeartTransactionQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class AdminHeartTransactionQueryRepositoryImpl
    implements AdminHeartTransactionQueryRepository {

  private final JPAQueryFactory queryFactory;

  @Override
  public Page<AdminHeartTransactionView> findPage(
      AdminHeartTransactionSearchCondition condition, Set<Long> memberIds, Pageable pageable) {
    if (memberIds != null && memberIds.isEmpty()) {
      return new PageImpl<>(List.of(), pageable, 0);
    }

    List<AdminHeartTransactionView> views =
        queryFactory
            .select(
                new QAdminHeartTransactionView(
                    heartTransaction.id,
                    heartTransaction.createdAt,
                    heartTransaction.transactionType.stringValue(),
                    heartTransaction.content,
                    heartTransaction.amount.amount,
                    getHeartBalance()))
            .from(heartTransaction)
            .where(
                memberIdIn(memberIds),
                createdAtGoe(condition.createdDateGoe()),
                createdAtLoe(condition.createdDateLoe()))
            .orderBy(heartTransaction.id.desc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

    long totalCount =
        queryFactory
            .select(heartTransaction.count())
            .from(heartTransaction)
            .where(
                memberIdIn(memberIds),
                createdAtGoe(condition.createdDateGoe()),
                createdAtLoe(condition.createdDateLoe()))
            .fetchOne();

    return new PageImpl<>(views, pageable, totalCount);
  }

  private NumberExpression<Long> getHeartBalance() {
    return heartTransaction.balance.missionBalance.add(heartTransaction.balance.purchaseBalance);
  }

  private BooleanExpression memberIdIn(Set<Long> memberIds) {
    return memberIds != null ? heartTransaction.memberId.in(memberIds) : null;
  }

  private BooleanExpression createdAtGoe(LocalDate createdDateGoe) {
    return createdDateGoe != null
        ? heartTransaction.createdAt.goe(createdDateGoe.atStartOfDay())
        : null;
  }

  private BooleanExpression createdAtLoe(LocalDate createdDateLoe) {
    return createdDateLoe != null
        ? heartTransaction.createdAt.loe(createdDateLoe.plusDays(1).atStartOfDay().minusSeconds(1))
        : null;
  }
}
