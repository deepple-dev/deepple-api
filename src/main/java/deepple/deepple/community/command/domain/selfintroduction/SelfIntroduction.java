package deepple.deepple.community.command.domain.selfintroduction;

import deepple.deepple.common.entity.SoftDeleteBaseEntity;
import deepple.deepple.community.command.domain.selfintroduction.exception.InvalidSelfIntroductionContentException;
import deepple.deepple.community.command.domain.selfintroduction.exception.InvalidSelfIntroductionTitleException;
import deepple.deepple.member.command.domain.profileImage.exception.InvalidImageUrlException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.hibernate.annotations.SQLDelete;

@Entity
@Table(name = "self_introductions",
    indexes = @Index(name = "idx_member_id", columnList = "memberId")
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLDelete(sql = "UPDATE self_introductions SET deleted_at = now() WHERE id = ?")
public class SelfIntroduction extends SoftDeleteBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter
    private Long id;

    @Getter
    private Long memberId;

    @Getter
    private String title;

    @Getter
    private String content;

    @Getter
    @Column(name = "image_url")
    private String imageUrl;

    @Getter
    private boolean isOpened;

    private SelfIntroduction(@NonNull Long memberId, @NonNull String title, @NonNull String content, String imageUrl,
        boolean isOpened) {
        this.isOpened = isOpened;
        this.memberId = memberId;
        setTitle(title);
        setContent(content);
        setImageUrl(imageUrl);
    }

    public static SelfIntroduction write(Long memberId, String title, String content, String imageUrl) {
        return new SelfIntroduction(memberId, title, content, imageUrl, false);
    }

    public void update(String title, String content, String imageUrl) {
        setTitle(title);
        setContent(content);
        setImageUrl(imageUrl);
    }

    public void close() {
        isOpened = false;
    }

    public void open() {
        isOpened = true;
    }

    private void setTitle(@NonNull String title) {
        validateTitle(title);
        this.title = title;
    }

    private void setContent(@NonNull String content) {
        validateContent(content);
        this.content = content;
    }

    private void setImageUrl(String imageUrl) {
        if (imageUrl != null && imageUrl.isEmpty()) {
            throw new InvalidImageUrlException();
        }
        this.imageUrl = imageUrl;
    }

    private void validateContent(String content) {
        if (content.isBlank() || content.length() < 30) {
            throw new InvalidSelfIntroductionContentException();
        }
    }

    private void validateTitle(String title) {
        if (title.isBlank()) {
            throw new InvalidSelfIntroductionTitleException();
        }
    }
}
