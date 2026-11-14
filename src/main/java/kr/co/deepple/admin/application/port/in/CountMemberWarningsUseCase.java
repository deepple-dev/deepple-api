package kr.co.deepple.admin.application.port.in;

import java.util.Collection;
import java.util.Map;

public interface CountMemberWarningsUseCase {
  Map<Long, Integer> countMemberWarnings(Collection<Long> memberIds);
}
