package kr.co.deepple.heart.application.port.in;

import kr.co.deepple.heart.application.dto.AdminHeartGrantRequest;

public interface AddAdminHeartUseCase {
  void addAdminHeart(long adminId, AdminHeartGrantRequest request);
}
