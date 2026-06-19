# Deepple API 개선 로드맵

프로덕션 레벨 이상의 서비스를 목표로 하는 전체 개선 계획.
전체 흐름: **기반 → 테스트 → 경계 → 성능 → 운영 → 보안 → 인프라 → MSA → 데이터 → 프로덕트 → 스케일**

## 진행 원칙

- 각 Phase는 다음 Phase의 기반이 되도록 배치한다. 단, Phase 9~10(데이터·프로덕트)은 Phase 8(MSA)과 의존성이 약해 순서를 바꿔 진행할 수 있다.
- 핵심 의존 관계:
    - MQ 도입(47)은 아웃박스(19)가 선행 — 아웃박스를 먼저 깔면 MQ 전환이 "전송로 교체"로 단순해진다.
    - 추천 알고리즘 고도화(57)는 데이터 분석 파이프라인(55)이 선행 — 측정 없는 알고리즘 개선은 검증할 수 없다.
    - 서비스 분리(46~52)는 Spring Modulith 경계 검증(17~18) + IaC(43) + 분산 추적/중앙 로그(29~30)가 선행.
- 모든 성능·품질 개선은 측정(before) → 개선 → 재측정(after)으로 수치를 남긴다.

---

## Phase 1 — 기반: 문서·컨벤션·품질 게이트

- [ ] **1. 문서 체계 구축 (docs/)** — 코딩 컨벤션, 아키텍처 규칙, 테스트 작성 규칙, Git/PR 규칙을 문서화. AI(Claude)와 사람이 동일한 기준으로 작업하는 기반. Git/PR 규칙
  문서는 4번(브랜치 전략 명문화)을 포함하여 함께 작성.
