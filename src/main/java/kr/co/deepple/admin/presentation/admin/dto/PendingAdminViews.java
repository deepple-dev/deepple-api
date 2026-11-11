package kr.co.deepple.admin.presentation.admin.dto;

import kr.co.deepple.admin.query.admin.PendingAdminView;

import java.util.List;

public record PendingAdminViews(
    List<PendingAdminView> admins
) {
}
