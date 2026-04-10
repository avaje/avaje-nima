# GitHub Copilot Instructions for avaje-nima projects

This file provides context and instructions for GitHub Copilot (and other AI coding agents) working in avaje-nima projects.

---

## Project context

**avaje-nima** projects are Java applications built on:
- [Helidon SE](https://helidon.io/) virtual-thread HTTP server
- [avaje-inject](https://avaje.io/inject/) for dependency injection
- [avaje-jsonb](https://avaje.io/jsonb/) for JSON serialisation
- Maven as the build tool
- Java 21+ (typically Java 25)

Applications listen on port **8080** by default.

---

## How to add a JVM Docker image build with Jib

When asked to *"add a Docker build"*, *"containerise this app"*, or *"add Jib"* to an avaje-nima project, follow these steps exactly.

### Step 1 — Locate the insertion point in `pom.xml`

Find the `<build><plugins>` section. Identify the last existing `<plugin>` entry before the closing `</plugins>` tag. The Jib plugin must be added **inside** `<build><plugins>...</plugins></build>`.

If an `avaje-inject-maven-plugin` entry is present, insert the Jib plugin **after** it.

### Step 2 — Insert the Jib plugin XML

Insert the following XML block:

```xml
<plugin> <!-- build "normal jvm" docker image -->
  <groupId>com.google.cloud.tools</groupId>
  <artifactId>jib-maven-plugin</artifactId>
  <version>3.5.1</version>
  <executions>
    <execution>
      <goals>
        <goal>dockerBuild</goal>
      </goals>
      <phase>package</phase>
    </execution>
  </executions>
  <configuration>
    <container>
      <jvmFlags>
        <jvmFlag>-XX:MaxRAMPercentage=70</jvmFlag>
        <jvmFlag>-XX:+UseG1GC</jvmFlag>
        <!-- ZGC alternative (needs explicit heap bounds):
        <jvmFlag>-Xmx400m</jvmFlag>
        <jvmFlag>-Xms250m</jvmFlag>
        <jvmFlag>-XX:+UseZGC</jvmFlag>
        <jvmFlag>-XX:SoftMaxHeapSize=300m</jvmFlag>
        -->
      </jvmFlags>
      <ports>8080</ports>
    </container>
    <from>
      <image>amazoncorretto:25-al2023-headless</image>
    </from>
    <to>
      <image>${project.artifactId}:${project.version}</image>
    </to>
  </configuration>
</plugin>
```

### Step 3 — Adjust values if needed

| Setting | Default | When to change |
|---|---|---|
| `<version>3.5.1</version>` | `3.5.1` | Update to the latest stable Jib release if available |
| `amazoncorretto:25-al2023-headless` | Corretto 25 headless | Change the major version to match `maven.compiler.release` in the pom (e.g. `amazoncorretto:21-al2023-headless` for Java 21) |
| `-XX:MaxRAMPercentage=70` | 70 % of container memory | Adjust if the service has unusual memory requirements |
| `<ports>8080</ports>` | 8080 | Change to match the application's actual HTTP port |
| `${project.artifactId}:${project.version}` | Maven artifact name + version | Override if a different image name/tag convention is in use |

### Step 4 — Verify the addition

Check that:
1. The `<plugin>` block is inside `<build><plugins>` and not inside any `<profile>`.
2. There is no duplicate `jib-maven-plugin` entry in the default build (a separate Jib entry may exist inside the `native` profile — that is expected and correct).
3. The XML is well-formed (no unclosed tags, correct nesting).

### Step 5 — Confirm the build works

```bash
mvn package
```

The build should complete and a Docker image named `<artifactId>:<version>` should appear in the local daemon:

```bash
docker images | grep <artifactId>
```

Run the container to verify it starts:

```bash
docker run --rm -p 8080:8080 <artifactId>:<version>
```

---

## Notes

- The `dockerBuild` goal pushes to the **local Docker daemon**. To push to a remote registry, replace `dockerBuild` with `build` and add `<to><image>registry.example.com/…</image></to>`.
- The JVM flags use `MaxRAMPercentage` so the heap scales with the container's memory limit — no explicit `-Xmx` is required.
- A ZGC configuration is provided as a commented-out alternative; enable it if very low GC pause times are needed (requires explicit heap bounds via `-Xmx`/`-Xms`).
- For GraalVM **native image** Docker builds, see `docs/jib-native-docker.md` — that is handled separately via the `native` Maven profile.

---

## Reference

- JVM Docker build how-to: [`docs/jib-jvm-docker.md`](docs/jib-jvm-docker.md)
- Native image Docker build how-to: [`docs/jib-native-docker.md`](docs/jib-native-docker.md)
- Jib documentation: https://github.com/GoogleContainerTools/jib/tree/master/jib-maven-plugin
- Amazon Corretto images: https://hub.docker.com/_/amazoncorretto

---

## How to add a native image Docker build with Jib

When asked to *"add a native Docker build"*, *"add GraalVM native image support"*, or *"add the native profile"* to an avaje-nima project, follow these steps exactly.

### Step 1 — Confirm GraalVM is available

The native build requires GraalVM JDK. Note this in any instructions you generate — the developer must run `mvn package -Pnative` with GraalVM active (e.g. via `sdk use java 25.0.2-graal`).

### Step 2 — Locate the insertion point in `pom.xml`

Find the `<profiles>` section (or the closing `</project>` tag if none exists). The entire native configuration lives inside a single `<profile>` block:

```xml
<profiles>
  <profile>
    <id>native</id>
    ...
  </profile>
</profiles>
```

If `<profiles>` does not exist, add it before `</project>`.

### Step 3 — Insert the native profile XML

Insert the following complete profile. Replace `com.example.Main` with the project's actual main class (find it by searching for `public static void main` or `void main()` in the source tree, or look for an existing `<mainClass>` reference in the pom).

```xml
<profile>
  <id>native</id>
  <build>
    <plugins>

      <plugin> <!-- compile to a native executable -->
        <groupId>org.graalvm.buildtools</groupId>
        <artifactId>native-maven-plugin</artifactId>
        <version>0.11.4</version>
        <extensions>true</extensions>
        <executions>
          <execution>
            <id>build-native</id>
            <goals>
              <goal>compile-no-fork</goal>
            </goals>
            <phase>package</phase>
            <configuration>
              <mainClass>com.example.Main</mainClass>
            </configuration>
          </execution>
        </executions>
        <configuration>
          <buildArgs>
            <buildArg>--emit build-report</buildArg>
            <buildArg>--no-fallback</buildArg>
            <buildArg>-march=compatibility</buildArg>
            <buildArg>--static-nolibc</buildArg>
          </buildArgs>
        </configuration>
      </plugin>

      <plugin> <!-- build the native image docker container -->
        <groupId>com.google.cloud.tools</groupId>
        <artifactId>jib-maven-plugin</artifactId>
        <version>3.5.1</version>
        <executions>
          <execution>
            <goals>
              <goal>dockerBuild</goal>
            </goals>
            <phase>package</phase>
          </execution>
        </executions>
        <dependencies>
          <dependency>
            <groupId>com.google.cloud.tools</groupId>
            <artifactId>jib-native-image-extension-maven</artifactId>
            <version>0.1.0</version>
          </dependency>
        </dependencies>
        <configuration>
          <pluginExtensions>
            <pluginExtension>
              <implementation>com.google.cloud.tools.jib.maven.extension.nativeimage.JibNativeImageExtension</implementation>
              <properties>
                <imageName>${project.artifactId}</imageName>
              </properties>
            </pluginExtension>
          </pluginExtensions>
          <container>
            <mainClass>com.example.Main</mainClass>
            <ports>8080</ports>
          </container>
          <from> <!-- UBI micro image with glibc support -->
            <image>redhat/ubi10-micro:10.1-1762215812</image>
          </from>
          <to>
            <image>${project.artifactId}-native:${project.version}</image>
          </to>
        </configuration>
      </plugin>

    </plugins>
  </build>
</profile>
```

### Step 4 — Adjust values if needed

| Setting | Default | When to change |
|---|---|---|
| `<mainClass>com.example.Main</mainClass>` | — | **Always replace** with the project's actual main class; appears in **both** plugins |
| `native-maven-plugin` version `0.11.4` | `0.11.4` | Update to the latest compatible release |
| `jib-maven-plugin` version `3.5.1` | `3.5.1` | Match the version used in the default JVM build |
| `redhat/ubi10-micro:10.1-1762215812` | UBI 10 micro | Use a newer digest tag if available; keep `ubi*-micro` for glibc support |
| `<ports>8080</ports>` | 8080 | Change to match the application's actual HTTP port |
| Image tag suffix `-native` | `-native` | Keep this suffix to distinguish from the JVM image |

### Step 5 — Verify the addition

Check that:
1. The `<profile id="native">` block is inside `<profiles>` at the top level of `<project>`, **not** inside `<build>`.
2. `<mainClass>` is set to the same value in **both** `native-maven-plugin` and Jib's `<container>` section.
3. There is no duplicate `jib-maven-plugin` entry inside this profile.
4. The XML is well-formed.

### Step 6 — Confirm the build works

```bash
mvn package -Pnative
```

The build compiles a native binary and then builds a Docker image. This typically takes 3–10 minutes. Verify:

```bash
docker images | grep native
```

Run the container:

```bash
docker run --rm -p 8080:8080 <artifactId>-native:<version>
```

Startup should complete in under 200 ms.

---

## Notes (native)

- The `native` profile is **additive** — it does not replace the default JVM build. Both coexist in the same `pom.xml`.
- The `--static-nolibc` build arg creates a mostly-static binary that links glibc dynamically. The `ubi10-micro` base image provides glibc, which is why it is chosen over `scratch` or distroless images.
- No `<jvmFlags>` are needed in the Jib container config — the container runs a native binary, not a JVM process.
- The `JibNativeImageExtension` locates the binary produced by `native-maven-plugin` automatically; the `<imageName>` property sets the binary's name inside the image.

---

## Reference (native)

- Full how-to guide: [`docs/jib-native-docker.md`](docs/jib-native-docker.md)
- Jib native image extension: https://github.com/GoogleContainerTools/jib-extensions/tree/master/first-party/jib-native-image-extension-maven
- GraalVM native build tools: https://graalvm.github.io/native-build-tools/latest/maven-plugin.html
- Red Hat UBI micro images: https://catalog.redhat.com/software/containers/ubi10/ubi-micro

---

## How to add a global exception handler

When asked to *"add a global exception handler"*, *"add centralised error handling"*, or *"add an error response"* to an avaje-nima project, follow these steps exactly.

### Step 1 — Add the `avaje-record-builder` dependency to `pom.xml`

Insert inside `<dependencies>`:

```xml
<dependency>
  <groupId>io.avaje</groupId>
  <artifactId>avaje-record-builder</artifactId>
  <version>1.4</version>
  <scope>provided</scope>
</dependency>
```

> `avaje-nima` already transitively includes `avaje-jsonb` (`@Json`) and `avaje-http-api` (`@ExceptionHandler`). Only `avaje-record-builder` needs to be added.

### Step 2 — Create `ErrorResponse.java`

Create the file at `src/main/java/<base-package>/web/exception/ErrorResponse.java`:

```java
package <base-package>.web.exception;

import io.avaje.jsonb.Json;
import io.avaje.recordbuilder.RecordBuilder;

@Json
@RecordBuilder
public record ErrorResponse(
    int httpCode,
    String path,
    String message,
    String traceId
) {
    public static ErrorResponseBuilder builder() {
        return ErrorResponseBuilder.builder();
    }
}
```

Replace `<base-package>` with the project's root package (find it by looking at existing controller imports or `module-info.java`).

### Step 3 — Create `GlobalExceptionController.java`

Create the file at `src/main/java/<base-package>/web/exception/GlobalExceptionController.java`:

```java
package <base-package>.web.exception;

import io.avaje.http.api.Controller;
import io.avaje.http.api.ExceptionHandler;
import io.avaje.http.api.Produces;
import io.helidon.http.BadRequestException;
import io.helidon.http.InternalServerException;
import io.helidon.http.NotFoundException;
import io.helidon.webserver.http.ServerRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
final class GlobalExceptionController {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionController.class);

    private static final int HTTP_500 = 500;
    private static final int HTTP_400 = 400;
    private static final int HTTP_404 = 404;

    private static final String HTTP_500_MESSAGE = "An error occurred processing the request.";
    private static final String HTTP_404_MESSAGE = "Not found for ";

    @Produces(statusCode = HTTP_500)
    @ExceptionHandler
    ErrorResponse defaultErrorResponse(Exception ex, ServerRequest req) {
        logException(ex, path(req));
        return ErrorResponse.builder()
            .httpCode(HTTP_500)
            .path(path(req))
            .message(HTTP_500_MESSAGE)
            .build();
    }

    @Produces(statusCode = HTTP_400)
    @ExceptionHandler
    ErrorResponse badRequest(BadRequestException ex, ServerRequest req) {
        logException(ex, path(req));
        return ErrorResponse.builder()
            .httpCode(HTTP_400)
            .path(path(req))
            .message(ex.getMessage())
            .build();
    }

    @Produces(statusCode = HTTP_500)
    @ExceptionHandler
    ErrorResponse internalServerError(InternalServerException ex, ServerRequest req) {
        logException(ex, path(req));
        return ErrorResponse.builder()
            .httpCode(HTTP_500)
            .path(path(req))
            .message(HTTP_500_MESSAGE)
            .build();
    }

    @Produces(statusCode = HTTP_400)
    @ExceptionHandler(UnsupportedOperationException.class)
    ErrorResponse unsupportedOperation(UnsupportedOperationException ex, ServerRequest req) {
        return ErrorResponse.builder()
            .httpCode(HTTP_400)
            .path(path(req))
            .message(ex.getMessage())
            .traceId(null)
            .build();
    }

    @Produces(statusCode = HTTP_404)
    @ExceptionHandler(NotFoundException.class)
    ErrorResponse notFound(ServerRequest req) {
        String path = path(req);
        log.debug("404 not found path:{}", path);
        return ErrorResponse.builder()
            .httpCode(HTTP_404)
            .path(path)
            .message(HTTP_404_MESSAGE + path)
            .traceId(null)
            .build();
    }

    private static void logException(Exception ex, String path) {
        log.error("An error occurred processing request on path '{}'", path, ex);
    }

    private static String path(ServerRequest req) {
        return req != null && req.path() != null
            ? req.path().path()
            : null;
    }
}
```

### Step 4 — Key rules to follow

1. **`GlobalExceptionController` must be package-private** (`final class`, no `public`). avaje-inject discovers it from generated wiring regardless of visibility.
2. **`ErrorResponse` must be `public`** — it is part of the JSON API surface.
3. Both files go in the **same `web/exception` package** (or equivalent sub-package).
4. The `@ExceptionHandler` exception type is inferred from the first parameter; use `@ExceptionHandler(SomeException.class)` when the parameter type differs or is omitted (e.g. the 404 handler).
5. Always pair `@ExceptionHandler` with `@Produces(statusCode = N)` to set the correct HTTP status.

### Step 5 — Verify

```bash
mvn compile
```

The build must succeed with no errors from the annotation processors. Then run and test:

```bash
curl -i http://localhost:8080/no-such-path
# Expected: HTTP 404, Content-Type: application/json
```

---

## Notes (exception handler)

- The `traceId` field is always `null` in this baseline. Populate it with a distributed trace ID (e.g. from a `traceparent` header) when tracing is integrated.
- To add handlers for additional exception types, add new methods following the same pattern: `@Produces(statusCode = N)` + `@ExceptionHandler` + the exception type as first parameter.
- The `Exception` catch-all is the fallback. More-specific types always take priority.

---

## Reference (exception handler)

- Full how-to guide: [`docs/global-exception-handler.md`](docs/global-exception-handler.md)
- avaje-http `@ExceptionHandler` docs: https://avaje.io/http/
- avaje-record-builder: https://github.com/avaje/avaje-record-builder

---

## How to add a test for a controller

When asked to *"add a test"*, *"test this endpoint"*, *"write a test for my controller"*, or *"add a controller test"* to an avaje-nima project, follow these steps exactly.

### Step 1 — Confirm the test dependencies are present in `pom.xml`

The following dependency must be in `<scope>test</scope>`. It is included in all projects generated from the avaje-nima archetype, but verify before proceeding:

```xml
<!-- Starts an embedded Helidon server and wires beans for tests -->
<dependency>
  <groupId>io.avaje</groupId>
  <artifactId>avaje-nima-test</artifactId>
  <version>${avaje.nima.version}</version>
  <scope>test</scope>
</dependency>


If the dependency is missing, add it inside `<dependencies>`.

### Step 2 — Choose a testing approach

There are two ways to test a controller. Prefer **Approach 2** (generated typed API) for standard JSON endpoints; use **Approach 1** (raw `HttpClient`) when you need direct control over headers, raw body inspection, or are testing non-JSON endpoints.

| | Approach 1: `HttpClient` | Approach 2: Generated typed API |
|---|---|---|
| Inject | `HttpClient` | `HelloControllerTestAPI` |
| Response type | `HttpResponse<String>` | `HttpResponse<MyDto>` (typed) |
| Path construction | manual string | generated method per endpoint |
| Best for | raw/non-JSON, custom headers | standard CRUD/JSON endpoints |

### Step 3 — Create or locate the test class

Test classes live in `src/test/java` and mirror the main source package. If a test class already exists for the controller, open it. Otherwise create one at:

```
src/test/java/<base-package>/<ControllerName>Test.java
```

The class must carry `@InjectTest`. Declare the injections you need:

```java
package <base-package>;

import static org.assertj.core.api.Assertions.assertThat;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.Test;
import io.avaje.http.client.HttpClient;
import io.avaje.inject.test.InjectTest;
import jakarta.inject.Inject;
import <controller-package>.HelloControllerTestAPI;

@InjectTest
class HelloControllerTest {

  @Inject HttpClient client;                    // Approach 1 — raw HTTP

  @Inject HelloControllerTestAPI helloApi;      // Approach 2 — typed API
```

- `@InjectTest` starts a real Helidon server on a random port and builds the full `BeanScope` once per test class.
- `HelloControllerTestAPI` is **auto-generated** by `avaje-nima-generator` during test compilation — one interface per `@Controller` class, named `<ControllerName>TestAPI`, located in the same package as the controller.

### Step 4 — Write the test method

#### Approach 1 — `HttpClient` (untyped)

```java
@Test
void data_returnsJson() {
  HttpResponse<String> res = client.request()
    .path("hi/data")
    .GET().asString();

  assertThat(res.statusCode()).isEqualTo(200);
  assertThat(res.body()).contains("message");
  assertThat(res.body()).contains("timestamp");
}
```

**Request builder reference:**

| Scenario | Snippet |
|---|---|
| GET plain text | `.path("hi").GET().asString()` |
| GET with path parameter | `.path("hi/data/Alice").GET().asString()` |
| GET with query parameter | `.path("hi/search").queryParam("q", "foo").GET().asString()` |
| POST with JSON body | `.path("hi").body(myDto).POST().asString()` |

#### Approach 2 — Generated typed API (preferred for JSON)

The generated API exposes one method per controller endpoint. Responses are typed, so you assert on fields directly rather than parsing raw JSON strings:

```java
@Test
void dataByName_returnsTypedResponse() {
  HttpResponse<GreetingResponse> res = helloApi.dataByName("World");

  assertThat(res.statusCode()).isEqualTo(200);
  assertThat(res.body().message()).isEqualTo("World");
  assertThat(res.body().timestamp()).isGreaterThan(0);
}
```

The generated interface mirrors the controller exactly:

```java
// Generated: <controller-package>.HelloControllerTestAPI
@Client("/hi")
public interface HelloControllerTestAPI {
  @Get                   HttpResponse<String>           hi();
  @Get("/data")          HttpResponse<GreetingResponse> data();
  @Get("/data/{name}")   HttpResponse<GreetingResponse> dataByName(String name);
}
```

### Step 5 — Verify

```bash
mvn test
```

The build should complete and all tests should pass:

```
Tests run: N, Failures: 0, Errors: 0, Skipped: 0
```

---

## Notes (controller testing)

- `@InjectTest` starts an embedded server on a **random port** — never hard-code a port number in tests.
- `HttpClient` and `HelloControllerTestAPI` can be injected as `static` if desired for class-level lifecycle.
- The `TestAPI` interface is generated into `target/generated-test-sources` during `mvn test-compile`. It will not exist on a clean checkout until at least one compilation has run.
- The `HttpResponse` type used by avaje HttpClient is JDK `java.net.http.HttpResponse`.

---

## Reference (controller testing)

- Full how-to guide: [`docs/controller-testing.md`](docs/controller-testing.md)
- avaje-http client docs: https://avaje.io/http-client/
- avaje-inject test docs: https://avaje.io/inject/#testing
