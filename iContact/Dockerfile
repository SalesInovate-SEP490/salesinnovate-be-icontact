FROM openjdk:17

ARG JAR_FILE=target/*.jar

ADD ${JAR_FILE} icontact-service.jar

ENTRYPOINT ["java","-jar","icontact-service.jar"]

EXPOSE 8084