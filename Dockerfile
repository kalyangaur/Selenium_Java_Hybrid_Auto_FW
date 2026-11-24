# Java + Maven environment for running the existing automation project.
# Keep this JDK version compatible with the JDK used by your project locally.
FROM maven:3.9.11-eclipse-temurin-25

WORKDIR /workspace

# The project is bind-mounted by docker-compose.yaml, so source files stay editable
# on the host. Maven dependencies are cached in the named volume.
RUN java -version && mvn -version

CMD ["mvn", "clean", "test"]
