FROM --platform=$BUILDPLATFORM eclipse-temurin:21-jdk-jammy@sha256:4b1a74fbffbe6b4e7b6f0171a472478282fb463790a51edf10e465d578f22b69 AS build
WORKDIR /workspace

# 빌드 스크립트만 먼저 복사해 의존성 레이어를 소스 변경과 분리한다
COPY gradlew settings.gradle build.gradle ./
COPY gradle gradle
RUN ./gradlew dependencies --no-daemon > /dev/null

COPY .git .git
COPY src src
RUN ./gradlew bootJar --no-daemon \
    && java -Djarmode=tools -jar build/libs/*.jar extract --layers --launcher --destination extracted

FROM eclipse-temurin:21-jre-jammy@sha256:1eac8b8fee11ca7d6725a48bc5d41992ed1561504ebae25490d2d062847e8269
WORKDIR /app

RUN useradd --system --uid 1001 app

COPY certs/appstore /etc/certs/appstore

# 자주 바뀌지 않는 레이어부터 복사해 이미지 레이어 캐시를 살린다
COPY --from=build /workspace/extracted/dependencies/ ./
COPY --from=build /workspace/extracted/spring-boot-loader/ ./
COPY --from=build /workspace/extracted/snapshot-dependencies/ ./
COPY --from=build /workspace/extracted/application/ ./

USER app
EXPOSE 8080

ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "org.springframework.boot.loader.launch.JarLauncher"]
