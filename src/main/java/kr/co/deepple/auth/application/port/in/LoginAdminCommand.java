package kr.co.deepple.auth.application.port.in;

import lombok.NonNull;

public record LoginAdminCommand(@NonNull String email, @NonNull String password) {}
