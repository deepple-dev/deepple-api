package deepple.deepple.datingexam.domain;

import lombok.Getter;

@Getter
public enum AnswerPersonalityType {
    DECISIVE_INDEPENDENT("단호한 독립주의자"),
    GROWING_RUNNING_MATE("성장하는 러닝메이트"),
    DEVOTED_ROMANTIC("헌신적인 로맨티스트"),
    REALISTIC_SHELTER("현실적인 안식처"),
    STIMULATING_ADVENTURER("자극모험형"),
    RATIONAL_REALIST("이성중심형");

    public static final String SCHEMA_DESCRIPTION =
        "연애가치관 유형 - DECISIVE_INDEPENDENT: 단호한 독립주의자, GROWING_RUNNING_MATE: 성장하는 러닝메이트, "
            + "DEVOTED_ROMANTIC: 헌신적인 로맨티스트, REALISTIC_SHELTER: 현실적인 안식처, "
            + "STIMULATING_ADVENTURER: 자극모험형, RATIONAL_REALIST: 이성중심형";

    private final String description;

    AnswerPersonalityType(String description) {
        this.description = description;
    }
}
