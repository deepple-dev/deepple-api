package kr.co.deepple.member.application.dto;

import com.querydsl.core.annotations.QueryProjection;

public record ProfileImageView(Long id, String url, boolean isPrimary, int order) {
  @QueryProjection
  public ProfileImageView {}
}
