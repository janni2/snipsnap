# SnipSnap Migration Plan

This plan outlines the steps to modernize the SnipSnap application. Each step produces a verifiable, runnable artifact.

## Phase 1: Lift Build to Maven & Containerization
**Goal:** Replace Ant with Maven and establish a reproducible Docker build.
**Status:** Completed.
**Artifacts:** `pom.xml`, `install-libs.sh`, `Dockerfile`, `docker-compose.yml`, `.gitlab-ci.yml`.
**Verification:**
- `bash install-libs.sh` installs local dependencies.
- `mvn clean package` generates `target/snipsnap-1.0-SNAPSHOT.war`.
- `docker-compose up` (requires Docker) starts the application.

## Phase 2: Upgrade Java Version (OpenRewrite)
**Goal:** Upgrade from Java 8 to Java 17 using automated refactoring.
**Steps:**
1.  **Configure OpenRewrite:** Add `rewrite-maven-plugin` to `pom.xml`.
2.  **Migrate to Java 11:**
    - Run `mvn rewrite:run -Drewrite.activeRecipes=org.openrewrite.java.migrate.UpgradeJavaVersion -Drewrite.recipeArtifactCoordinates=org.openrewrite.recipe:rewrite-migrate-java:RELEASE -Dversion=11`.
    - Update `maven-compiler-plugin` source/target to 11.
    - Update `Dockerfile` base image to `eclipse-temurin:11`.
    - Verification: `mvn clean package` succeeds.
3.  **Migrate to Java 17:**
    - Repeat for Java 17.
    - Update dependencies if needed (e.g., CGLib, ASM).
    - Verification: `mvn clean package` succeeds.

## Phase 3: Database Migration (PostgreSQL)
**Goal:** Switch persistence from MckoiDB to PostgreSQL.
**Steps:**
1.  **Infrastructure:** Add `postgres` service to `docker-compose.yml`.
2.  **Configuration:** Update `conf/snipsnap.conf` to use PostgreSQL JDBC driver and URL.
3.  **Verification:** Start application, verify connection in logs, verify data persistence.

## Phase 4: Web Server Migration (Spring Boot)
**Goal:** Migrate embedded Servlet container to Spring Boot.
**Steps:**
1.  **Add Dependencies:** Add `spring-boot-starter-web` (2.7.x) to `pom.xml`.
2.  **Create Application Class:** Create `@SpringBootApplication` class.
3.  **Register Servlets:** Create configuration to register existing servlets (`SnipSnapServlet`, etc.) using `ServletRegistrationBean`.
4.  **Update Entrypoint:** Update `Dockerfile` to run Spring Boot JAR.
5.  **Verification:** `mvn spring-boot:run` starts application.

## Phase 5: Kubernetes Deployment
**Goal:** Enable K8s deployment.
**Steps:**
1.  **Manifests:** Create `k8s/deployment.yaml`, `k8s/service.yaml`.
2.  **Verification:** `kubectl apply --dry-run` succeeds.

## Phase 6: Switch to Gradle (Optional)
**Goal:** Migrate to Gradle.
**Steps:**
1.  Run `gradle init`.
2.  Update CI/Docker to use Gradle.
