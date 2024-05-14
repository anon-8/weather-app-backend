FROM openjdk:21-jdk

RUN groupadd -r app && useradd -r -g app app

USER app

COPY target/*.jar app.jar

ENTRYPOINT ["java", "-jar", "/app.jar"]