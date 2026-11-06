package deepple.deepple.admin.command.domain.admin;

import deepple.deepple.admin.command.domain.AdminErrorCode;
import deepple.deepple.common.enums.Role;
import deepple.deepple.common.exception.DeeppleException;
import lombok.Getter;

import java.util.Locale;

@Getter
public enum AdminRole {
    ADMIN("일반 관리자", Role.ADMIN),
    SUPER_ADMIN("슈퍼 관리자", Role.SUPER_ADMIN);

    private final String description;
    private final Role tokenRole;

    AdminRole(String description, Role tokenRole) {
        this.description = description;
        this.tokenRole = tokenRole;
    }

    public static AdminRole from(String value) {
        try {
            return AdminRole.valueOf(value.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new DeeppleException(AdminErrorCode.INVALID_ADMIN_ROLE, "value=" + value, e);
        }
    }
}
