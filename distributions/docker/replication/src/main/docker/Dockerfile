FROM openjdk:11-jre-slim
COPY maven/${app.artifactId}-${project.version}.jar /opt/replication/app.jar
COPY maven/demo-config/ /opt/replication/config/
WORKDIR /opt/replication
ENTRYPOINT ["java", "-jar", "app.jar"]