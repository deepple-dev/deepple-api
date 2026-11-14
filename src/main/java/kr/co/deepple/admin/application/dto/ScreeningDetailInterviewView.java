package kr.co.deepple.admin.application.dto;

import com.querydsl.core.annotations.QueryProjection;

public record ScreeningDetailInterviewView(String question, String answer) {
  @QueryProjection
  public ScreeningDetailInterviewView {}
}
