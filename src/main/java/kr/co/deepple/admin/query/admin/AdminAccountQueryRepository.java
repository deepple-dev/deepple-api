package kr.co.deepple.admin.query.admin;

import static kr.co.deepple.admin.command.domain.admin.QAdmin.admin;

import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import kr.co.deepple.admin.command.domain.admin.ApprovalStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class AdminAccountQueryRepository {

  private final JPAQueryFactory queryFactory;

  public List<PendingAdminView> findPendingAdmins() {
    return queryFactory
        .select(
            new QPendingAdminView(
                admin.id,
                admin.email.address,
                admin.name.value,
                admin.phoneNumber.value,
                admin.comment,
                admin.createdAt))
        .from(admin)
        .where(admin.approvalStatus.eq(ApprovalStatus.PENDING))
        .orderBy(admin.createdAt.asc())
        .fetch();
  }
}
