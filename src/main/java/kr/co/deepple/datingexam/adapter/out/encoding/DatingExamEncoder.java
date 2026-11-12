package kr.co.deepple.datingexam.adapter.out.encoding;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import kr.co.deepple.datingexam.domain.DatingExamAnswerEncoder;
import kr.co.deepple.datingexam.domain.dto.DatingExamSubmitRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
@Slf4j
public class DatingExamEncoder implements DatingExamAnswerEncoder {

  private static final String ENCODED_ANSWERS_KEY = "as";
  private static final String ENCODED_QUESTION_ID_KEY = "q";
  private static final String ENCODED_ANSWER_ID_KEY = "a";
  private final ObjectMapper objectMapper;

  @Override
  public String encode(DatingExamSubmitRequest request) {
    List<Map<String, Object>> answers =
        request.answers().stream()
            .sorted(Comparator.comparingLong(answer -> answer.questionId()))
            .map(answer -> {
              Map<String, Object> linkedHashMap = new LinkedHashMap<>();
              linkedHashMap.put(ENCODED_QUESTION_ID_KEY, answer.questionId());
              linkedHashMap.put(ENCODED_ANSWER_ID_KEY, answer.answerId());
              return linkedHashMap;
            })
            .toList();

    Map<String, Object> resultMap = new LinkedHashMap<>();
    resultMap.put(ENCODED_ANSWERS_KEY, answers);

    return objectMapper.writeValueAsString(resultMap);
  }
}
