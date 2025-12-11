# deepple

딥플은 사용자의 가치관과 취향을 분석하여 최적의 매칭을 제공하는 **데이팅 서비스**입니다.
**도메인 주도 설계**로 복잡한 비즈니스 로직을 응집력 있게 모델링하고, **도메인 이벤트**를 통해 경계 간 직접적인 의존 없이 협력하도록 설계했습니다.
**AWS ECS 기반 클라우드 인프라**를 구축하였고, **GitHub Actions**를 활용한 CI/CD 파이프라인으로 자동화된 배포를 구현했습니다.

<br>

## 기술 스택

- JDK 21, Spring Boot 3, JPA, QueryDSL, Flyway
- AWS EC2, ECS, ECR, RDS(MySQL), ElastiCache(Valkey), S3
- Docker, GitHub Actions, Nginx
- Firebase Cloud Messaging, Apple App Store Server API, Bizgo SMS

<br>

## 문서

자세한 내용은 [Wiki](https://github.com/deepple-dev/deepple-api/wiki)를 참고하세요.

- [시스템 아키텍처](https://github.com/deepple-dev/deepple-api/wiki/시스템-아키텍처)
- [소프트웨어 아키텍처](https://github.com/deepple-dev/deepple-api/wiki/소프트웨어-아키텍처)
- [CI/CD 파이프라인](https://github.com/deepple-dev/deepple-api/wiki/CI-CD-파이프라인)

<br>

## 주요 기능

- **회원**: SMS 인증, 프로필 관리, 프로필 이미지 업로드, 이상형 설정
- **매치**: 다중 필터 기반 이성 소개 (등급별, 취미, 종교, 지역), 오늘의 카드, 소울메이트 매칭, 연애고사 기반 분석, 프로필 블러 해제 등
- **매칭 메시지**: 매칭 요청 메시지 전송/수락/거절, 메시지 내역 관리
- **하트 및 결제**: 앱 내 재화 시스템, Apple App Store 결제 연동, 미션 보상
- **커뮤니티**: 셀프 소개, 프로필 교환, 좋아요
- **관리자**: 회원 프로필 심사, 회원 관리, 신고 처리, 회원 정지 및 경고

<br>

## 개발자

- [공태현](https://github.com/Kong-TaeHyeon): 회원, 매칭, 커뮤니티 도메인
- [정호윤](https://github.com/stemmmm): 알림, 관리자, 좋아요 도메인, 인프라(AWS), CI/CD
- [하인호](https://github.com/hainho): 하트, 결제, 연애고사, 차단, 신고 도메인