package kr.co.deepple.auth.adapter.out.persistence;

import static kr.co.deepple.auth.domain.QAdmin.admin;

import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import kr.co.deepple.auth.application.dto.PendingAdminView;
import kr.co.deepple.auth.application.dto.QPendingAdminView;
import kr.co.deepple.auth.application.port.out.AdminAccountQueryRepository;
import kr.co.deepple.auth.domain.ApprovalStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class AdminAccountQueryRepositoryImpl implements AdminAccountQueryRepository {

  private final JPAQueryFactory queryFactory;

  @Override
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
