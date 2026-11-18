package kr.co.deepple.auth.adapter.out.persistence;

import static kr.co.deepple.auth.domain.QAdmin.admin;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import kr.co.deepple.auth.application.dto.AdminSearchCondition;
import kr.co.deepple.auth.application.dto.AdminView;
import kr.co.deepple.auth.application.dto.QAdminView;
import kr.co.deepple.auth.application.port.out.AdminQueryRepository;
import kr.co.deepple.auth.domain.AdminRole;
import kr.co.deepple.auth.domain.ApprovalStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

@Repository
@RequiredArgsConstructor
public class AdminQueryRepositoryImpl implements AdminQueryRepository {

  private final JPAQueryFactory queryFactory;

  @Override
  public List<AdminView> findAdmins(AdminSearchCondition condition) {
    return queryFactory
        .select(
            new QAdminView(
                admin.id,
                admin.email.address,
                admin.name.value,
                admin.phoneNumber.value,
                admin.comment,
                admin.role.stringValue(),
                admin.approvalStatus.stringValue(),
                admin.createdAt))
        .from(admin)
        .where(
            admin.deletedAt.isNull(),
            approvalStatusEq(condition.approvalStatus()),
            roleEq(condition.role()),
            nameContains(condition.name()),
            emailContains(condition.email()))
        .orderBy(admin.createdAt.asc())
        .fetch();
  }

  private BooleanExpression approvalStatusEq(String approvalStatus) {
    return StringUtils.hasText(approvalStatus)
        ? admin.approvalStatus.eq(ApprovalStatus.from(approvalStatus))
        : null;
  }

  private BooleanExpression roleEq(String role) {
    return StringUtils.hasText(role) ? admin.role.eq(AdminRole.from(role)) : null;
  }

  private BooleanExpression nameContains(String name) {
    return StringUtils.hasText(name) ? admin.name.value.contains(name) : null;
  }

  private BooleanExpression emailContains(String email) {
    return StringUtils.hasText(email) ? admin.email.address.contains(email) : null;
  }
}
