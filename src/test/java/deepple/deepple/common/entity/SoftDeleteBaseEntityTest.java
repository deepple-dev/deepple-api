package deepple.deepple.common.entity;

import deepple.deepple.common.support.RepositoryTestSupport;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

public class SoftDeleteBaseEntityTest extends RepositoryTestSupport {

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("SoftDeleteBaseEntity를 상속받은 엔티티 isDeleted 기본값은 false")
    void saveSoftDeleteBaseEntityTest() {
        // given
        SoftDeleteTestEntity entity = new SoftDeleteTestEntity();

        // when
        boolean result = entity.isDeleted();

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("SoftDeleteBaseEntity를 상속받은 엔티티에 @SQLDelete로 soft delete 적용하고 삭제 시 isDeleted 값이 true로 변경")
    void deleteSoftDeleteBaseEntityWithSQLDeleteTest() {
        // given
        SoftDeleteTestEntity entity = new SoftDeleteTestEntity();
        entityManager.persist(entity);

        // when
        entityManager.remove(entity);
        entityManager.flush();
        SoftDeleteTestEntity deletedEntity = entityManager.find(SoftDeleteTestEntity.class,
            entity.getId());

        // then
        assertThat(deletedEntity.isDeleted()).isTrue();
    }
}