package deepple.deepple.common.fixture;

import deepple.deepple.admin.fixture.AdminFixture;
import deepple.deepple.admin.fixture.ScreeningFixture;
import deepple.deepple.admin.fixture.SuspensionFixture;
import deepple.deepple.admin.fixture.WarningFixture;
import deepple.deepple.block.fixture.BlockFixture;
import deepple.deepple.common.support.RepositoryTestSupport;
import deepple.deepple.community.fixture.ProfileExchangeFixture;
import deepple.deepple.community.fixture.SelfIntroductionFixture;
import deepple.deepple.datingexam.fixture.*;
import deepple.deepple.heart.fixture.HeartTransactionFixture;
import deepple.deepple.heart.fixture.HeartUsagePolicyFixture;
import deepple.deepple.interview.fixture.InterviewAnswerFixture;
import deepple.deepple.interview.fixture.InterviewQuestionFixture;
import deepple.deepple.like.fixture.LikeFixture;
import deepple.deepple.match.fixture.MatchFixture;
import deepple.deepple.member.fixture.MemberFixture;
import deepple.deepple.member.fixture.MemberIdealFixture;
import deepple.deepple.member.fixture.MemberIntroductionFixture;
import deepple.deepple.member.fixture.ProfileImageFixture;
import deepple.deepple.mission.fixture.MemberMissionFixture;
import deepple.deepple.mission.fixture.MissionFixture;
import deepple.deepple.notification.fixture.DeviceRegistrationFixture;
import deepple.deepple.notification.fixture.NotificationFixture;
import deepple.deepple.notification.fixture.NotificationPreferenceFixture;
import deepple.deepple.notification.fixture.NotificationTemplateFixture;
import deepple.deepple.payment.fixture.HeartPurchaseOptionFixture;
import deepple.deepple.payment.fixture.OrderFixture;
import deepple.deepple.payment.fixture.RefundFixture;
import deepple.deepple.report.fixture.ReportFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.jupiter.params.provider.Arguments.arguments;

@DisplayName("fixture 기본값 저장 테스트")
class FixturePersistenceTest extends RepositoryTestSupport {

    @Autowired
    private TestEntityManager entityManager;

    static Stream<Arguments> fixtures() {
        return Stream.of(
            arguments("Admin", (Supplier<Object>) () -> AdminFixture.builder().build()),
            arguments("Screening", (Supplier<Object>) () -> ScreeningFixture.builder().build()),
            arguments("Suspension", (Supplier<Object>) () -> SuspensionFixture.builder().build()),
            arguments("Warning", (Supplier<Object>) () -> WarningFixture.builder().build()),
            arguments("Block", (Supplier<Object>) () -> BlockFixture.builder().build()),
            arguments("ProfileExchange", (Supplier<Object>) () -> ProfileExchangeFixture.builder().build()),
            arguments("SelfIntroduction", (Supplier<Object>) () -> SelfIntroductionFixture.builder().build()),
            arguments("DatingExamAnswer", (Supplier<Object>) () -> DatingExamAnswerFixture.builder().build()),
            arguments("DatingExamQuestion", (Supplier<Object>) () -> DatingExamQuestionFixture.builder().build()),
            arguments("DatingExamSubject", (Supplier<Object>) () -> DatingExamSubjectFixture.builder().build()),
            arguments("DatingExamSubmit", (Supplier<Object>) () -> DatingExamSubmitFixture.builder().build()),
            arguments("DatingExamSubmitResult",
                (Supplier<Object>) () -> DatingExamSubmitResultFixture.builder().build()),
            arguments("HeartTransaction", (Supplier<Object>) () -> HeartTransactionFixture.builder().build()),
            arguments("HeartUsagePolicy", (Supplier<Object>) () -> HeartUsagePolicyFixture.builder().build()),
            arguments("InterviewAnswer", (Supplier<Object>) () -> InterviewAnswerFixture.builder().build()),
            arguments("InterviewQuestion", (Supplier<Object>) () -> InterviewQuestionFixture.builder().build()),
            arguments("Like", (Supplier<Object>) () -> LikeFixture.builder().build()),
            arguments("Match", (Supplier<Object>) () -> MatchFixture.builder().build()),
            arguments("Member", (Supplier<Object>) () -> MemberFixture.builder().build()),
            arguments("MemberIdeal", (Supplier<Object>) () -> MemberIdealFixture.builder().build()),
            arguments("MemberIntroduction", (Supplier<Object>) () -> MemberIntroductionFixture.builder().build()),
            arguments("ProfileImage", (Supplier<Object>) () -> ProfileImageFixture.builder().build()),
            arguments("Mission", (Supplier<Object>) () -> MissionFixture.builder().build()),
            arguments("MemberMission", (Supplier<Object>) () -> MemberMissionFixture.builder().build()),
            arguments("DeviceRegistration", (Supplier<Object>) () -> DeviceRegistrationFixture.builder().build()),
            arguments("Notification", (Supplier<Object>) () -> NotificationFixture.builder().build()),
            arguments("NotificationPreference",
                (Supplier<Object>) () -> NotificationPreferenceFixture.builder().build()),
            arguments("NotificationTemplate", (Supplier<Object>) () -> NotificationTemplateFixture.builder().build()),
            arguments("HeartPurchaseOption", (Supplier<Object>) () -> HeartPurchaseOptionFixture.builder().build()),
            arguments("Order", (Supplier<Object>) () -> OrderFixture.builder().build()),
            arguments("Refund", (Supplier<Object>) () -> RefundFixture.builder().build()),
            arguments("Report", (Supplier<Object>) () -> ReportFixture.builder().build())
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("fixtures")
    @DisplayName("기본값으로 만든 엔티티를 저장할 수 있다")
    void persistDefault(String name, Supplier<Object> fixture) {
        Object entity = fixture.get();

        assertThatCode(() -> entityManager.persistAndFlush(entity)).doesNotThrowAnyException();
    }
}
