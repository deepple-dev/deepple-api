package kr.co.deepple.auth.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.groups.Tuple.tuple;

import kr.co.deepple.auth.application.dto.AdminSearchCondition;
import kr.co.deepple.auth.application.dto.AdminView;
import kr.co.deepple.auth.domain.Admin;
import kr.co.deepple.auth.domain.AdminRole;
import kr.co.deepple.auth.domain.ApprovalStatus;
import kr.co.deepple.auth.domain.AuthErrorCode;
import kr.co.deepple.auth.fixture.AdminFixture;
import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.common.support.RepositoryTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

class AdminQueryRepositoryImplTest extends RepositoryTestSupport {

  @Autowired private TestEntityManager entityManager;

  @Autowired private AdminQueryRepositoryImpl adminQueryRepository;

  @BeforeEach
  void setUp() {
    entityManager.persist(
        AdminFixture.builder()
            .email("super@deepple.com")
            .name("김슈퍼")
            .role(AdminRole.SUPER_ADMIN)
            .approvalStatus(ApprovalStatus.APPROVED)
            .build());
    entityManager.persist(
        AdminFixture.builder()
            .email("approved@deepple.com")
            .name("이승인")
            .role(AdminRole.ADMIN)
            .approvalStatus(ApprovalStatus.APPROVED)
            .build());
    entityManager.persist(
        AdminFixture.builder()
            .email("pending@test.com")
            .name("박대기")
            .role(null)
            .approvalStatus(ApprovalStatus.PENDING)
            .build());
    entityManager.flush();
  }

  @Test
  @DisplayName("조건이 없으면 승인 상태와 역할에 관계없이 모든 관리자를 역할·승인 상태와 함께 찾는다.")
  void findAllWithoutCondition() {
    assertThat(adminQueryRepository.findAdmins(new AdminSearchCondition(null, null, null, null)))
        .extracting(AdminView::email, AdminView::role, AdminView::approvalStatus)
        .containsExactlyInAnyOrder(
            tuple("super@deepple.com", "SUPER_ADMIN", "APPROVED"),
            tuple("approved@deepple.com", "ADMIN", "APPROVED"),
            tuple("pending@test.com", null, "PENDING"));
  }

  @Test
  @DisplayName("삭제된 관리자는 찾지 않는다.")
  void excludeDeletedAdmin() {
    Admin deleted = AdminFixture.builder().email("deleted@deepple.com").build();
    deleted.delete();
    entityManager.persist(deleted);
    entityManager.flush();

    assertThat(adminQueryRepository.findAdmins(new AdminSearchCondition(null, null, null, null)))
        .extracting(AdminView::email)
        .doesNotContain("deleted@deepple.com")
        .hasSize(3);
  }

  @Test
  @DisplayName("승인 상태로 찾는다.")
  void findByApprovalStatus() {
    assertThat(
            adminQueryRepository.findAdmins(new AdminSearchCondition("PENDING", null, null, null)))
        .extracting(AdminView::email)
        .containsExactly("pending@test.com");
  }

  @Test
  @DisplayName("역할로 찾고, 역할이 없는 승인 대기 관리자는 찾지 않는다.")
  void findByRole() {
    assertThat(
            adminQueryRepository.findAdmins(
                new AdminSearchCondition(null, "SUPER_ADMIN", null, null)))
        .extracting(AdminView::email)
        .containsExactly("super@deepple.com");
    assertThat(adminQueryRepository.findAdmins(new AdminSearchCondition(null, "ADMIN", null, null)))
        .extracting(AdminView::email)
        .containsExactly("approved@deepple.com");
  }

  @Test
  @DisplayName("이름과 이메일은 일부만 맞아도 찾고, 여러 조건은 모두 만족하는 관리자만 찾는다.")
  void findByNameAndEmailContains() {
    assertThat(adminQueryRepository.findAdmins(new AdminSearchCondition(null, null, "승인", null)))
        .extracting(AdminView::email)
        .containsExactly("approved@deepple.com");
    assertThat(
            adminQueryRepository.findAdmins(
                new AdminSearchCondition("APPROVED", "ADMIN", null, "deepple")))
        .extracting(AdminView::email)
        .containsExactly("approved@deepple.com");
  }

  @Test
  @DisplayName("승인 상태 값이 잘못되면 INVALID_APPROVAL_STATUS 예외를 던진다.")
  void throwWhenApprovalStatusInvalid() {
    assertThatThrownBy(() ->
            adminQueryRepository.findAdmins(new AdminSearchCondition("REJECTED", null, null, null)))
        .isInstanceOf(DeeppleException.class)
        .extracting("errorCode")
        .isEqualTo(AuthErrorCode.INVALID_APPROVAL_STATUS);
  }
}
