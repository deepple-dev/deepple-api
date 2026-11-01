package deepple.deepple.member.command.application.introduction.exception;

import deepple.deepple.datingexam.domain.AnswerPersonalityType;

public class PersonalityIntroductionAlreadyOpenedException extends RuntimeException {
    public PersonalityIntroductionAlreadyOpenedException(AnswerPersonalityType openedType,
        AnswerPersonalityType requestedType) {
        super("오늘은 이미 '" + openedType + "' 유형을 소개받아 다른 유형('" + requestedType + "')은 소개받을 수 없습니다.");
    }
}
