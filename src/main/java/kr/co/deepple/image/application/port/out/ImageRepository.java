package kr.co.deepple.image.application.port.out;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import kr.co.deepple.image.domain.Image;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

public interface ImageRepository extends JpaRepository<Image, Long> {

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select i from Image i where i.sourceKey = :sourceKey")
  Optional<Image> findBySourceKeyForUpdate(String sourceKey);
}
