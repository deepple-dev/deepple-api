package deepple.deepple.admin.presentation.admin.dto;

import deepple.deepple.admin.query.admin.PendingAdminView;

import java.util.List;

public record PendingAdminViews(
    List<PendingAdminView> admins
) {
}
