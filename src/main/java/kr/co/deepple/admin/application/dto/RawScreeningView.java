package kr.co.deepple.admin.application.dto;

import com.querydsl.core.annotations.QueryProjection;

public record RawScreeningView(
    long screeningId, long memberId, String screeningStatus, String rejectionReason) {

  @QueryProjection
  public RawScreeningView {}
}
