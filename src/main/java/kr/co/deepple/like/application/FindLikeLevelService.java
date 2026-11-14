package kr.co.deepple.like.application;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import kr.co.deepple.like.application.port.in.FindLikeLevelUseCase;
import kr.co.deepple.like.application.port.out.LikeRepository;
import kr.co.deepple.like.domain.Like;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FindLikeLevelService implements FindLikeLevelUseCase {

  private final LikeRepository likeRepository;

  @Override
  @Transactional(readOnly = true)
  public Optional<String> findLikeLevel(long senderId, long receiverId) {
    return likeRepository
        .findBySenderIdAndReceiverId(senderId, receiverId)
        .map(Like::getLevel)
        .map(Enum::name);
  }

  @Override
  @Transactional(readOnly = true)
  public Map<Long, String> findLikeLevels(long senderId, Collection<Long> receiverIds) {
    if (receiverIds.isEmpty()) {
      return Map.of();
    }
    return likeRepository.findAllBySenderIdAndReceiverIdIn(senderId, receiverIds).stream()
        .filter(like -> like.getLevel() != null)
        .collect(Collectors.toMap(Like::getReceiverId, like -> like.getLevel().name()));
  }
}
