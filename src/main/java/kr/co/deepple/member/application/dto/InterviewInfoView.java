package kr.co.deepple.member.application.dto;

import com.querydsl.core.annotations.QueryProjection;

public record InterviewInfoView(Long questionId, String title, String content) {
  @QueryProjection
  public InterviewInfoView {}
}
