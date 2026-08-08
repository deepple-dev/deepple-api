# 유형별 이상형 조회 API 설계 (#454)

- **이슈**: #454 `GET /member/introduction/{유형}` 유형별 이상형 조회 API 추가
- **작성일**: 2026-07-20
- **선행 의존**: #453(가치관 테스트 유형 6종 확장) 완료 — `AnswerPersonalityType` 6종 존재

## 1. 목표

홈화면에서 사용자가 6개 성격 유형(`AnswerPersonalityType`) 중 하나를 선택하면, 그 유형에 해당하는 이성을 최대 3명까지 조회하고 상세로 진입할 수 있는 기능을 추가한다. 하루 1회만 무료 오픈이 가능하며, 상세 진입 시 첫 상대는 무료, 이후 상대는 하트를 소모한다.

## 2. 확정된 요구사항 (화면 플로우 반영)

- 6개 유형을 제공하되 **무료 오픈은 하루 1회**만 가능
- 한 번 오픈된 유형은 **24시간 유지 후 초기화**(오픈 시점 기준 롤링)
- 오픈된 유형의 목록은 24시간 동안 **같은 후보 3명이 고정** 노출
- **다른 유형 차단**: 오늘 한 유형을 오픈했다면 24h 내 다른 유형은 오픈 불가
- **최대 3명** 제공, 후보가 더 많으면 **최신 가입순** 상위 3명
- **조회 실패(0명)**: 목록으로 이동하지 않고 팝업만 닫힘 → 서버는 빈 목록 반환하며 **오픈을 소진하지 않음**
- **상세 진입 = 언락**: 해당 유형의 **첫 언락은 무료**, 2·3번째 언락은 **하트 소모**(순번은 언락 순서 기준)

## 3. 스코프

포함:
- `GET /member/introduction/{personalityType}` — 유형 오픈 / 목록 조회
- `POST /member/introduction/{personalityType}/{targetMemberId}` — 상세 언락(순번별 과금)
- `GET /member/introduction/personality-status` — 오픈 상태 조회(오픈된 유형 + 오픈 시각, 부수효과 없음)

제외(별도/기존 재사용):
- 프로필 상세 렌더링 자체(기존 `GET /member/{memberId}` 재사용)
- #452 문항 데이터 확정(별도 진행)

## 4. 접근 방식

**기존 소개 파이프라인 확장 (A안)** 을 채택한다. 근거: `MemberIntroductionController` / `IntroductionQueryService` / 하트 이벤트 파이프라인(`MemberIntroducedEvent` → `HeartUsageEventHandler`) / View 매핑이 이미 정비되어 재사용 이득이 크다. "유형→회원 조회" 포트만 datingexam에 신규로 추가한다.

## 5. 아키텍처 / 컴포넌트

### 5.1 datingexam 도메인 (신규 포트)
- `application/provided/PersonalityTypeMemberFinder` (신규 인터페이스)
  - `List<Long> findMemberIdsByDominantPersonalityType(long requesterId, AnswerPersonalityType type)`
  - 반환: 반대 성별 · `isProfilePublic=true` · `activityStatus=ACTIVE` · 차단/피차단 제외 필터를 적용한 회원 id를 **최신 가입순(member.id desc)** 으로 반환
- 구현: `SoulmateQueryRepositoryImpl.findSameAnswerMemberIds()` 를 템플릿으로 한 QueryDSL 조회. `dating_exam_submit_result.dominant_personality_type = :type` 조인/조건 추가.
- 노출: `SoulmateFinder` 패턴과 동일하게 `provided` 포트로 member 도메인에 제공.

> 결정: "이미 소개/매칭된 상대 제외"는 member_introductions·match 테이블에 의존하므로 datingexam이 아닌 **member 도메인**에서 적용한다(도메인 결합 최소화). datingexam 포트는 유형·성별·공개·활성·차단까지만 책임진다.

### 5.2 member/query 도메인
- `IntroductionQueryService` (또는 신규 `PersonalityIntroductionQueryService`)에 유형별 조회 유스케이스 추가:
  1. Redis 오픈 상태 확인
  2. 미오픈 시 `PersonalityTypeMemberFinder`로 후보 id 획득 → **이미 소개/매칭된 상대 제외** → 상위 3명
  3. 뷰 매핑은 기존 `findMemberIntroductionProfileViews(memberId, memberIds)` 재사용 → `List<MemberIntroductionProfileView>`
- Redis 오픈 상태 저장소(신규): 오픈 유형 + 후보 id + 언락 집합 관리.

### 5.3 member/command 도메인
- 언락(상세 진입) 유스케이스: 첫 언락 무료 / 이후 과금 분기.
  - 대상이 현재 오픈 목록(Redis)에 포함되는지 검증
  - 이미 언락됨 → 무과금(멱등) 반환
  - 언락 집합이 비어있음(첫 언락) → **무료**: `MemberIntroduction` 레코드 생성하되 하트 이벤트 미발행
  - 그 외 → **과금**: 기존 경로(`MemberIntroduction.of()` → `MemberIntroducedEvent` → `INTRODUCTION` 정책 차감)
  - 언락 성공 시 Redis 언락 집합에 targetId 추가
