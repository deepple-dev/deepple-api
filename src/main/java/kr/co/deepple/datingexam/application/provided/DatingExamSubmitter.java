package kr.co.deepple.datingexam.application.provided;

import kr.co.deepple.datingexam.domain.dto.DatingExamSubmitRequest;
import jakarta.validation.Valid;

public interface DatingExamSubmitter {
    void submitSubject(@Valid DatingExamSubmitRequest submitRequest, long memberId);
}
