package kr.co.deepple.feed.application.dto;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public record FeedBackgroundCatalog(List<FeedBackgroundView> backgrounds, Set<Long> hiddenIds) {

  public List<FeedBackgroundView> visibleBackgrounds() {
    return backgrounds.stream()
        .filter(background -> !hiddenIds.contains(background.backgroundId()))
        .toList();
  }

  public Optional<FeedBackgroundView> find(Long backgroundId) {
    if (backgroundId == null) {
      return Optional.empty();
    }
    return backgrounds.stream()
        .filter(background -> background.backgroundId() == backgroundId)
        .findFirst();
  }
}
