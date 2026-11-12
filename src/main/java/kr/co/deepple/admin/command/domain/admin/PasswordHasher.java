package kr.co.deepple.admin.command.domain.admin;

public interface PasswordHasher {
  String hash(String rawPassword);

  boolean matches(String rawPassword, String hashedPassword);
}
