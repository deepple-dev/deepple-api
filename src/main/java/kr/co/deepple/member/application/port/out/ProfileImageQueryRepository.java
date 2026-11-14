package kr.co.deepple.member.application.port.out;

import java.util.List;
import kr.co.deepple.member.application.dto.ProfileImageView;

public interface ProfileImageQueryRepository {
  List<ProfileImageView> findByMemberId(long memberId);
}
