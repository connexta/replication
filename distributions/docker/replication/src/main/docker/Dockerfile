FROM openjdk:11-jre-slim
COPY maven/${app.artifactId}-${project.version}.jar /opt/replication/app.jar
COPY maven/demo-config/ /opt/replication/config/
WORKDIR /opt/replication
ENTRYPOINT ["java", "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:8000", "-jar" , "app.jar"]
