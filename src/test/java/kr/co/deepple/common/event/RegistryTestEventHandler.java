package kr.co.deepple.common.event;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

@Component
public class RegistryTestEventHandler {

  private final Set<UUID> failingEventIds = ConcurrentHashMap.newKeySet();
  private final Map<UUID, AtomicInteger> callCounts = new ConcurrentHashMap<>();
  private final Map<UUID, String> receivedNotes = new ConcurrentHashMap<>();

  @ApplicationModuleListener
  public void handle(RegistryTestEvent event) {
    callCounts.computeIfAbsent(event.id(), id -> new AtomicInteger()).incrementAndGet();
    if (failingEventIds.contains(event.id())) {
      throw new IllegalStateException("테스트용 실패. eventId=" + event.id());
    }
    receivedNotes.put(event.id(), String.valueOf(event.note()));
  }

  public void failFor(UUID eventId) {
    failingEventIds.add(eventId);
  }

  public void succeedFor(UUID eventId) {
    failingEventIds.remove(eventId);
  }

  public int callCount(UUID eventId) {
    AtomicInteger count = callCounts.get(eventId);
    return count == null ? 0 : count.get();
  }

  public String receivedNote(UUID eventId) {
    return receivedNotes.get(eventId);
  }
}
