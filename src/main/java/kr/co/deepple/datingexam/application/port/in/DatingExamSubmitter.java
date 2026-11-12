package kr.co.deepple.datingexam.application.port.in;

import jakarta.validation.Valid;
import kr.co.deepple.datingexam.domain.dto.DatingExamSubmitRequest;

public interface DatingExamSubmitter {
  void submitSubject(@Valid DatingExamSubmitRequest submitRequest, long memberId);
}
