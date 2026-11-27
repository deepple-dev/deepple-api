package kr.co.deepple.datingexam.application.port.in;

import jakarta.validation.Valid;
import kr.co.deepple.datingexam.application.dto.DatingExamSubmitResponse;
import kr.co.deepple.datingexam.domain.dto.DatingExamSubmitRequest;

public interface DatingExamSubmitter {
  DatingExamSubmitResponse submitSubject(
      @Valid DatingExamSubmitRequest submitRequest, long memberId);
}
