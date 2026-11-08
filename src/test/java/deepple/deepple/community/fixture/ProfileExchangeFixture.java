package deepple.deepple.community.fixture;

import deepple.deepple.community.command.domain.profileexchange.ProfileExchange;
import deepple.deepple.community.command.domain.profileexchange.ProfileExchangeStatus;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import static org.springframework.beans.BeanUtils.instantiateClass;
import static org.springframework.test.util.ReflectionTestUtils.setField;

@Setter
@Accessors(fluent = true, chain = true)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ProfileExchangeFixture {

    private Long id;
    private long requesterId = 1L;
    private long responderId = 2L;
    private ProfileExchangeStatus status = ProfileExchangeStatus.WAITING;

    public static ProfileExchangeFixture builder() {
        return new ProfileExchangeFixture();
    }

    public ProfileExchange build() {
        ProfileExchange profileExchange = instantiateClass(ProfileExchange.class);
        setField(profileExchange, "id", id);
        setField(profileExchange, "requesterId", requesterId);
        setField(profileExchange, "responderId", responderId);
        setField(profileExchange, "status", status);
        return profileExchange;
    }
}
