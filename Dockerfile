# build jar, copy to src user directory
FROM maven:3.8.2-jdk-11 AS build
WORKDIR /usr/src/utopia
COPY /src src
COPY /pom.xml pom.xml
RUN mvn -f pom.xml clean package

# copy existing jar
FROM gcr.io/distroless/java:11
COPY --from=build /usr/src/utopia/target/auth-0.0.1-SNAPSHOT.jar /usr/app/auth-0.0.1-SNAPSHOT.jar
COPY --from=build /usr/src/utopia/src/main/resources /src/main/resources
ENTRYPOINT ["java", "-jar", "/usr/app/auth-0.0.1-SNAPSHOT.jar"]
