# 성능 테스트용 데이터 생성

## 실행

```bash
mysql -u [user] -p -h [host] [db] < ./01_members.sql
mysql -u [user] -p -h [host] [db] < ./02_introductions.sql
mysql -u [user] -p -h [host] [db] < ./03_likes.sql
mysql -u [user] -p -h [host] [db] < ./04_matches.sql
mysql -u [user] -p -h [host] [db] < ./05_notifications.sql
mysql -u [user] -p -h [host] [db] < ./06_finalize.sql
```

## 설정

| 파일               | 변수                 | 기본값       | 설명          |
|------------------|--------------------|-----------|-------------|
| 01_members       | @TARGET_COUNT      | 1,000,000 | 회원 수        |
| 02_introductions | @INTROS_PER_MEMBER | 30        | 회원당 소개 수    |
| 03_likes         | @LIKE_RATIO        | 30%       | 소개 중 좋아요 비율 |
| 04_matches       | @MATCH_RATIO       | 30%       | 좋아요 중 매칭 비율 |

## 생성 데이터

- **회원**: 100만명 (전화번호: 01000000001 ~ 01001000000)
- **소개**: 3,000만건 (회원당 30명)
- **좋아요**: 900만건
- **매칭**: 270만건

## 주의

각 파일 실행 시 해당 테이블 데이터 자동 삭제 후 재생성
