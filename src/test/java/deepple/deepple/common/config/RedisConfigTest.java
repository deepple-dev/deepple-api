package deepple.deepple.common.config;

import deepple.deepple.datingexam.application.dto.DatingExamAnswerInfo;
import deepple.deepple.datingexam.application.dto.DatingExamInfoResponse;
import deepple.deepple.datingexam.application.dto.DatingExamQuestionInfo;
import deepple.deepple.datingexam.application.dto.DatingExamSubjectInfo;
import deepple.deepple.notification.command.application.NotificationTemplateInfo;
import deepple.deepple.notification.command.domain.NotificationType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Redis 캐시 직렬화")
class RedisConfigTest {

    private final GenericJacksonJsonRedisSerializer serializer = RedisConfig.jsonRedisSerializer();

    @Test
    @DisplayName("연애고사 정보 캐시 값이 그대로 복원된다")
    void roundTripDatingExamInfo() {
        DatingExamAnswerInfo answer = new DatingExamAnswerInfo(1L, "답");
        DatingExamQuestionInfo question = new DatingExamQuestionInfo(1L, "질문", List.of(answer));
        DatingExamSubjectInfo subject = new DatingExamSubjectInfo(1L, "REQUIRED", "가치관", List.of(question));
        DatingExamInfoResponse value = new DatingExamInfoResponse(List.of(subject));

        Object restored = serializer.deserialize(serializer.serialize(value));

        assertThat(restored).isEqualTo(value);
    }

    @Test
    @DisplayName("알림 템플릿 캐시 값이 그대로 복원된다")
    void roundTripNotificationTemplate() {
        NotificationTemplateInfo value = new NotificationTemplateInfo(1L, NotificationType.values()[0], "제목", "본문");

        Object restored = serializer.deserialize(serializer.serialize(value));

        assertThat(restored).isEqualTo(value);
    }

    @Test
    @DisplayName("허용하지 않은 타입은 역직렬화하지 않는다")
    void rejectUnknownType() {
        byte[] json = """
            ["javax.naming.InitialContext",{}]""".getBytes();

        assertThatThrownBy(() -> serializer.deserialize(json))
            .isInstanceOf(SerializationException.class);
    }
}
