# ---------- Build stage ----------
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn

RUN chmod +x mvnw

RUN ./mvnw dependency:go-offline -B

COPY src src

RUN ./mvnw clean package -DskipTests



# ---------- Runtime stage ----------
FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

# Exécuter l'application sans les privilèges root
RUN useradd --system --no-create-home --shell /usr/sbin/nologin spring
USER spring

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
