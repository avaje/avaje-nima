# avaje-nima-archetype-minimal-rest

Maven archetype that scaffolds a minimal [avaje-nima](https://github.com/avaje/avaje-nima) REST service.
The generated project provides two endpoints out of the box:

| Method | Path | Response |
|--------|------|----------|
| `GET` | `/hi` | `"hi"` — `text/plain` |
| `GET` | `/hi/data` | `{"message":"…","timestamp":…}` — `application/json` |

---

## Prerequisites

| Tool | Minimum version |
|------|-----------------|
| JDK | 25              |
| Maven | 3.9             |

---

## 1. Generate a new project

Run the following command and answer the interactive prompts for `groupId`, `artifactId`, `version`, and `package`:

```bash
mvn archetype:generate \
  -DarchetypeGroupId=io.avaje \
  -DarchetypeArtifactId=avaje-nima-archetype-minimal-rest \
  -DarchetypeVersion=1.9-RC1
```

**Non-interactive (batch mode) example:**

```bash
mvn archetype:generate \
  -DarchetypeGroupId=io.avaje \
  -DarchetypeArtifactId=avaje-nima-archetype-minimal-rest \
  -DarchetypeVersion=1.9-RC1 \
  -DgroupId=com.example \
  -DartifactId=my-service \
  -Dversion=1.0-SNAPSHOT \
  -Dpackage=com.example.myservice \
  -B
```

---

## 2. Build the project

```bash
cd my-service
mvn compile
```

The annotation processors (`avaje-nima-generator`) run automatically during compilation and generate the dependency-injection wiring and JSON adapters.

---

## 3. Run the tests

```bash
mvn test
```

The integration tests start an embedded server, exercise both endpoints, and shut it down automatically.

---

## 4. Start the application

```bash
mvn exec:java -Dexec.mainClass=com.example.myservice.Main
```

Or run the `Main` class directly from your IDE. The server starts on port **8080** by default.

Test the endpoints:

```bash
# Plain-text endpoint
curl http://localhost:8080/hi

# JSON endpoint
curl http://localhost:8080/hi/data
```

---

## 5. Build as native executable (optional)

```bash
mvn package -Pnative
./target/my-service
```

---

## 6. Build a fat jar (optional)

The `fat-jar` Maven profile bundles all dependencies into a single executable jar using the Maven Shade plugin.
The `ServicesResourceTransformer` ensures avaje-inject SPI registrations are merged correctly.

```bash
mvn package -Pfat-jar
java -jar target/my-service-1.0-SNAPSHOT.jar
```

---

## Generated project structure

```
my-service/
├── pom.xml
└── src/
    ├── main/
    │   ├── java/com/example/myservice/
    │   │   ├── Main.java                  # Bootstraps the Nima server
    │   │   ├── web/
    │   │   │   └── HelloController.java   # REST controller (@Controller @Path("/hi"))
    │   │   └── model/
    │   │       └── GreetingResponse.java  # JSON record (@Json)
    │   └── resources/
    │       ├── application.properties     # Server port and shutdown config
    │       └── avaje-logger.properties    # Logging configuration
    └── test/
        ├── java/com/example/myservice/
        │   └── HelloControllerTest.java   # Integration tests (@InjectTest)
        └── resources/
            └── avaje-logger-test.properties
```

---

## Key dependencies in the generated project

| Dependency | Scope | Purpose |
|---|---|---|
| `io.avaje:avaje-nima` | compile | Helidon Níma server + avaje-inject + avaje-jsonb |
| `io.avaje:avaje-simple-logger` | compile | Lightweight SLF4J implementation |
| `io.avaje:avaje-nima-generator` | annotation processor | Code-generates DI wiring, JSON adapters, and HTTP routing |
| `io.avaje:avaje-nima-test` | test | Embedded server lifecycle for `@InjectTest` |

---

## Configuration

`application.properties` (in `src/main/resources`) is picked up automatically by avaje-config:

```properties
server.port=8080
server.shutdownGraceMillis=5000
```

The port can also be overridden at runtime via the `NIMA_PORT` environment variable or the `server.port` system property.
