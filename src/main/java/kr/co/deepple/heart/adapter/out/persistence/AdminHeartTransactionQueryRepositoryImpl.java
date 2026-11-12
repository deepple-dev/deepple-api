package kr.co.deepple.heart.adapter.out.persistence;

import static kr.co.deepple.heart.domain.QHeartTransaction.heartTransaction;
import static kr.co.deepple.member.command.domain.member.QMember.member;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDate;
import java.util.HashSet;
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
      AdminHeartTransactionSearchCondition condition, Pageable pageable) {
    Set<Long> memberIds = getMemberIds(condition.nickname(), condition.phoneNumber());
    if (memberIds != null && memberIds.isEmpty()) {
      // nickname과 phoneNumber 검색 결과 대상 멤버가 없는 경우 빈 페이지를 반환합니다.
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

  private Set<Long> getMemberIds(String nickname, String phoneNumber) {
    if (nickname == null && phoneNumber == null) {
      // nickname과 phoneNumber가 모두 null인 경우 null을 반환하여 memberId 조건을 무시합니다.
      return null;
    }

    List<Long> list =
        queryFactory
            .select(member.id)
            .from(member)
            .where(nicknameContains(nickname), phoneNumberEq(phoneNumber))
            .fetch();

    return new HashSet<>(list);
  }

  private BooleanExpression nicknameContains(String nickname) {
    return nickname != null ? member.profile.nickname.value.contains(nickname) : null;
  }

  private BooleanExpression phoneNumberEq(String phoneNumber) {
    return phoneNumber != null ? member.phoneNumber.value.eq(phoneNumber) : null;
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
