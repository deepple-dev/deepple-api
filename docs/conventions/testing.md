# 테스트 작성 규칙

## 1. 공통 규칙

- 테스트 클래스명: `{대상클래스}Test`
- 모든 테스트 메서드에 `@DisplayName` 필수, **한국어로 행위와 기대 결과를 서술** (예: `"회원가입 시 유효한 요청이면 회원가입할 수 있습니다."`)
- 메서드명은 영어 camelCase로 시나리오 요약 (예: `canSignupWhenRequestIsValid`)
- 구조 주석 필수: `// given`, `// when`, `// then` (붙여 쓸 경우 `// when & then`)
- 기능 단위 그룹화는 `@Nested` + `@DisplayName` 사용
- 테스트 패키지는 main과 동일한 구조를 따른다

## 2. 표준 라이브러리·패턴

- 어설션: **AssertJ만 사용** (`assertThat`, `assertThatThrownBy`, `assertThatCode`). JUnit `assertThrows`/`assertEquals` 금지.
- 모킹: **Mockito `when().thenReturn()` 스타일로 통일.** BDDMockito(`given().willReturn()`)는 사용하지 않는다.
- 예외 검증:
  ```java
  assertThatThrownBy(() -> service.doSomething(request))
      .isInstanceOf(SomethingException.class)
      .hasMessageContaining("핵심 메시지");
  ```

## 3. 테스트 종류별 패턴

### 도메인 단위 테스트

- 의존성·Spring 컨텍스트 없이 순수 객체로 검증
- 이벤트를 발행하는 엔티티는 `MockEventsExtension` 적용 (`Events.raise()` 모킹)

### 서비스 테스트

- `@ExtendWith(MockitoExtension.class)` + `@Mock` 의존성 + `@InjectMocks` 대상
- Spring 컨텍스트를 띄우지 않는다 (`@SpringBootTest` 금지)
- ID 등 빌더로 설정 불가한 필드는 `ReflectionTestUtils.setField()` 사용

### 리포지토리 테스트

- `@DataJpaTest` + `@Import({QueryDslConfig.class, {대상}QueryRepository.class})`
- 데이터 준비는 `TestEntityManager`로 `persist → flush → clear` 후 검증
- 이벤트 발행 엔티티를 persist하면 `@ExtendWith(MockEventsExtension.class)` 적용

### 통합 테스트

- `IntegrationTestSupport` 상속 (`@SpringBootTest` + Redis/S3 테스트 설정 포함)
- 전체 컨텍스트가 필요한 경우에만 사용 (필터, 외부 연동 등)

## 4. 테스트 데이터

- 생성은 엔티티 빌더 또는 정적 팩토리를 직접 사용
- 같은 클래스에서 반복되면 private 헬퍼 메서드로 추출, 이름은 `create{대상}(가변 속성...)`
- 중앙 픽스처 클래스는 아직 없음 — 도입 시 ROADMAP 15번에서 표준화

## 5. 환경

- 테스트 DB: H2 인메모리 (`src/test/resources/application.yml`), Flyway 비활성화
- 알려진 한계: H2는 MySQL과 락·인덱스 동작이 다름 — 동시성·쿼리 동작 검증은 ROADMAP 10번(Testcontainers) 이후 실DB 컨테이너로 전환 예정

## 6. 무엇을 테스트하는가

- 커맨드 서비스: 정상 흐름 + 검증 실패 + 상태별 분기 (정지/탈퇴 회원 등)
- 도메인 객체: 불변식 (null/빈 값 생성 차단), 상태 전이 규칙, 이벤트 발행 여부
- 쿼리 리포지토리: 필터·정렬·페이징 조건별 결과
- 컨트롤러: 상태코드·검증 에러 응답 계약 (ROADMAP 12번에서 도입)
- 재화(하트)·결제·매칭 로직은 동시성 테스트 필수 (ROADMAP 14번)
