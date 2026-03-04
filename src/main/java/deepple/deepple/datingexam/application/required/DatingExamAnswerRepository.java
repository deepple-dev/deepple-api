package deepple.deepple.datingexam.application.required;

import deepple.deepple.datingexam.domain.DatingExamAnswer;
import org.springframework.data.repository.Repository;

import java.util.Collection;
import java.util.List;

public interface DatingExamAnswerRepository extends Repository<DatingExamAnswer, Long> {
    List<DatingExamAnswer> findAllByIdIn(Collection<Long> ids);
}
