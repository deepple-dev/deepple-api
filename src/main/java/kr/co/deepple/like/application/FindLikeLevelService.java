package kr.co.deepple.like.application;

import java.util.Optional;
import kr.co.deepple.like.application.port.in.FindLikeLevelUseCase;
import kr.co.deepple.like.application.port.out.LikeCommandRepository;
import kr.co.deepple.like.domain.Like;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FindLikeLevelService implements FindLikeLevelUseCase {

  private final LikeCommandRepository likeCommandRepository;

  @Override
  @Transactional(readOnly = true)
  public Optional<String> findLikeLevel(long senderId, long receiverId) {
    return likeCommandRepository
        .findBySenderIdAndReceiverId(senderId, receiverId)
        .map(Like::getLevel)
        .map(Enum::name);
  }
}
