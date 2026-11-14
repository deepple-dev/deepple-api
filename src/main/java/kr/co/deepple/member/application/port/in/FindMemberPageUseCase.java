package kr.co.deepple.member.application.port.in;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface FindMemberPageUseCase {
  Page<MemberPageItem> findMemberPage(MemberPageSearchCondition condition, Pageable pageable);
}