- [ ] **2. CLAUDE.md 재정비** — docs/ 문서를 참조하도록 연결하고, 도메인별 주의사항·금지 패턴을 추가.
- [ ] **3. AI 하네스 구성** — 프로젝트 전용 스킬/에이전트 정의(테스트 작성, 마이그레이션 검증 등)로 반복 작업 위임 체계화.
- [ ] **4. Git 브랜치 전략 명문화 (전략 확정 완료, 2026-06-11 / 1번에 포함하여 진행)** — 간소화 Git Flow로 확정: 현행 구조(feat/* → develop → main) 유지,
  release/* 브랜치는 생략(dev 서버가 QA 환경 역할, 필요해지면 추가). 명문화·적용할 규칙 5가지: ① feat/* → develop은 항상 PR 경유(CI 통과 기록 필수, 로컬 머지 push
  금지) ② develop → main 머지는 기능 1~3개 단위로 작고 자주 ③ main 머지마다 SemVer 태그(v1.4.0) — prod 배포 내용 추적의 기준 ④ hotfix/*는 main에서 분기 →
  main 머지 + 패치 태그 → develop back-merge ⑤ main·develop 브랜치 보호(PR + CI 통과 필수). 릴리스 노트 자동화는 5번과 연계.
- [ ] **5. 릴리스 자동화** — 태그 기반 릴리스 노트 자동 생성(release-drafter 등). 프로덕션 배포에 "무엇이 나갔는지" 기록이 남는 체계.
- [ ] **6. Spotless 도입** — 코드 포맷 자동화. 리뷰에서 스타일 논쟁 제거, diff 정리.
- [ ] **7. Jacoco + 커버리지 게이트** — 현재 커버리지 측정 자체가 없음. 측정 없이는 개선을 증명할 수 없으므로 모든 테스트 작업의 선행 조건.
- [ ] **8. PR 템플릿 + 커밋 컨벤션 명문화** — 기존 `feat:` 스타일 공식화, PR 체크리스트 추가.
- [ ] **9. Dependabot/Renovate 도입** — 취약 의존성 자동 감지·업데이트 PR. 프로덕션 서비스의 필수 위생.

## Phase 2 — 테스트: 안전망 구축

- [ ] **10. Testcontainers 통합 테스트 환경** — 실제 MySQL·Redis 컨테이너로 테스트. 실DB 동작(인덱스, 락, 타임존)과의 차이 제거. 이후 모든 리팩토링의 안전망.
- [ ] **11. auth 도메인 테스트 보강 + /admin 권한 체크 추가** — 현재 테스트 3개. JWT 발급/갱신/만료, 토큰 필터 우회 경로 검증. 인증 사고가 가장 치명적이므로 테스트 1순위. 현재
  role이 발급만 되고 검사되지 않아 일반 사용자 토큰으로 /admin API 호출이 가능한 상태이므로, TokenFilter에 /admin/** 경로 ADMIN role 검사를 추가하고 USER 토큰 접근 시
  403 응답 테스트까지 함께 작성. (Spring Security 정식 전환은 38번)
- [ ] **12. 컨트롤러 테스트 도입** — 현재 0개. API 계약(상태코드, 검증, 에러 응답) 검증.
- [ ] **13. API 문서 자동화 (REST Docs 연계)** — 테스트에서 문서를 생성해 문서와 실동작의 불일치를 원천 차단.
- [ ] **14. 동시성 테스트** — 하트 차감·분산 락(RedissonLockRepository)이 실제 동시 요청을 막는지 멀티스레드 테스트로 검증. 재화 도메인의 동시성 버그는 곧 금전 사고. 점검에서
  발견된 하트 차감 race condition(D2)과 매칭 중복 생성(D4)을 재현하는 테스트 작성 후 수정까지 포함.
- [ ] **15. 테스트 픽스처/빌더 표준화** — 도메인 객체 생성 헬퍼 표준화로 테스트 작성 비용 절감.
- [ ] **16. 뮤테이션 테스트 (PIT, 선택)** — 테스트가 실제로 버그를 잡는지 검증. heart/payment 핵심 도메인에만 적용해도 충분.

## Phase 3 — 아키텍처: 경계 정리

- [ ] **17. Spring Modulith 도입 + 위반 베이스라인 측정** — 모듈 경계를 테스트로 강제. 현재 크로스 도메인 import 40건을 실패 목록으로 가시화.
- [ ] **18. 도메인 경계 위반 해소 (40건 → 0건)** — 다른 도메인의 entity/repository 직접 참조를 이벤트 또는 공개 인터페이스로 교체. MSA 전환의 실질적 선행 조건.
- [ ] **19. Event Publication Registry (아웃박스)** — 커밋 후 핸들러 실패 시 이벤트 유실, @TransactionalEventListener가 트랜잭션 없는 발행처에서 조용히
  미실행되는 구조적 문제(D6) 해결. Modulith의 이벤트 영속화로 알림 누락·잔액 불일치 원천 차단.
- [ ] **20. 이벤트 재시도·실패 정책 (DLQ 개념)** — 핸들러 실패 시 재시도 횟수·백오프·최종 실패 기록 표준화. 현재 실패를 빈 catch로 삼키는 핸들러들(D1:
  HeartPurchasedEventHandler, MemberEventHandler, ScreeningEventHandler, BlockEventHandler, MatchEventHandler)의 실패 처리 정비
  포함.
- [ ] **21. 예외·에러 응답 표준화 점검** — 전 도메인 에러 코드 체계, 예외 계층 일관성 점검.

## Phase 4 — 성능: 측정과 개선

- [ ] **22. 쿼리 가시화 (p6spy/슬로우 쿼리 감지)** — 쿼리 수·실행시간 가시화. N+1을 수치로 잡는 도구.
- [ ] **23. k6 성능 테스트 사이클 완성 + 베이스라인 기록** — 기존 k6 시나리오 확장, p95/RPS 베이스라인 기록. 모든 성능 개선의 기준점.
- [ ] **24. N+1·쿼리 비효율 제거** — fetch join, batch size, QueryDSL 프로젝션으로 개선하고 before/after 수치 확보. 점검에서 확인된 우선 대상(D9):
  IntroductionMemberIdFetcher/TodayCardMemberIdFetcher의 호출당 고정 5쿼리, IntroductionQueryRepository(77-105)의 hobbies join
  카테시안 곱 + 메모리 GroupBy, TodayCardService(31-50)의 메모리 필터링(isActive를 DB where로), MatchService.findNickname()의 반복 Member
  조회.
- [ ] **25. 캐싱 전략 수립 (@Cacheable + 무효화 설계)** — 현재 Redis는 토큰·인증코드 저장에만 사용. 프로필 등 읽기 많고 변경 적은 데이터에 선언적 캐싱 적용.
- [ ] **26. 커넥션 풀·타임아웃 튜닝 + OSIV 비활성화** — HikariCP, 쿼리/Redis 타임아웃을 부하 테스트 결과 기반으로 조정. 현재 OSIV 기본값(true)으로 요청 종료까지 커넥션 점유
  중(D10) — open-in-view: false 명시 전환(트랜잭션 밖 lazy 접근 정리 필요하므로 테스트 안전망 이후). 장애 시 스레드 고갈 연쇄 붕괴 방지.
- [ ] **27. 인덱스 점검 (실행 계획 기반)** — 부하 상태에서 슬로우 쿼리 실행 계획 확인 후 Flyway로 인덱스 반영. 점검에서 확인된 누락 인덱스(D8) 우선 적용:
  heart_transactions(member_id) — 거래내역 조회 풀스캔, blocks(blocker_id) — 추천/매칭 조회마다 호출, likes(sender_id) — 보낸 좋아요 조회.

## Phase 5 — 운영: 관측성과 안정성

- [ ] **28. 구조화 로깅 (logback-spring.xml + JSON + MDC traceId)** — 요청별 traceId를 모든 로그에 자동 포함. 요청 단위 추적으로 운영 디버깅 효율화.
- [ ] **29. 분산 추적 (Micrometer Tracing)** — 컨트롤러→서비스→DB→Redis 구간별 소요시간 추적. MSA 전 필수 선행.
- [ ] **30. 중앙 로그 수집 (Loki/ELK/CloudWatch Logs Insights)** — 컨테이너가 여러 개가 되는 순간 서버 접속 로그 확인은 불가능. MSA 필수 선행.
- [ ] **31. Grafana 대시보드 + 알림 규칙** — RPS, p95, 에러율, JVM, DB 풀 대시보드와 임계치 Slack 알림. Prometheus 메트릭은 이미 노출 중.
- [ ] **32. 에러 트래킹 (Sentry)** — 프로덕션 예외 실시간 수집·그룹핑·알림.
- [ ] **33. SLO 정의 + 런북 작성** — 가용성·응답시간 목표 수치와 장애 유형별 대응 절차 문서. 운영 성숙도의 대표 산출물.
- [ ] **34. Resilience4j (서킷브레이커·재시도·타임아웃)** — Firebase, App Store API, SMS 등 외부 의존성 장애의 전파 차단.
- [ ] **35. Rate Limiting + 멱등성 키** — 인증 SMS 발송·결제 요청의 어뷰징과 중복 처리 방지.
- [ ] **36. 무중단 DB 마이그레이션 규칙 (expand-contract)** — 배포 중 구버전·신버전 공존 시에도 안전한 마이그레이션 규칙. Blue-Green 배포에서 컬럼 drop으로 인한 장애
  방지.
- [ ] **37. 백업·복구 검증 (DR 훈련)** — RDS 스냅샷 복구 절차 문서화 + 정기 복구 테스트. 복구해본 적 없는 백업은 백업이 아니다.

## Phase 6 — 보안·컴플라이언스

- [ ] **38. Spring Security 정식 도입 + 인가 체계** — 1차 role 검사(11번)를 검증된 프레임워크 기반으로 전환. SecurityFilterChain +
  authorizeHttpRequests로 URL 기반 인가(/admin/** → ADMIN), @EnableMethodSecurity + @PreAuthorize로 메서드 수준 인가, TokenFilter의
  SecurityContext 통합. 보안 헤더, refresh token rotation(갱신 race·재사용 감지 부재 D7 해결), JWT 시크릿 로테이션, CORS 화이트리스트 전환(D5: 현재 모든
  Origin echo + credentials 허용) 포함. 인증/인가가 코드 레벨에서 분리되어 이후 인증 서버 분리(46) 시 토큰 발급만 인증 서버로 이동하고 인가 설정은 각 서비스에 유지됨.
- [ ] **39. PII 보호** — 전화번호·실명 컬럼 암호화 또는 마스킹, 관리자 조회 감사 로그, 데이터 보존·파기 정책. 데이팅 서비스 최대 리스크.
- [ ] **40. 휴면·탈퇴 처리 자동화** — 개인정보보호법상 휴면 계정 분리보관/파기, 탈퇴 데이터 파기 스케줄. Spring Batch(54) 활용처.
- [ ] **41. CI 보안 스캔** — 의존성 취약점(Trivy/OWASP Dependency-Check), 컨테이너 이미지, 시크릿 유출(gitleaks) 스캔.
- [ ] **42. 시크릿 관리 개선** — .env 파일 → AWS Secrets Manager/Parameter Store 이전. IaC(43)와 함께 진행하면 효율적.

## Phase 7 — 인프라: IaC와 배포 고도화

- [ ] **43. IaC 도입 (Terraform, dev 환경부터)** — 수동 구성된 AWS 리소스(ECS, ECR, EC2, RDS, S3) 코드화. 환경 복제·DR·서비스 분리의 전제 조건.
- [ ] **44. 환경 패리티 정리** — local/dev/prod 구성 차이 최소화. "로컬에서는 됐는데" 문제 제거.
- [ ] **45. 배포 고도화** — 배포 후 에러율 기반 자동 롤백, 카나리 검토. 메트릭(31) 연동.

## Phase 8 — MSA 전환

- [ ] **46. 서비스 분리: 인증 → 관리자 → 메인** — Modulith로 검증된 경계를 따라 분리. 인증(무상태에 가깝고 의존 단순)부터, 배포 주기가 다른 관리자, 메인 순.
- [ ] **47. MQ 도입 (SQS/SNS 또는 Kafka)** — 서비스 분리 후 로컬 이벤트는 프로세스 간 전달 불가. 아웃박스(19)가 발행 측, MQ가 전송로. 운영 부담 최소화는 SQS/SNS, 이벤트
  재생·스트림 처리는 Kafka(MSK). 단계적으로 SQS → 필요 시 Kafka 권장.
- [ ] **48. API Gateway** — 분리된 서비스 앞단에서 라우팅·인증 검증·Rate Limit 일원화. 인증 전파 설계(게이트웨이 JWT 검증 후 내부 전달)와 한 묶음.
- [ ] **49. 서비스 간 통신 표준 + 계약 테스트** — 동기(REST/gRPC) vs 비동기(MQ) 선택 기준 문서화. Spring Cloud Contract/Pact로 서비스 간 API 계약을 CI에서
  검증.
- [ ] **50. 분산 트랜잭션 — Saga 패턴** — 결제→하트 지급→알림처럼 여러 서비스에 걸치는 흐름의 보상 트랜잭션 설계.
- [ ] **51. 구성 관리 중앙화** — 서비스별 설정 분산 관리(Parameter Store/Config Server). 시크릿 관리(42)의 확장.
- [ ] **52. 컨테이너 오케스트레이션 고도화 (EKS 전환, 선택)** — 서비스 3개 이상 시 검토. 운영 부담 증가를 감수할 가치가 있을 때만.

## Phase 9 — 데이터·배치

- [ ] **53. Read Replica 분리 (CQRS의 물리적 완성)** — 현재 code-level CQRS이나 query도 같은 DB 사용. 조회를 리플리카로 분리해 매칭/피드 조회 부하를 본DB에서
  격리.
- [ ] **54. Spring Batch 도입** — 휴면 처리(40), 매칭 만료, 일별 통계 집계, 미사용 하트 정리 등 정기 작업의 표준 실행 기반. 실패 시 재시작 지점 관리 포함.
- [ ] **55. 데이터 분석 파이프라인** — 행동 이벤트 수집 → 웨어하우스(Redshift/BigQuery) → 지표 대시보드(가입 전환율, 매칭 성사율, 리텐션, 하트 소비 패턴). 추천 고도화(57)의
  데이터 기반.
- [ ] **56. CDC (Debezium, 선택)** — DB 변경을 이벤트 스트림으로. 검색 인덱스 동기화(59)·웨어하우스 적재의 고급 해법. Kafka 도입 시에만 의미 있음.

## Phase 10 — 프로덕트 내부 고도화

- [ ] **57. 추천/매칭 알고리즘 고도화** — 단계적 접근: ① 룰 기반 정리(선호 조건 필터, 차단/신고 제외, 재노출 방지) → ② 점수 기반 랭킹(활동성, 응답률, 프로필 완성도, 상호 선호 일치도
  가중합, A/B 테스트로 검증) → ③ 협업 필터링/임베딩 기반(좋아요 이력 학습). 데이팅 서비스의 핵심 경쟁력. 55번 선행 필요.
- [ ] **58. A/B 테스트 + Feature Flag 체계** — 추천 알고리즘·기능 실험을 안전하게 배포/롤백. 57번과 한 묶음.
- [ ] **59. 검색 도입 (Elasticsearch/OpenSearch)** — 관리자 회원 검색, 커뮤니티 글 검색. RDB LIKE 검색 한계 도달 시점에.
- [ ] **60. 실시간 채팅 고도화 (WebSocket/STOMP)** — 매칭 후 커뮤니케이션이 폴링 기반이라면 WebSocket 전환, 읽음 처리·접속 상태 표시. (현재 구현 방식 확인 필요)
- [ ] **61. 이미지 파이프라인** — 프로필 사진 리사이징(썸네일), CloudFront CDN, AI 검수(부적절 이미지 자동 필터)로 admin 수동 심사(screening) 부하 절감.
- [ ] **62. 어뷰징·사기 탐지** — 허위 프로필 패턴 탐지(가입 패턴, 디바이스 중복), 신고 누적 자동 제재 규칙. 데이팅 서비스 신뢰도의 핵심.
- [ ] **63. 푸시 알림 고도화** — 세그먼트 발송, 야간 발송 제한, 발송량 제어(Firebase 쿼터), 알림 오픈율 측정.
- [ ] **64. 결제 도메인 고도화** — App Store·Google Play 영수증 검증은 구현되어 있음. 서버 웹훅 기반 환불·구독 상태 동기화(App Store Server Notifications,
  Google Play RTDN), 결제 요청 멱등성, 영수증 재사용 공격 방어 점검.

## Phase 11 — 스케일·신뢰성 (장기)

- [ ] **65. 부하 기반 오토스케일링 정교화** — CPU 기준이 아닌 RPS/큐 깊이 기반 스케일링 정책.
- [ ] **66. DB 파티셔닝/샤딩 검토** — 알림·좋아요처럼 무한 증가하는 테이블의 파티셔닝부터. 샤딩은 실제 한계 도달 시.
- [ ] **67. 카오스 엔지니어링** — dev 환경에서 Redis 다운·DB 페일오버 시뮬레이션으로 서킷브레이커(34)의 실동작 검증.
- [ ] **68. 비용 최적화 (FinOps)** — 태깅 기반 비용 가시화, 인스턴스 적정화. IaC(43) 기반 위에서.

---

## 부록 — 코드 점검 발견 결함 (2026-06-11)

코드베이스 전수 점검(보안, 정합성·동시성, JPA/쿼리, 일반 버그)에서 발견된 구체적 결함 목록.
각 결함은 연관된 로드맵 항목에서 함께 처리하거나, 독립 수정 항목으로 진행한다.

### 심각도 높음

- [ ] **D1. 이벤트 핸들러 실패 무대응 (하트 지급 유실 가능)** — `HeartPurchasedEventHandler`(빈 catch, TODO만 존재): 결제 완료 후 하트 지급 실패 시 기록 없이
  유실. `MemberEventHandler`(환불): 로그만 남김. `ScreeningEventHandler`, `BlockEventHandler`, `MatchEventHandler`: 예외 삼킴 — 신고로
  인한 차단 실패가 조용히 누락될 수 있음. → 로드맵 20번에서 처리.
- [ ] **D2. 하트 차감 race condition** — `HeartUsagePolicyService.useHeart()`(라인 27-36): 잔액 조회→차감 사이 동시 요청 보호 없음. Member
  엔티티에 @Version 없음, 비관적 락·분산 락 미사용. 동시 요청 시 차감 유실 가능. → 로드맵 14번에서 재현 테스트 후 수정 (@Version 또는 비관적 락).
- [ ] **D3. 회원 상태 검증 누락 (정지 회원이 기능 사용 가능)** — `LikeSendService.validateMember()`(라인 42-65): receiver만 검사, sender 상태
  미검사 → 정지 회원이 좋아요 전송 가능. `MatchService.request()`: 요청자 상태 검증 없음(approve 시점에만 검사). `OrderService.processReceipt()`:
  정지/탈퇴 회원도 결제 처리됨. → 독립 수정 항목, Phase 2 테스트 안전망 구축과 함께 처리 권장.
- [ ] **D4. matches 테이블 unique 제약 부재** — likes는 (sender_id, receiver_id) unique가 있으나 matches는 없음. named lock이 있지만 A→B와
  B→A 동시 요청은 서로 다른 락 키를 잡아 중복 매칭 생성 가능. → Flyway 마이그레이션으로 unique 제약 추가, 로드맵 14번 동시성 테스트와 함께.
- [ ] **D5. CORS 전체 허용 + credentials** — `CorsFilter`(라인 23-29): 요청 Origin을 그대로 echo하며 Allow-Credentials: true. 모든 도메인에서
  인증 요청 가능한 설정. Authorization 헤더 기반이라 즉시 악용은 어려우나 관리자 웹 사용 시 위험. → 허용 도메인 화이트리스트로 전환, 로드맵 38번에서 처리.

### 심각도 중간

- [ ] **D6. 이벤트 발행-트랜잭션 결합 구조 문제** — @TransactionalEventListener는 트랜잭션 없는 곳에서 raise하면 조용히 미실행. 발행처가 트랜잭션 내부인지 코드만으로 판단
  불가한 구조. (참고: "롤백 시 AFTER_COMMIT 핸들러 실행" 우려는 오탐 — Spring이 커밋 후에만 실행함) → 로드맵 19번 아웃박스로 해결.
- [ ] **D7. refresh token 갱신 race + 재사용 감지 부재** — `MemberAuthService.refresh()`(라인 87-112): 검증→삭제 사이 동시 요청이 모두 통과 가능, 탈취
  토큰 재사용 감지(rotation) 없음. → 로드맵 38번에서 처리.
- [ ] **D8. 인덱스 누락 3건** — heart_transactions(member_id): 거래내역 조회 풀스캔. blocks(blocker_id): 추천/매칭/좋아요 조회마다 호출되는 차단 목록 조회
  풀스캔. likes(sender_id): 보낸 좋아요 조회. → 로드맵 27번에서 실행 계획 확인 후 적용.
- [ ] **D9. N+1·쿼리 비효율** — `IntroductionMemberIdFetcher`/`TodayCardMemberIdFetcher`: 추천 호출당 고정 5쿼리.
  `IntroductionQueryRepository`(라인 77-105): hobbies join 카테시안 곱 + 메모리 GroupBy. `TodayCardService`(라인 31-50): 조회 후 메모리
  필터(isActive를 DB where로 처리 가능). `MatchService.findNickname()`: 매치 작업마다 Member 재조회. → 로드맵 24번에서 처리.
- [ ] **D10. OSIV 미설정 (기본값 true)** — 요청 종료까지 DB 커넥션 점유, 부하 시 커넥션 고갈 가속. open-in-view: false 명시 필요하나 트랜잭션 밖 lazy 접근 정리가
  선행되어야 함. → 로드맵 26번에서 처리.

### 심각도 낮음 (품질)

- [ ] **D11. LocalDateTime.now() 직접 호출 (9개 파일)** — SoftDeleteBaseEntity, Match, Notification, RefundService 등. 시간 의존 로직(
  삭제 유예, 나이 계산, 만료) 단위 테스트 불가. → Clock 주입 패턴으로 전환.
- [ ] **D12. ZoneId.systemDefault() 의존** — TodayCardMemberIdFetcher, MemberAuthService 등. JVM 설정에 의존하는 취약한 구조. →
  ZoneId.of("Asia/Seoul") 명시.
- [ ] **D13. Apple 웹훅 실패 시 signedPayload 전체 로깅** — `PaymentController`(라인 60). 민감정보 최소화 원칙 위반. → 로깅 제거 또는 식별자만 기록.
- [ ] **D14. @Transactional readOnly 적용률 저조 (7/131)** — 조회 메서드에 readOnly 누락으로 불필요한 트랜잭션 오버헤드. → 일괄 점검·적용. Read Replica
  분리(53) 시 라우팅 기준이 되므로 선행 가치 있음.

### 점검 결과 문제없음으로 확인된 항목

소유권 검증(자기소개/인터뷰/매칭/이상형), 결제 중복 방지(transactionId unique + 코드 검증 이중 방어), 환불 중복 방지, 좋아요 중복 방지, SQL 인젝션, 입력 검증(@Valid), 파일
업로드 확장자 화이트리스트, 인증 제외 경로 범위, 락 해제 패턴(Redisson/NamedLock 모두 finally 처리), AuthContext(@RequestScope로 요청 간 격리 정상), 로그아웃 시
토큰 무효화.