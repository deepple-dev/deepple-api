package kr.co.deepple.datingexam.domain;

import kr.co.deepple.datingexam.domain.dto.DatingExamSubmitRequest;

public interface DatingExamAnswerEncoder {
    String encode(DatingExamSubmitRequest request);
}
