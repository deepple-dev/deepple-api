package kr.co.deepple.admin.application.dto;

import java.util.List;

public record ScreeningDetailView(
    long screeningId,
    long version,
    ScreeningDetailProfileView profile,
    List<ScreeningDetailProfileImageView> profileImages,
    List<ScreeningDetailInterviewView> interviews) {}
