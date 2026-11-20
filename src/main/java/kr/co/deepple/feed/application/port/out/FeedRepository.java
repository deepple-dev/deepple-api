package kr.co.deepple.feed.application.port.out;

import java.util.List;
import java.util.Optional;
import kr.co.deepple.feed.domain.Feed;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeedRepository extends JpaRepository<Feed, Long> {

  Optional<Feed> findByIdAndDeletedAtIsNull(long id);

  Optional<Feed> findByIdAndAuthorIdAndDeletedAtIsNull(long id, long authorId);

  List<Feed> findByAuthorIdAndDeletedAtIsNullOrderByIdDesc(long authorId, Limit limit);

  List<Feed> findByAuthorIdAndIdLessThanAndDeletedAtIsNullOrderByIdDesc(
      long authorId, long id, Limit limit);

  Optional<Feed> findByAuthorIdAndIdempotencyKey(long authorId, String idempotencyKey);
}
