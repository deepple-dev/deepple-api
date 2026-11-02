package deepple.deepple.common.entity;


import deepple.deepple.common.support.RepositoryTestSupport;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

public class BaseEntityTest extends RepositoryTestSupport {

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("BaseEntity를 상속받은 엔티티 저장 테스트")
    void saveBaseEntityTest() {
        // given
        String name = "test";
        TestEntity entity = new TestEntity(name);

        // when
        entityManager.persist(entity);
        entityManager.flush();
        entityManager.clear();

        // then
        TestEntity savedEntity = entityManager.find(TestEntity.class, entity.getId());
        assertThat(savedEntity.getId()).isEqualTo(entity.getId());
        assertThat(savedEntity.getCreatedAt()).isEqualTo(savedEntity.getUpdatedAt());
    }

    @Test
    @DisplayName("BaseEntity를 상속받은 엔티티 수정 테스트")
    void updateBaseEntityTest() {
        // given
        String name = "test";
        TestEntity entity = new TestEntity(name);
        entityManager.persist(entity);
        entityManager.flush();
        entityManager.clear();
        TestEntity savedEntity = entityManager.find(TestEntity.class, entity.getId());
        LocalDateTime createdAt = savedEntity.getCreatedAt();
        LocalDateTime updatedAt = savedEntity.getUpdatedAt();

        // when
        savedEntity.setName("updated");
        entityManager.flush();
        entityManager.clear();

        // then
        TestEntity updatedEntity = entityManager.find(TestEntity.class, entity.getId());
        assertThat(updatedEntity.getId()).isEqualTo(savedEntity.getId());
        assertThat(updatedEntity.getCreatedAt()).isEqualTo(createdAt);
        assertThat(updatedEntity.getUpdatedAt()).isAfter(updatedAt);
    }
}
