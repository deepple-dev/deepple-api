---
paths:
  - "build.gradle"
  - "settings.gradle"
  - "gradle/**"
---

# 빌드

- 의존성 버전은 `gradle/libs.versions.toml`(Version Catalog)에서 관리한다. `build.gradle`에서는 `libs.*`로 참조한다.
- Spring Boot가 관리하는 라이브러리는 버전을 적지 않는다.
