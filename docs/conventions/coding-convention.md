# 코딩 컨벤션

실제 코드베이스에서 추출한 규칙. 새 코드는 이 문서를 따르고, 기존 코드와 충돌하면 이 문서가 기준이다.

## 1. 네이밍

### 클래스 접미사 (레이어별)

| 접미사                          | 역할                    | 위치                           |
|------------------------------|-----------------------|------------------------------|
| `{도메인}Controller`            | API 엔드포인트             | `presentation/`              |
| `{도메인}Service`               | 커맨드 애플리케이션 서비스        | `command/application/`       |
| `{도메인}QueryService`          | 조회 애플리케이션 서비스         | `query/{하위도메인}/application/` |
| `{도메인}Mapper`                | DTO ↔ 엔티티 변환 (정적 메서드) | `command/application/`       |
| `{도메인}Validator`             | 요청/도메인 검증             | `application/`               |
| `{엔티티}CommandRepository`     | 커맨드 측 리포지토리 인터페이스     | `command/domain/`            |
| `{엔티티}CommandJpaRepository`  | JPA 인터페이스             | `command/infra/`             |
| `{엔티티}CommandRepositoryImpl` | 리포지토리 구현              | `command/infra/`             |
| `{엔티티}QueryRepository`       | QueryDSL 조회 리포지토리     | `query/{하위도메인}/infra/`       |
| `{도메인}EventHandler`          | 이벤트 핸들러               | `command/infra/`             |
| `{도메인}Fetcher`               | 조회 보조 (ID 목록 등 배치 조회) | `query/.../application/`     |
| `{도메인}ExceptionHandler`      | 도메인별 예외 핸들러           | `presentation/`              |

### 메서드 네이밍

- 조회: `get{대상}()` 단일 조회, `find{대상}()` Optional 반환(리포지토리), `fetch()` 배치 조회
- 생성: `create()`, 도메인 의도가 있으면 의도 동사 사용 (`write()`, `signup()`, `request()`)
- 수정: `update()`, 상태 전이는 의도 동사 (`approve()`, `reject()`, `suspend()`)
- 삭제: `delete()`
- 검증: `validate{대상}()`, boolean 반환은 `is{속성}()`
- CRUD 동사보다 **도메인 의도를 드러내는 동사를 우선**한다. (예: `changeOpenStatus()` > `updateOpen()`)

### DTO 네이밍

- 요청: `{대상}{동작}Request` — record, 검증 어노테이션은 record 컴포넌트에 선언
- 응답: `{대상}{동작}Response` — record
- 조회 결과: `{대상}View` — record, `query/.../view/` 위치
- 검색 조건: presentation에서 받는 것은 `{대상}SearchRequest`, query 레이어 내부 조건 객체는 `{대상}SearchCondition`

## 2. 컨트롤러

- 응답은 항상 `ResponseEntity<BaseResponse<T>>`로 감싼다.
    - 데이터 없음: `BaseResponse.from(StatusType.OK)`
    - 데이터 포함: `BaseResponse.of(StatusType.OK, data)`
- 요청 검증: `@Valid @RequestBody` (바디), `@Valid @ModelAttribute` (쿼리 파라미터)
- 인증 정보: `@AuthPrincipal AuthContext authContext` 파라미터로 받고 `authContext.getId()` 사용. 토큰을 직접 파싱하지 않는다.
- URL 규칙:
    - 리소스는 복수형 (`/admin/screenings`), 단어 구분은 kebab-case (`/profile-exchange`)
    - 관리자 API는 `/admin/{리소스}` 프리픽스
    - 상태 변경 동작은 `/{id}/{동작}` (예: `POST /admin/screenings/{id}/approve`)
- Swagger: `@Tag(name = "...")`, `@Operation(summary = "...")` 한국어로 작성

## 3. 예외 처리

- 모든 커스텀 예외는 `RuntimeException` 상속, 이름은 `{구체적상황}Exception`
- 예외 메시지는 한국어, 생성자에서 고정 메시지 지정
- 위치 규칙:
    - 도메인 규칙 위반 (불변식, 상태 전이 불가 등) → `command/domain/.../exception/`
    - 유스케이스 오류 (대상 없음, 중복 등) → `command/application/`
- 처리 규칙:
    - 공통 예외는 `GlobalExceptionHandler`가 처리
    - 도메인 특화 응답이 필요하면 `{도메인}ExceptionHandler` + `@Order(Ordered.HIGHEST_PRECEDENCE)`
    - 에러 코드는 `StatusType` enum에 추가 (HTTP status + 커스텀 code + 메시지)
- 예외를 빈 catch로 삼키지 않는다. 복구 불가하면 로그(원인 포함) + 재던지기 또는 실패 기록을 남긴다.

## 4. Lombok

| 대상            | 어노테이션 조합                                                                                                                   |
|---------------|----------------------------------------------------------------------------------------------------------------------------|
| 엔티티           | `@Entity` `@Getter` `@NoArgsConstructor(access = PROTECTED)` + private 생성자에 `@Builder`                                     |
| 임베디드 VO       | `@Embeddable` `@Getter` `@EqualsAndHashCode` `@NoArgsConstructor(access = PROTECTED, force = true)` + private 생성자 + 정적 팩토리 |
| 서비스/리포지토리/핸들러 | `@RequiredArgsConstructor` (+ 필요 시 `@Slf4j`)                                                                               |
| Mapper        | `@NoArgsConstructor(access = PRIVATE)` + `public static` 메서드만                                                              |
| DTO           | Lombok 사용 금지 — record 사용                                                                                                   |

- `@Setter`는 사용하지 않는다. 상태 변경은 의도가 드러나는 도메인 메서드로.
- `@Data`, `@AllArgsConstructor`(public)는 사용하지 않는다.

## 5. 정적 팩토리 메서드

- 단일 인자 변환: `from(x)`
- 다중 인자 생성: `of(a, b)`
- 변형이 있으면 의도를 접미사로: `fromRaw()`, `fromHashed()`
- VO·이벤트·DTO는 생성자를 private으로 막고 정적 팩토리만 공개한다.

## 6. 엔티티 → DTO 변환

- 변환은 **application 레이어의 Mapper 클래스**에서 한다. 컨트롤러에서 변환하지 않는다.
- query 측은 QueryDSL Projections(`new Q{View}(...)`)로 리포지토리에서 View를 직접 생성한다.

## 7. 언어

- 클래스/메서드/필드/상수명: 영어
- 주석, 로그 메시지, 예외 메시지, Swagger 문서: 한국어
- 로그 포맷: `log.warn("[토큰 인증 실패] {}", ...)` — 대괄호 컨텍스트 프리픽스
- Javadoc은 쓰지 않는다. 이름으로 의도가 드러나지 않으면 이름을 고친다.
