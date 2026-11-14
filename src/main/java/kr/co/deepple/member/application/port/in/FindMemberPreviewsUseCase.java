package kr.co.deepple.member.application.port.in;

import java.util.Collection;
import java.util.Map;

public interface FindMemberPreviewsUseCase {
  Map<Long, MemberPreview> findMemberPreviews(Collection<Long> memberIds);
}
