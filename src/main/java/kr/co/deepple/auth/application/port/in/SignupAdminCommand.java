package kr.co.deepple.auth.application.port.in;

import lombok.NonNull;

public record SignupAdminCommand(
    @NonNull String email,
    @NonNull String password,
    @NonNull String name,
    @NonNull String phoneNumber) {}
