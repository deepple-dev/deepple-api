# 빌드를 위한 베이스 이미지
FROM eclipse-temurin:21-jdk-jammy AS build

# 작업 디렉토리 설정
WORKDIR /workspace/app

# Gradle Wrapper와 빌드 스크립트 복사
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

RUN chmod +x gradlew

# Gradle 의존성 다운로드
RUN ./gradlew --no-daemon dependencies

# 소스 코드와 나머지 프로젝트 파일 복사
COPY src src

# 애플리케이션 빌드
RUN ./gradlew build -x test --no-daemon

# 런타임을 위한 새로운 베이스 이미지
FROM eclipse-temurin:21-jre-jammy

VOLUME /tmp

# Apple Root CA 인증서 복사
COPY certs /etc/certs

# Firebase 서비스 계정 키 복사
COPY secrets/deepple-firebase.json /etc/secrets/deepple-firebase.json

# 빌드 아티팩트 복사
COPY --from=build /workspace/app/build/libs/*.jar app.jar

# 8080 포트 노출
EXPOSE 8080

# 애플리케이션 실행
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]
