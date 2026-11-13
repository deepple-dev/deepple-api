package kr.co.deepple.recommendation.application.dto;

import com.querydsl.core.annotations.QueryProjection;

public record InterviewAnswerQueryResult(long memberId, String content) {
  @QueryProjection
  public InterviewAnswerQueryResult {}
}
