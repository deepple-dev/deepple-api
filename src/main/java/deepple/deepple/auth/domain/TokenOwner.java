package deepple.deepple.auth.domain;

import deepple.deepple.common.enums.Role;
import lombok.NonNull;

public record TokenOwner(long id, @NonNull Role role) {

    public static TokenOwner member(long id) {
        return new TokenOwner(id, Role.MEMBER);
    }

    public boolean isMember() {
        return role == Role.MEMBER;
    }

    public boolean isAdmin() {
        return !isMember();
    }
}
