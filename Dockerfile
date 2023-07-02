# build
FROM maven:3.8.6-jdk-11 AS build
COPY . .
RUN mvn clean package -DskipTests

# package
FROM openjdk:11-jdk-slim
COPY --from=build /target/ged-app-0.0.1-SNAPSHOT.jar ged-app.jar
EXPOSE ${PORT}
ENTRYPOINT ["java","-Dserver.port=${PORT}","-jar","ged-app.jar"]