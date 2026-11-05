package deepple.deepple.admin.presentation.admin;

import deepple.deepple.admin.command.domain.admin.Admin;
import deepple.deepple.admin.command.domain.admin.AdminCommandRepository;
import deepple.deepple.admin.command.domain.admin.AdminRole;
import deepple.deepple.admin.command.domain.admin.Email;
import deepple.deepple.admin.command.domain.admin.Name;
import deepple.deepple.admin.command.domain.admin.Password;
import deepple.deepple.admin.command.domain.admin.PasswordHasher;
import deepple.deepple.admin.command.domain.admin.PhoneNumber;
import deepple.deepple.common.support.IntegrationTestSupport;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

import static org.assertj.core.api.Assertions.assertThat;

class AdminAccountIntegrationTest extends IntegrationTestSupport {

    private static final String SUPER_ADMIN_EMAIL = "super-admin@deepple.test";
    private static final String SUPER_ADMIN_PASSWORD = "SuperAdmin1234!";
    private static final String APPLICANT_EMAIL = "applicant@deepple.test";
    private static final String APPLICANT_PASSWORD = "Applicant1234!";

    @Autowired
    private MockMvcTester mockMvc;

    @Autowired
    private AdminCommandRepository adminCommandRepository;

    @Autowired
    private PasswordHasher passwordHasher;

    @BeforeEach
    void setUp() {
        Admin superAdmin = Admin.builder()
            .email(Email.from(SUPER_ADMIN_EMAIL))
            .password(Password.fromRaw(SUPER_ADMIN_PASSWORD, passwordHasher))
            .name(Name.from("슈퍼관리자"))
            .phoneNumber(PhoneNumber.from("01000000000"))
            .build();
        superAdmin.approve(AdminRole.SUPER_ADMIN);
        adminCommandRepository.save(superAdmin);
    }

    @AfterEach
    void tearDown() {
        deleteAdmin(SUPER_ADMIN_EMAIL);
        deleteAdmin(APPLICANT_EMAIL);
    }

    @Test
    @DisplayName("승인되지 않은 관리자는 로그인하면 403과 ADMIN_NOT_APPROVED 코드를 응답한다.")
    void rejectLoginBeforeApproval() {
        signup();

        MvcTestResult result = login(APPLICANT_EMAIL, APPLICANT_PASSWORD);

        assertThat(result).hasStatus(HttpStatus.FORBIDDEN);
        assertThat(result)
            .bodyJson()
            .extractingPath("$.code")
            .isEqualTo("403006");
    }

    @Test
    @DisplayName("슈퍼 관리자가 승인하면 로그인할 수 있다.")
    void allowLoginAfterApproval() {
        long applicantId = signup();
        String superAdminToken = accessToken(login(SUPER_ADMIN_EMAIL, SUPER_ADMIN_PASSWORD));

        MvcTestResult approveResult = mockMvc.post()
            .uri("/admin/accounts/{adminId}/approve", applicantId)
            .header(HttpHeaders.AUTHORIZATION, superAdminToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"role\": \"ADMIN\"}")
            .exchange();

        assertThat(approveResult).hasStatusOk();
        assertThat(login(APPLICANT_EMAIL, APPLICANT_PASSWORD)).hasStatusOk();
    }

    @Test
    @DisplayName("슈퍼 관리자 역할로 승인하면 관리자 승인 API를 호출할 수 있다.")
    void approveAsSuperAdmin() {
        long applicantId = signup();
        String superAdminToken = accessToken(login(SUPER_ADMIN_EMAIL, SUPER_ADMIN_PASSWORD));

        MvcTestResult approveResult = mockMvc.post()
            .uri("/admin/accounts/{adminId}/approve", applicantId)
            .header(HttpHeaders.AUTHORIZATION, superAdminToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"role\": \"SUPER_ADMIN\"}")
            .exchange();

        assertThat(approveResult).hasStatusOk();

        String applicantToken = accessToken(login(APPLICANT_EMAIL, APPLICANT_PASSWORD));
        MvcTestResult result = mockMvc.get()
            .uri("/admin/accounts")
            .header(HttpHeaders.AUTHORIZATION, applicantToken)
            .exchange();

        assertThat(result).hasStatusOk();
    }

    @Test
    @DisplayName("거절하면 가입 요청이 삭제되어 같은 이메일로 다시 가입할 수 있다.")
    void allowSignupAgainAfterRejection() {
        long applicantId = signup();
        String superAdminToken = accessToken(login(SUPER_ADMIN_EMAIL, SUPER_ADMIN_PASSWORD));

        MvcTestResult rejectResult = mockMvc.post()
            .uri("/admin/accounts/{adminId}/reject", applicantId)
            .header(HttpHeaders.AUTHORIZATION, superAdminToken)
            .exchange();

        assertThat(rejectResult).hasStatusOk();
        assertThat(signupResult()).hasStatusOk();
    }

    private long signup() {
        signupResult();
        return adminCommandRepository.findByEmail(Email.from(APPLICANT_EMAIL))
            .orElseThrow()
            .getId();
    }

    private MvcTestResult signupResult() {
        return mockMvc.post()
            .uri("/admin/signup")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"email": "%s", "password": "%s", "name": "신청자", "phoneNumber": "01012345678"}
                """.formatted(APPLICANT_EMAIL, APPLICANT_PASSWORD))
            .exchange();
    }

    private MvcTestResult login(String email, String password) {
        return mockMvc.post()
            .uri("/admin/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"email": "%s", "password": "%s"}
                """.formatted(email, password))
            .exchange();
    }

    private void deleteAdmin(String email) {
        adminCommandRepository.findByEmail(Email.from(email))
            .ifPresent(adminCommandRepository::delete);
    }

    private String accessToken(MvcTestResult loginResult) {
        return loginResult.getResponse()
            .getHeader(HttpHeaders.AUTHORIZATION);
    }
}
