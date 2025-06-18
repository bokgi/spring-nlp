FROM openjdk:21-jdk
COPY target/nlp-0.0.1.jar nlp-app.jar
ENTRYPOINT ["java", "-jar", "nlp-app.jar"]