- `IntroductionType`에 신규 값 추가(예: `PERSONALITY`) 및 `TransactionSubtype` 대응값 추가(과금 대상이므로 free-eligible 아님).

> 구현 유의: 기존 `MemberIntroduction.of()`는 생성 시 항상 `MemberIntroducedEvent`를 발행한다. "첫 언락 무료"를 위해 이벤트를 발행하지 않는 생성 경로(무료 전용 팩토리/플래그)가 필요하다. 세부 방식은 구현 계획에서 확정.

### 5.4 presentation
- `MemberIntroductionController`(`@RequestMapping("/member/introduction")`)에 2개 엔드포인트 추가:
  - `GET /{personalityType}` → `BaseResponse<List<MemberIntroductionProfileView>>`
  - `POST /{personalityType}/{targetMemberId}` → `BaseResponse<Void>`(또는 언락된 뷰)
- 인증: `@AuthPrincipal AuthContext authContext` → `authContext.getId()`
- 라우팅: 기존 고정 경로(`/grade`,`/hobby`,`/religion`,`/city`,`/recent`,`/ideal`,`/soulmate`)가 우선 매칭되며 `AnswerPersonalityType` enum 값과 겹치지 않아 충돌 없음. 잘못된 유형 문자열은 enum 바인딩 실패 → 400.

## 6. 데이터 흐름

### 6.1 GET (오픈/조회)
```
GET /member/introduction/{type}
  └ Redis 오픈상태 조회 intro:personality:open:{memberId}
      ├ 존재 & 같은 type  → 저장된 id 3개로 View 매핑 후 반환
      ├ 존재 & 다른 type  → 409 (이미 오늘 다른 유형 오픈)
      └ 없음 → PersonalityTypeMemberFinder.findMemberIdsByDominantPersonalityType(memberId, type)
               → 이미 소개/매칭 제외 → 상위 3명
               ├ 0명 → 빈 목록 반환(Redis 저장 X, 오픈 미소진)
               └ ≥1명 → Redis 저장(type + ids, TTL 24h) → View 매핑 후 반환
```

### 6.2 POST (언락/과금)
```
POST /member/introduction/{type}/{targetMemberId}
  └ Redis 오픈상태 확인: type 일치 & targetMemberId ∈ 저장된 ids 인지 검증(아니면 400/409)
      ├ 이미 언락(unlocked set 포함) → 무과금 반환
      ├ unlocked set 비어있음(첫 언락) → 무료 레코드 생성, 이벤트 미발행
      └ 그 외 → MemberIntroduction.of() → MemberIntroducedEvent → INTRODUCTION 하트 차감
  └ unlocked set에 targetMemberId 추가
```

### 6.3 Redis 키
| 키 | 값 | TTL | 용도 |
|---|---|---|---|
| `intro:personality:open:{memberId}` | `{type, [id1,id2,id3], openedAt}` | 24h(롤링) | 오픈 상태 + 고정 목록 + 오픈 시각(`GET /personality-status`가 노출) |
| `intro:personality:unlocked:{memberId}` | Set&lt;targetId&gt; | 24h(롤링) | 첫 언락 판정 |

> 두 키의 TTL은 오픈 시점에 함께 설정하여 동일 창(window)에서 만료되게 한다.

## 7. 엣지 케이스

- **0명**: 빈 목록 반환, 오픈 미소진(Redis 미저장) — 다음 시도에서 다른/같은 유형 오픈 가능
- **다른 유형 재오픈**: 24h 내 거부(409)
- **24h 경과**: 키 만료 → 재오픈 가능(유형 무관)
- **하트 부족**: 언락 과금 시 기존 하트 부족 예외 그대로 전파(레코드/언락 미반영)
- **언락 멱등**: 같은 대상 재언락 → 무과금
- **오픈 목록에 없는 대상 언락 시도**: 거부(위변조 방지)
- **datingexam 결과 없음(제출 이력 없는 회원만 존재)**: 0명과 동일 처리

## 8. 테스트 전략

- **datingexam 쿼리** (`@DataJpaTest`): `dominantPersonalityType` + 반대성별·공개·ACTIVE·차단제외 필터, 최신순, 다양한 유형 혼재 시 정확 조회
- **오픈 게이트(서비스 단위)**: 같은 유형 재요청 시 동일 목록 / 다른 유형 거부 / 0명 미소진 / 24h 만료 후 재오픈
- **언락 과금(서비스 단위)**: 첫 언락 무료(이벤트 미발행) / 2·3번째 과금(이벤트 발행) / 멱등 / 오픈목록 외 대상 거부
- **하트 차감 통합**: `MemberIntroducedEvent` → `HeartUsageEventHandler` → `INTRODUCTION` 정책(남10/여4) 반영 확인

## 9. 미결/향후

- 상세 응답 형태(POST가 언락된 View를 돌려줄지 Void일지)는 프론트 요구에 맞춰 구현 계획에서 확정
- `IntroductionType.PERSONALITY` 네이밍 최종 확정
- 후보 다양성(랜덤) 요구가 추후 생기면 선별 전략만 교체(인터페이스 유지)
