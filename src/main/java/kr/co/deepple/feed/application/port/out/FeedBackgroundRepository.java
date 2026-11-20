package kr.co.deepple.feed.application.port.out;

import java.util.List;
import kr.co.deepple.feed.domain.FeedBackground;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeedBackgroundRepository extends JpaRepository<FeedBackground, Long> {

  List<FeedBackground> findAllByOrderByDisplayOrderAscIdAsc();
}
