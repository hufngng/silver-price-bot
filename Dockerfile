FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -q
COPY src ./src
RUN mvn package -q -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/silver-price-bot-1.0.0.jar app.jar

ENV TELEGRAM_BOT_TOKEN=""
ENV TELEGRAM_CHAT_ID=""

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]
