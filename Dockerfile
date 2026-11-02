FROM eclipse-temurin:21-jdk-jammy AS build
WORKDIR /workspace

# 빌드 스크립트만 먼저 복사해 의존성 레이어를 소스 변경과 분리한다
COPY gradlew settings.gradle build.gradle ./
COPY gradle gradle
RUN ./gradlew dependencies --no-daemon > /dev/null

COPY .git .git
COPY src src
RUN ./gradlew bootJar --no-daemon \
    && java -Djarmode=tools -jar build/libs/*.jar extract --layers --launcher --destination extracted

FROM eclipse-temurin:21-jre-jammy
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
