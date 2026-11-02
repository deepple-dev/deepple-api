---
paths:
  - "src/test/**"
---

# 테스트

- 서비스·도메인: JUnit 6 + Mockito (`@ExtendWith(MockitoExtension.class)`)
- 리포지토리: `RepositoryTestSupport` 상속 (`@DataJpaTest`, Testcontainers MySQL)
- 통합: `IntegrationTestSupport` 상속 (MySQL·Valkey·S3Mock 컨테이너)
- 테스트 패키지는 main과 같은 구조로 둔다.
