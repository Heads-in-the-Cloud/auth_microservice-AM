FROM adoptopenjdk/openjdk11:alpine-jre
ARG jar_name
COPY ./target/${jar_name} /usr/app/utopia.jar
COPY ./src/main/resources /src/main/resources
ENTRYPOINT ["java", "-jar", "/usr/app/utopia.jar"]
