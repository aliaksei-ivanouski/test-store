FROM gcr.io/distroless/java:11
WORKDIR /app
COPY target/libs/* libs/
COPY target/test-store-*.jar app.jar
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
