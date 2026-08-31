FROM eclipse-temurin:17-jre-jammy

WORKDIR /app
COPY target/sahyog-data-collection-be-*.jar app.jar

EXPOSE 8091
HEALTHCHECK --interval=5s --timeout=3s --start-period=20s --retries=12 \
  CMD bash -c '</dev/tcp/127.0.0.1/8091'

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
