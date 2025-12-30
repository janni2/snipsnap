FROM maven:3.8-eclipse-temurin-8 as builder

WORKDIR /app

COPY . .

# Run the installation of local libs
RUN chmod +x install-libs.sh && ./install-libs.sh

# Build the project
RUN mvn clean package -DskipTests

# Run stage
FROM eclipse-temurin:8-jre

WORKDIR /app

# Copy artifacts
COPY --from=builder /app/target/snipsnap-1.0-SNAPSHOT.war /app/snipsnap.war
COPY --from=builder /app/lib /app/lib

# Copy configuration (if needed by AppServer)
COPY --from=builder /app/conf /app/conf

# We need to construct the classpath manually because we are running the Main class, not a JAR
# But we produced a WAR. The original AppServer expects everything in classpath.
# Let's try to run with java -cp ...
# We need all dependencies.
# A better way is to use maven-dependency-plugin to copy-dependencies in the builder stage.

COPY --from=builder /app/target/snipsnap-1.0-SNAPSHOT/WEB-INF/lib /app/libs

EXPOSE 8668

# Run with all jars in libs
CMD java -cp "/app/libs/*" org.snipsnap.server.AppServer
