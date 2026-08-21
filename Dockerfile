# CI/로컬에서 ./gradlew bootJar 후 이미지 빌드 (소스는 이미지에 포함하지 않음)
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN apk add --no-cache dumb-init \
    && addgroup -S app && adduser -S app -G app

COPY --chown=app:app build/libs/*.jar /app/app.jar

USER app
EXPOSE 8080
ENTRYPOINT ["/usr/bin/dumb-init", "--"]
CMD ["java", "-Duser.timezone=Asia/Seoul", "-jar", "/app/app.jar"]
