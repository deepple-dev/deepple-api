package kr.co.deepple.auth.application.port.out;

public interface AuthMessageRepository {
  void save(String key, String message);

  String getByKey(String key);

  void delete(String key);
}
