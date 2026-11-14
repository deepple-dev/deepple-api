package kr.co.deepple.interview.application.port.out;

import java.util.Optional;
import kr.co.deepple.interview.application.dto.AdminInterviewQuestionView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AdminInterviewQuestionQueryRepository {
  Page<AdminInterviewQuestionView> findAdminInterviewQuestionPage(Pageable pageable);

  Optional<AdminInterviewQuestionView> findAdminInterviewQuestionById(long id);
}
