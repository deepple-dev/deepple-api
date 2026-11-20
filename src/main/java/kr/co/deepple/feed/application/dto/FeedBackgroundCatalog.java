package kr.co.deepple.feed.application.dto;

import java.util.List;
import java.util.Set;

public record FeedBackgroundCatalog(List<FeedBackgroundView> backgrounds, Set<Long> hiddenIds) {

  public List<FeedBackgroundView> visibleBackgrounds() {
    return backgrounds.stream()
        .filter(background -> !hiddenIds.contains(background.backgroundId()))
        .toList();
  }
}
