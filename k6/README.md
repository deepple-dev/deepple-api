# 성능 테스트 (k6)

## 설치

```bash
brew install k6
```

## 구조

```
k6/
├── config.js   # 설정
├── lib/        # 헬퍼
└── api/        # API 테스트
```

## 환경 변수

| 변수          | 설명                | 기본값                     |
|-------------|-------------------|-------------------------|
| `BASE_URL`  | API 서버 URL        | `http://localhost:8080` |
| `TEST_TYPE` | 부하 프로파일 (아래 표 참고) | `smoke`                 |

### TEST_TYPE

| 값        | VU        | 시간  | 용도         |
|----------|-----------|-----|------------|
| `smoke`  | 1         | 30s | 기능 확인      |
| `load`   | 50        | 5m  | 일반 부하      |
| `stress` | 100 → 200 | 16m | 한계 테스트     |
| `spike`  | 10 → 500  | 5m  | 급격한 트래픽 대응 |
| `soak`   | 100       | 40m | 장시간 안정성    |

## 실행

```bash
k6 run k6/api/member-profile.js
k6 run -e BASE_URL=https://dev-api.deepple.co.kr -e TEST_TYPE=load k6/api/member-profile.js
```
