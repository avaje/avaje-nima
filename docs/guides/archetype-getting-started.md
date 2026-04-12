# Avaje Nima Archetype Guide for AI Agents

**Target Audience:** AI systems (Claude, Copilot, ChatGPT, etc.)
**Purpose:** Scaffold new avaje-nima REST services programmatically
**Last Updated:** 2026-04-12
**Archetype Version:** 1.9-RC1

---

## Quick Reference

| Action | Command |
|--------|---------|
| **Generate interactively** | `mvn archetype:generate -DarchetypeGroupId=io.avaje.archetype -DarchetypeArtifactId=avaje-nima-archetype-minimal-rest -DarchetypeVersion=1.9-RC1` |
| **Generate in batch mode** | See [Batch Generation](#batch-generation) section |
| **Compile generated project** | `cd <project-dir> && mvn compile` |
| **Run tests** | `mvn test` |
| **Start server** | `mvn exec:java -Dexec.mainClass=<package>.Main` |
| **Expected port** | 8080 (configurable) |

---

## What This Archetype Provides

The `io.avaje.archetype:avaje-nima-archetype-minimal-rest` archetype generates a minimal, working REST service with:

- **Framework**: Helidon Níma web server + avaje-inject dependency injection
- **Included Libraries**: avaje-jsonb for JSON serialization, avaje-logger for logging
- **Annotation Processing**: avaje-nima-generator automatically wires dependency injection and HTTP routing
- **Generated Endpoints**:
  - `GET /hi` → Returns `"hi"` as `text/plain`
  - `GET /hi/data` → Returns JSON `{"message":"...", "timestamp":...}`
- **Test Framework**: Integration tests using `@InjectTest` with embedded server
- **Build Profiles**: Native executable (`-Pnative`) and fat-jar (`-Pfat-jar`) support
- **Java Version**: 25 (minimum)

---

## Prerequisites

Before using this archetype, verify:

| Requirement | Minimum Version | How to Verify |
|-------------|-----------------|---------------|
| Maven | 3.9 | `mvn --version` |
| Java JDK | 25 | `java -version` |
| Git (optional) | Any | `git --version` |

**Installation:**
- Maven: https://maven.apache.org/install.html
- Java 25: https://www.oracle.com/java/technologies/ or use SDKMAN (`sdk install java 25`)

---

## Generation Methods

### Interactive Generation

Run the archetype and respond to prompts:

```bash
mvn archetype:generate \
  -DarchetypeGroupId=io.avaje.archetype \
  -DarchetypeArtifactId=avaje-nima-archetype-minimal-rest \
  -DarchetypeVersion=1.9-RC1
```

**Prompts:**
```
Define value for property 'groupId': com.example
Define value for property 'artifactId': my-service
Define value for property 'version' [1.0-SNAPSHOT]: 1.0-SNAPSHOT
Define value for property 'package' [com.example]: com.example.service
```

**Output:**
```
Project created from Archetype in dir: /path/to/my-service
```

### Batch Generation (Non-Interactive)

Use the `-B` (batch) flag with all parameters defined:

```bash
mvn archetype:generate \
  -DarchetypeGroupId=io.avaje.archetype \
  -DarchetypeArtifactId=avaje-nima-archetype-minimal-rest \
  -DarchetypeVersion=1.9-RC1 \
  -DgroupId=com.example \
  -DartifactId=my-service \
  -Dversion=1.0-SNAPSHOT \
  -Dpackage=com.example.service \
  -B
```

**Key Parameters:**
- `groupId`: Reverse-domain package prefix (e.g., `com.company`)
- `artifactId`: Project directory name and JAR name (e.g., `my-service`)
- `version`: Semantic version (e.g., `1.0.0`, `1.0-SNAPSHOT`)
- `package`: Java package for generated classes (defaults to `groupId` if omitted)
- `-B`: Batch mode flag (no interactive prompts)

---

## Generated Project Structure

After generation, the project structure is:

```
my-service/
├── pom.xml                                 # Maven configuration
├── README.md                               # Project-specific README
└── src/
    ├── main/
    │   ├── java/
    │   │   └── com/example/service/        # <package> directory
    │   │       ├── Main.java               # Bootstraps Nima server (main entry point)
    │   │       ├── web/
    │   │       │   └── HelloController.java # REST controller with 2 sample endpoints
    │   │       └── model/
    │   │           └── GreetingResponse.java # JSON response record (@Json annotated)
    │   └── resources/
    │       ├── application.properties      # Server configuration (port, shutdown)
    │       └── avaje-logger.properties     # Logging configuration
    └── test/
        ├── java/
        │   └── com/example/service/
        │       └── HelloControllerTest.java # Integration tests (@InjectTest)
        └── resources/
            └── avaje-logger-test.properties # Logging config for tests
```

### Key Generated Files Explained

**1. `Main.java`**
- Entry point for the application
- Starts the Nima web server on port 8080
- Uses `Nima.builder()` fluent API to configure and bootstrap

**2. `HelloController.java`**
- REST controller with `@Controller` and `@Path("/hi")` annotations
- Contains two sample methods:
  - `hi()`: Returns plain text "hi"
  - `data()`: Returns JSON-serialized `GreetingResponse` object
- Demonstrates HTTP routing with `@Get`, `@Produces` annotations

**3. `GreetingResponse.java`**
- Java record with `@Json` annotation
- Fields: `message` (String), `timestamp` (long)
- Automatically serialized to/from JSON by avaje-jsonb

**4. `HelloControllerTest.java`**
- Integration test using `@InjectTest` annotation
- Starts embedded server, executes HTTP requests, verifies responses
- Uses `HttpClient` injected from avaje-inject
- Tests both endpoints: text and JSON responses

**5. `application.properties`**
- Server configuration (commented by default):
  - `server.port=8080` - HTTP port (environment: `NIMA_PORT`)
  - `server.shutdownGraceMillis=5000` - Shutdown timeout

**6. `pom.xml`**
- Maven build configuration
- Key dependencies: avaje-nima, avaje-nima-test, avaje-jsonb
- Annotation processors: avaje-nima-generator (code generation)
- Profiles: `native` (GraalVM native image), `fat-jar` (shaded JAR)

---

## Quick Start: Build and Run

### Step 1: Generate Project

```bash
mvn archetype:generate \
  -DarchetypeGroupId=io.avaje.archetype \
  -DarchetypeArtifactId=avaje-nima-archetype-minimal-rest \
  -DarchetypeVersion=1.9-RC1 \
  -DgroupId=com.example \
  -DartifactId=my-service \
  -Dpackage=com.example.service \
  -B
```

### Step 2: Enter Project Directory

```bash
cd my-service
```

### Step 3: Compile

```bash
mvn compile
```

**What happens:**
- Maven runs `javac` with annotation processors
- `avaje-nima-generator` generates DI wiring classes (in `target/`)
- `avaje-inject-maven-plugin` generates service provider interfaces

### Step 4: Run Tests

```bash
mvn test
```

**Expected output:**
```
[INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0
```

Both tests should pass:
- `hi_returnsPlainText()` - Verifies GET /hi returns "hi"
- `data_returnsJson()` - Verifies GET /hi/data returns JSON

### Step 5: Start the Application

**Option A: Using Maven exec plugin**

```bash
mvn exec:java -Dexec.mainClass=com.example.service.Main
```

**Option B: Direct IDE execution**

Run `Main.java` from your IDE.

**Expected output:**
```
[main] INFO io.helidon.webserver.HelidonWebServer - Started on http://localhost:8080
```

### Step 6: Test Endpoints

**In a separate terminal:**

```bash
# Plain text endpoint
curl http://localhost:8080/hi
# Response: hi

# JSON endpoint
curl http://localhost:8080/hi/data
# Response: {"message":"hello from avaje-nima","timestamp":1712961787123}
```

---

## Generation Parameters Reference

### `groupId` (Required)

Reverse-domain notation for your organization.

| Example | Meaning |
|---------|---------|
| `com.example` | Example organization |
| `com.company` | Company-owned package |
| `io.github.username` | GitHub-based project |
| `org.myorg` | Non-profit organization |

**Effect**: Becomes part of the package hierarchy and JAR coordinates.

### `artifactId` (Required)

Project name (used as directory name and JAR name).

| Example | Notes |
|---------|-------|
| `my-service` | Kebab-case (recommended for Maven) |
| `myservice` | Lowercase (valid) |
| `MyService` | PascalCase (valid but not conventional) |

**Constraints:**
- Must be lowercase letters, numbers, hyphens
- No spaces or special characters
- Becomes directory name: `my-service/`

### `version` (Optional)

Project version following semantic versioning.

| Example | Meaning |
|---------|---------|
| `1.0.0` | Release version |
| `1.0-SNAPSHOT` | Development version (default) |
| `0.1.0` | Pre-release |
| `1.0.0-RC1` | Release candidate |

**Default**: `1.0-SNAPSHOT`

### `package` (Optional)

Java package root for generated classes.

| Example | Generated Path |
|---------|-----------------|
| `com.example.service` | `src/main/java/com/example/service/` |
| `com.example` | `src/main/java/com/example/` |

**Default**: Same as `groupId`

---

## Common Customization Patterns

### 1. Change Server Port

**Before running:**

Edit `src/main/resources/application.properties`:

```properties
server.port=9000
server.shutdownGraceMillis=5000
```

**Or override at runtime:**

```bash
mvn exec:java \
  -Dexec.mainClass=com.example.service.Main \
  -Dserver.port=9000
```

### 2. Add New REST Endpoint

Create a new controller file: `src/main/java/com/example/service/web/StatusController.java`

```java
package com.example.service.web;

import io.avaje.http.api.Controller;
import io.avaje.http.api.Get;
import io.avaje.http.api.Path;

@Controller
@Path("/status")
public class StatusController {

  @Get
  String status() {
    return "OK";
  }
}
```

Then recompile:

```bash
mvn compile
```

New endpoint available: `GET /status` → `"OK"`

### 3. Add JSON Request Body

Create a request DTO: `src/main/java/com/example/service/model/EchoRequest.java`

```java
package com.example.service.model;

import io.avaje.jsonb.Json;

@Json
public record EchoRequest(String message) {
}
```

Update controller: `src/main/java/com/example/service/web/EchoController.java`

```java
package com.example.service.web;

import io.avaje.http.api.Controller;
import io.avaje.http.api.Post;
import io.avaje.http.api.Path;
import com.example.service.model.EchoRequest;

@Controller
@Path("/echo")
public class EchoController {

  @Post
  EchoRequest echo(EchoRequest request) {
    return request;
  }
}
```

Test:

```bash
curl -X POST http://localhost:8080/echo \
  -H "Content-Type: application/json" \
  -d '{"message":"hello"}'
# Response: {"message":"hello"}
```

### 4. Add Dependency Injection

Add a service: `src/main/java/com/example/service/service/GreetingService.java`

```java
package com.example.service.service;

public class GreetingService {
  public String greet(String name) {
    return "Hello, " + name + "!";
  }
}
```

Inject into controller:

```java
package com.example.service.web;

import io.avaje.http.api.Controller;
import io.avaje.http.api.Get;
import io.avaje.http.api.Path;
import io.avaje.http.api.QueryParam;
import jakarta.inject.Inject;
import com.example.service.service.GreetingService;

@Controller
@Path("/greet")
public class GreetController {

  @Inject
  private GreetingService greetingService;

  @Get
  String greet(@QueryParam String name) {
    return greetingService.greet(name);
  }
}
```

Test:

```bash
curl "http://localhost:8080/greet?name=Alice"
# Response: Hello, Alice!
```

### 5. Add Database Entity

Generate archetype creates no database dependencies by default. To add JPA/database:

**Edit `pom.xml` and add dependency:**

```xml
<dependency>
  <groupId>io.ebean</groupId>
  <artifactId>ebean</artifactId>
  <version>17.4.0</version>
</dependency>
```

Recompile to use Ebean ORM.

---

## Build Profiles

### Native Executable Profile

Build as a standalone native binary (requires GraalVM):

```bash
mvn package -Pnative
```

**Output:**
```
./target/my-service
```

**Run directly (no Java required):**

```bash
./target/my-service
```

**Advantages:**
- Instant startup (<100ms)
- Low memory footprint
- No JVM required
- Single executable file

**Requirements:**
- GraalVM 23+ installed
- Native build tools (gcc on macOS/Linux)
- 10+ minutes build time

### Fat JAR Profile

Bundle all dependencies into a single executable JAR:

```bash
mvn package -Pfat-jar
```

**Output:**
```
./target/my-service-1.0-SNAPSHOT.jar
```

**Run:**

```bash
java -jar target/my-service-1.0-SNAPSHOT.jar
```

**Advantages:**
- All dependencies self-contained
- Easy distribution
- Works anywhere Java 25+ is installed
- Faster build than native

---

## File Locations Reference

| File | Path | Purpose |
|------|------|---------|
| Main entry point | `src/main/java/<package>/Main.java` | Server bootstrap |
| Controllers | `src/main/java/<package>/web/*.java` | HTTP endpoints |
| Models/DTOs | `src/main/java/<package>/model/*.java` | JSON request/response objects |
| Application config | `src/main/resources/application.properties` | Server configuration |
| Test suite | `src/test/java/<package>/*Test.java` | Integration tests |
| Compiled classes | `target/classes/` | Compiled bytecode |
| Generated code | `target/generated-sources/` | Annotation processor output |
| Build artifacts | `target/*.jar` | Packaged application |

---

## Troubleshooting

### Issue: "archetype not found"

**Symptom:** `Unknown archetype repository: archetype.repository`

**Cause:** Maven cannot locate the archetype in central repository.

**Solution:** Specify full archetype coordinates with version:

```bash
mvn archetype:generate \
  -DarchetypeGroupId=io.avaje.archetype \
  -DarchetypeArtifactId=avaje-nima-archetype-minimal-rest \
  -DarchetypeVersion=1.9-RC1
```

### Issue: Java version mismatch

**Symptom:** `[ERROR] Maven Compiler Plugin could not be configured: java.lang.UnsupportedClassVersionError`

**Cause:** System Java version < 25

**Solution:** Verify and upgrade Java:

```bash
java -version
# Should show: Java 25+

# Or set JAVA_HOME
export JAVA_HOME=/path/to/java25
```

### Issue: Annotation processors not running

**Symptom:** Generated classes not found, compilation fails

**Cause:** `avaje-nima-generator` not invoked during compile

**Solution:** Verify `pom.xml` has annotation processor configuration:

```xml
<plugin>
  <groupId>org.apache.maven.plugins</groupId>
  <artifactId>maven-compiler-plugin</artifactId>
  <configuration>
    <annotationProcessorPaths>
      <path>
        <groupId>io.avaje</groupId>
        <artifactId>avaje-nima-generator</artifactId>
        <version>${avaje.nima.version}</version>
      </path>
    </annotationProcessorPaths>
  </configuration>
</plugin>
```

Then run: `mvn clean compile`

### Issue: Port 8080 already in use

**Symptom:** `Exception: java.net.BindException: Address already in use`

**Solution A:** Kill process using port 8080

```bash
# macOS/Linux
lsof -i :8080 | grep LISTEN | awk '{print $2}' | xargs kill -9

# Or use different port
mvn exec:java -Dexec.mainClass=com.example.service.Main -Dserver.port=9000
```

**Solution B:** Uncomment and change port in `application.properties`:

```properties
server.port=9000
```

### Issue: Tests fail during compilation

**Symptom:** `[ERROR] Tests run: N, Failures: X`

**Cause:** Embedded server setup issue or missing dependencies

**Solution:** Clean and rebuild:

```bash
mvn clean test
```

Or run with verbose output:

```bash
mvn test -X
```

---

## For AI Agents: Command Patterns

### Pattern 1: Generate and Build

AI agents should use this sequence to ensure successful project creation:

```bash
# 1. Generate project in batch mode
mvn archetype:generate \
  -DarchetypeGroupId=io.avaje.archetype \
  -DarchetypeArtifactId=avaje-nima-archetype-minimal-rest \
  -DarchetypeVersion=1.9-RC1 \
  -DgroupId=<GROUP> \
  -DartifactId=<ARTIFACT> \
  -Dversion=<VERSION> \
  -Dpackage=<PACKAGE> \
  -B

# 2. Enter project directory
cd <ARTIFACT>

# 3. Verify compilation
mvn compile

# 4. Run tests to verify setup
mvn test

# 5. Optionally: Run application
mvn exec:java -Dexec.mainClass=<PACKAGE>.Main
```

### Pattern 2: Verify Generated Project

To verify a generated project is valid:

```bash
# Test compilation
mvn compile

# Test execution
mvn test

# Check for generated files
ls target/generated-sources/
```

**Expected files in `target/generated-sources/`:**
- `AppProvides.java` - Dependency injection wiring
- `$Controller*.java` - HTTP routing annotations

### Pattern 3: Extract Project Information

To read configuration from generated project:

```bash
# Get project group, artifact, version, package
cd <project-dir>
mvn help:describe -Ddetail=true | grep -E "group|artifact|version|package"

# Or parse pom.xml
grep -E "<groupId>|<artifactId>|<version>|<package>" pom.xml
```

### Pattern 4: Add Dependencies Programmatically

To add a dependency to the generated project:

```bash
mvn dependency:tree  # Show current dependencies

# Manually add to pom.xml <dependencies> section or use:
mvn install:install-file -Dfile=<jar> -DgroupId=<group> ...
```

### Pattern 5: Custom Build Instructions

For custom build steps after generation:

```bash
# 1. Compile and generate code
mvn compile

# 2. Copy generated sources to main
mvn exec:exec -Dexec.executable="cp -r"

# 3. Re-compile with generated code
mvn clean compile
```

---

## Integration with IDE/Tools

### VS Code

1. Install "Extension Pack for Java" by Microsoft
2. Open project folder
3. Open `src/main/java/<package>/Main.java`
4. Click "Run" button above main() method
5. Application starts on port 8080

### IntelliJ IDEA

1. File → Open → Select project directory
2. Maven should auto-detect `pom.xml`
3. Right-click `Main.java` → Run 'Main.main()'
4. Application starts on port 8080

### Eclipse

1. File → Import → Existing Maven Projects
2. Select project directory
3. Eclipse imports and indexes automatically
4. Right-click project → Run As → Java Application
5. Select `Main` class to run

---

## Reference Links

| Resource | URL |
|----------|-----|
| Avaje Nima Docs | https://avaje.io/nima/ |
| Avaje Archetypes | https://avaje.io/nima/archetypes |
| Maven Archetype Plugin | https://maven.apache.org/archetype/ |
| Helidon Níma | https://helidon.io/ |
| Avaje Inject | https://avaje.io/inject/ |
| Java 25 Release | https://www.oracle.com/java/technologies/ |

---

## Version Information

| Component | Version |
|-----------|---------|
| Archetype | 1.9-RC1 |
| Avaje Nima | 1.8 |
| Avaje HTTP | 3.8 |
| Helidon | 4.4.0 |
| Java Target | 25 |
| Maven Minimum | 3.9 |

---

## Document Information

**Intended for:** AI agents (Claude, GPT, Copilot)
**Format:** Markdown with code blocks
**Refresh Cadence:** Updated with new archetype versions
**Last Generated:** 2026-04-12
**Maintenance:** Keep in sync with archetype README and avaje.io docs

---
