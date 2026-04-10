# Building a Native Image Docker Container with GraalVM and Jib

This guide explains how to add a `native` Maven profile to an **avaje-nima** project that:
1. Compiles the application to a **GraalVM native executable** (no JVM at runtime)
2. Packages that executable into a **Docker image** using [Jib](https://github.com/GoogleContainerTools/jib)

The result is a small, fast-starting container with no JVM overhead. Activate it with:

```bash
mvn package -Pnative
```

---

## Why native image?

| | JVM image                             | Native image |
|---|---------------------------------------|---|
| Startup time | ~1–2 s                                | ~50–200 ms |
| Memory footprint | Higher (C1 C2 JIT profiling metadata) | Lower |
| Build time | Fast                                  | Slow (minutes) |

Native image suits latency-sensitive or cost-sensitive deployments where fast startup and low idle memory matter more than build time.

---

## Prerequisites

- **GraalVM JDK 25** (or matching your `maven.compiler.release`). Install via [SDKMAN](https://sdkman.io/):

  ```bash
  sdk install java 25.0.2-graal
  sdk use java 25.0.2-graal
  ```

  Confirm GraalVM is active:

  ```bash
  mvn --version   # JVM line should mention GraalVM
  ```

- Docker Desktop (or Docker Engine) running locally.
- Maven 3.9+.

---

## The `native` Maven Profile

Add the following profile inside `<profiles>` in your `pom.xml`. Replace `com.example.Main` with your application's actual main class.

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

---

## Configuration decisions

### `native-maven-plugin` build args

| Arg | Purpose |
|---|---|
| `--no-fallback` | Fail the build if native compilation fails — do not silently fall back to JVM mode |
| `-march=compatibility` | Generate code compatible with a broad range of x86-64 CPU microarchitectures |
| `--static-nolibc` | Produce a mostly-static binary: all libraries linked statically **except** glibc, which is linked dynamically |
| `--emit build-report` | Write a `target/native/nativeCompile/build-report.html` for inspection |

> **G1GC at runtime (Linux + non-community GraalVM only):** You can optionally add `-R:MaxGCPauseMillis=50` and `-R:MaxHeapSize=400m` via `--gc=G1`, but this requires a non-community GraalVM and Linux. Leave commented out for maximum portability.

### Jib configuration

| Setting | Value | Rationale |
|---|---|---|
| Extension | `JibNativeImageExtension` | Tells Jib to package the native binary produced by `native-maven-plugin` instead of a JAR |
| `<imageName>` property | `${project.artifactId}` | Name of the binary inside the container |
| Base image | `redhat/ubi10-micro:10.1-1762215812` | Minimal RHEL UBI 10 image; provides glibc required by `--static-nolibc` binaries |
| `<mainClass>` in `<container>` | Your app's main class | Must match the `mainClass` in `native-maven-plugin` |
| `<ports>8080</ports>` | 8080 | Matches Helidon's default HTTP port |
| Image tag | `${project.artifactId}-native:${project.version}` | Includes `-native` suffix to distinguish from the JVM image |

> **Why UBI micro?** The `--static-nolibc` flag links everything statically except glibc. A distroless or `scratch` base would fail at runtime because glibc is missing. UBI micro provides glibc in a minimal footprint.

---

## Building the Native Docker Image

```bash
mvn package -Pnative
```

This runs both plugins in sequence during the `package` phase:
1. `native-maven-plugin` compiles the native binary to `target/<artifactId>`
2. Jib (`JibNativeImageExtension`) picks up that binary and builds the Docker image

Build times are typically **3–10 minutes** depending on application size.

Verify the image was created:

```bash
docker images | grep native
```

---

## Running the Container

```bash
docker run --rm -p 8080:8080 <artifactId>-native:<version>
```

Test with curl:

```bash
curl http://localhost:8080/health
```

Startup is typically under 200 ms.

---

## Relationship to the JVM Docker Build

The `native` profile is independent of the default JVM build. Both can coexist in the same `pom.xml`:

| Build command | Image produced | When to use |
|---|---|---|
| `mvn package` | `<artifactId>:<version>` | Development, fast iterations |
| `mvn package -Pnative` | `<artifactId>-native:<version>` | Production, low-latency deployments |

See [`docs/jib-jvm-docker.md`](jib-jvm-docker.md) for the JVM build configuration.

---

## Version Compatibility

| Component | Tested version |
|---|---|
| `native-maven-plugin` | 0.11.4 |
| `jib-maven-plugin` | 3.5.1 |
| `jib-native-image-extension-maven` | 0.1.0 |
| Base image | `redhat/ubi10-micro:10.1-1762215812` |
| GraalVM / Java | 25 |
| Helidon SE | 4.4.0 |
| avaje-nima | 1.8 |
