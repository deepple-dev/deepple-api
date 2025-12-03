package deepple.deepple.auth.domain;

public interface TokenRepository {

    void save(String token);

    boolean delete(String token);

    boolean exists(String token);
}
