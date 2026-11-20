package kr.co.deepple.community.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import kr.co.deepple.common.entity.SoftDeleteBaseEntity;
import kr.co.deepple.common.exception.DeeppleException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.hibernate.annotations.SQLDelete;

@Entity
@Table(name = "self_introductions")
@SQLDelete(sql = "UPDATE self_introductions SET deleted_at = now() WHERE id = ?")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SelfIntroduction extends SoftDeleteBaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private long memberId;

  private String title;

  private String content;

  private Long imageId;

  private String imageKey;

  private boolean isOpened;

  private SelfIntroduction(
      long memberId,
      String title,
      String content,
      Long imageId,
      String imageKey,
      boolean isOpened) {
    this.memberId = memberId;
    setTitle(title);
    setContent(content);
    this.imageId = imageId;
    this.imageKey = imageKey;
    this.isOpened = isOpened;
  }

  public static SelfIntroduction write(
      long memberId, String title, String content, Long imageId, String imageKey) {
    return new SelfIntroduction(memberId, title, content, imageId, imageKey, false);
  }

  public void update(String title, String content, Long imageId, String imageKey) {
    setTitle(title);
    setContent(content);
    this.imageId = imageId;
    this.imageKey = imageKey;
  }

  public void close() {
    isOpened = false;
  }

  public void open() {
    isOpened = true;
  }

  private void setTitle(@NonNull String title) {
    if (title.isBlank()) {
      throw new DeeppleException(CommunityErrorCode.INVALID_SELF_INTRODUCTION_TITLE);
    }
    this.title = title;
  }

  private void setContent(@NonNull String content) {
    if (content.isBlank() || content.length() < 30) {
      throw new DeeppleException(CommunityErrorCode.INVALID_SELF_INTRODUCTION_CONTENT);
    }
    this.content = content;
  }
}
