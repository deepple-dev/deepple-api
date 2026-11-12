package kr.co.deepple.member.query.profileimage.view;

import com.querydsl.core.annotations.QueryProjection;

public record ProfileImageView(Long id, String url, boolean isPrimary, int order) {
  @QueryProjection
  public ProfileImageView {}
}
