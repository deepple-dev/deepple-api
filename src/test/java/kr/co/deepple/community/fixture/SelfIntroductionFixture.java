package kr.co.deepple.community.fixture;

import kr.co.deepple.community.command.domain.selfintroduction.SelfIntroduction;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import static org.springframework.test.util.ReflectionTestUtils.setField;

@Setter
@Accessors(fluent = true, chain = true)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SelfIntroductionFixture {

    private Long id;
    private long memberId = 1L;
    private String title = "셀프 소개 제목";
    private String content = "안녕하세요. 저는 여행과 운동을 좋아합니다. 잘 부탁드려요!";
    private String imageUrl;
    private boolean opened;

    public static SelfIntroductionFixture builder() {
        return new SelfIntroductionFixture();
    }

    public SelfIntroduction build() {
        SelfIntroduction selfIntroduction = SelfIntroduction.write(memberId, title, content, imageUrl);
        setField(selfIntroduction, "id", id);
        setField(selfIntroduction, "isOpened", opened);
        return selfIntroduction;
    }
}
