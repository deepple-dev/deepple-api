package kr.co.deepple.admin.application.dto;

import com.querydsl.core.annotations.QueryProjection;

public record RawScreeningDetailView(
    long screeningId, Long version, long memberId, String screeningStatus, String rejectionReason) {

  @QueryProjection
  public RawScreeningDetailView {}
}
