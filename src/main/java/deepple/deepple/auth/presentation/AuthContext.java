package deepple.deepple.auth.presentation;

import deepple.deepple.common.enums.Role;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class AuthContext {

    private long id;
    private Role role;

    public void authenticate(long id, @NonNull Role role) {
        this.id = id;
        this.role = role;
    }
}
