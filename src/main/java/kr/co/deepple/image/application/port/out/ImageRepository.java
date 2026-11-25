package kr.co.deepple.image.application.port.out;

import jakarta.persistence.LockModeType;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import kr.co.deepple.image.domain.Image;
import kr.co.deepple.image.domain.ImageStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

public interface ImageRepository extends JpaRepository<Image, Long> {

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select i from Image i where i.sourceKey = :sourceKey")
  Optional<Image> findBySourceKeyForUpdate(String sourceKey);

  @Query("select i.id from Image i where i.id in :ids and i.status = :status")
  List<Long> findIdsByIdInAndStatus(Collection<Long> ids, ImageStatus status);
}
