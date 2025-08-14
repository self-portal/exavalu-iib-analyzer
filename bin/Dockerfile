FROM openjdk:17
#ARG JAR_FILE=target/*.jar
VOLUME /app
EXPOSE 8079
ADD  target/exavalu-iib-analyzer-1.0.0.jar exavalu-iib-analyzer-1.0.0.jar
ENTRYPOINT [ "java","-jar","/exavalu-iib-analyzer-1.0.0.jar" ]