---
paths:
  - "src/test/**"
---

# 테스트

- 서비스·도메인: JUnit 6 + Mockito (`@ExtendWith(MockitoExtension.class)`)
- 리포지토리: `RepositoryTestSupport` 상속 (`@DataJpaTest`, Testcontainers MySQL)
- 통합: `IntegrationTestSupport` 상속 (MySQL·Valkey·S3Mock 컨테이너)
- 테스트 패키지는 main과 같은 구조로 둔다. 예외로 fixture는 `<도메인>/fixture`에 둔다.
- 엔티티 테스트 데이터는 fixture로 만든다: `MemberFixture.builder().gender(FEMALE).build()`. 테스트와 관련된 값만 지정하고, fixture는 저장하지 않는다.
- 엔티티를 추가하거나 필드를 바꾸면 fixture와 `FixturePersistenceTest`도 같이 고친다.
