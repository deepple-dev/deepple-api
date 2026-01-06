# k6 성능 테스트

## 설치

```bash
# macOS
brew install k6

# Docker
docker pull grafana/k6
```

## 실행

```bash
# 로컬 실행
k6 run k6/scripts/*.js

# 환경변수 설정
k6 run -e BASE_URL=http://localhost:8080 k6/scripts/*.js

# Docker 실행
docker run --rm -i grafana/k6 run - < k6/scripts/*.js
```

## 디렉토리 구조

```
k6/
├── scripts/     # 테스트 스크립트
└── data/        # 테스트 데이터 생성 SQL
```