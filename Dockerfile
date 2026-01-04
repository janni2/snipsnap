FROM eclipse-temurin:8-jdk

# Install Ant
RUN apt-get update && \
    apt-get install -y ant && \
    rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Copy project files
COPY . .

# Run build
# Compile for Java 1.5 target using Java 8 JDK
RUN ant -Dant.build.javac.target=1.5 -Dant.build.javac.source=1.5 clean all

# Expose ports
# 8668: HTTP
# 8574: Admin RPC
EXPOSE 8668 8574

# Run using the launcher which handles classpath and tools.jar configuration
CMD ["java", "-jar", "lib/snipsnap.jar"]
