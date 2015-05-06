FROM java:8-jre

ENV TZ=Europe/Berlin

WORKDIR /backend
EXPOSE 8080
CMD ["java", "-Xmx384m", "-Xms384m", "-server", "-jar", "backend.jar", "--server.port=8080", "--server.contextPath=/example-backend"]

ADD ./backend.jar /backend/backend.jar
