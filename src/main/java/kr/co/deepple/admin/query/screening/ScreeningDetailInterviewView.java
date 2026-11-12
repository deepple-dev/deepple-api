package kr.co.deepple.admin.query.screening;

import com.querydsl.core.annotations.QueryProjection;

public record ScreeningDetailInterviewView(String question, String answer) {
  @QueryProjection
  public ScreeningDetailInterviewView {}
}
