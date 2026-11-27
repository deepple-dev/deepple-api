package kr.co.deepple.interview.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import kr.co.deepple.interview.domain.InterviewCategory;

public record InterviewQuestionSaveRequest(
    @Schema(description = "인터뷰 질문 내용", example = "내가 생각하는 나의 장점과 단점")
        @NotBlank(message = "인터뷰 질문 내용은 필수입니다.")
        String questionContent,
    @Schema(implementation = InterviewCategory.class) @NotBlank(message = "인터뷰 질문 카테고리는 필수입니다.")
        String category,
    @Schema(description = "공개 여부", example = "true") @NotNull(message = "공개 여부는 필수입니다.")
        Boolean isPublic,
    @Schema(
            description = "질문 설명. 질문 카드와 답변 입력칸 안내에 쓰인다. 수정할 때 보내지 않으면 기존 값을 그대로 두고, 빈 문자열이면 지운다",
            example = "어떤 일을 하는지, 일하며 즐거운 순간을 적어 보세요")
        @Size(max = 100, message = "인터뷰 질문 설명은 100자 이하여야 합니다.")
        String description) {}
