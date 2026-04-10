# Guide: Add a Native Image Docker Build with Jib

## Purpose

This guide provides step-by-step instructions for adding a `native` Maven profile to
an **avaje-nima** project that:
1. Compiles the application to a **GraalVM native executable** (no JVM at runtime)
2. Packages that executable into a **Docker image** using [Jib](https://github.com/GoogleContainerTools/jib)

The result is a small, fast-starting container. Activate it with:

```bash
mvn package -Pnative
```

When asked to *"add a native Docker build"*, *"add GraalVM native image support"*, or
*"add the native profile"* to an avaje-nima project, follow these steps exactly.

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
- The JVM Docker build is **not** required first, but the same `pom.xml` can contain both.

### Native vs. JVM image comparison

| | JVM image | Native image |
|---|---|---|
| Startup time | ~1–2 s | ~50–200 ms |
| Memory footprint | Higher (JIT profiling metadata) | Lower |
| Build time | Fast | Slow (3–10 minutes) |

Native image suits latency-sensitive or cost-sensitive deployments where fast startup
and low idle memory matter more than build time.

---

## Step 1 — Confirm GraalVM is available

The native build requires GraalVM JDK. Note this when generating instructions — the
developer must run `mvn package -Pnative` with GraalVM active (e.g. via
`sdk use java 25.0.2-graal`).

---

## Step 2 — Locate the insertion point in `pom.xml`

Find the `<profiles>` section (or the closing `</project>` tag if none exists). The
entire native configuration lives inside a single `<profile>` block:

```xml
<profiles>
  <profile>
    <id>native</id>
    ...
  </profile>
</profiles>
```

If `<profiles>` does not exist, add it before `</project>`.

---

## Step 3 — Insert the native profile XML

Insert the following complete profile. **Replace `com.example.Main`** with the
project's actual main class (find it by searching for `public static void main` or
`void main()` in the source tree, or look for an existing `<mainClass>` reference in
the pom).

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

## Step 4 — Adjust values if needed

| Setting | Default | When to change |
|---|---|---|
| `<mainClass>com.example.Main</mainClass>` | — | **Always replace** with the project's actual main class; appears in **both** plugins |
| `native-maven-plugin` version `0.11.4` | `0.11.4` | Update to the latest compatible release |
| `jib-maven-plugin` version `3.5.1` | `3.5.1` | Match the version used in the default JVM build |
| `redhat/ubi10-micro:10.1-1762215812` | UBI 10 micro | Use a newer digest tag if available; keep `ubi*-micro` for glibc support |
| `<ports>8080</ports>` | 8080 | Change to match the application's actual HTTP port |
| Image tag suffix `-native` | `-native` | Keep this suffix to distinguish from the JVM image |

### `native-maven-plugin` build args

| Arg | Purpose |
|---|---|
| `--no-fallback` | Fail the build if native compilation fails — do not silently fall back to JVM mode |
| `-march=compatibility` | Generate code compatible with a broad range of x86-64 CPU microarchitectures |
| `--static-nolibc` | Produce a mostly-static binary: all libraries linked statically **except** glibc, which is linked dynamically |
| `--emit build-report` | Write a `target/native/nativeCompile/build-report.html` for inspection |

### Jib configuration

| Setting | Rationale |
|---|---|
| `JibNativeImageExtension` | Tells Jib to package the native binary produced by `native-maven-plugin` instead of a JAR |
| `<imageName>${project.artifactId}</imageName>` | Name of the binary inside the container |
| `redhat/ubi10-micro` base image | Minimal RHEL UBI 10 image; provides glibc required by `--static-nolibc` binaries |
| `<mainClass>` in `<container>` | Must match the `mainClass` in `native-maven-plugin` |

> **Why UBI micro?** The `--static-nolibc` flag links everything statically except
> glibc. A distroless or `scratch` base would fail at runtime because glibc is missing.
> UBI micro provides glibc in a minimal footprint.

---

## Step 5 — Verify the addition

Check that:
1. The `<profile id="native">` block is inside `<profiles>` at the top level of
   `<project>`, **not** inside `<build>`.
2. `<mainClass>` is set to the same value in **both** `native-maven-plugin` and Jib's
   `<container>` section.
3. There is no duplicate `jib-maven-plugin` entry inside this profile.
4. The XML is well-formed.

---

## Step 6 — Confirm the build works

```bash
mvn package -Pnative
```

The build compiles a native binary and then builds a Docker image. This typically
takes **3–10 minutes**. Verify:

```bash
docker images | grep native
```

Run the container:

```bash
docker run --rm -p 8080:8080 <artifactId>-native:<version>
```

Test with curl:

```bash
curl http://localhost:8080/health
```

Startup should complete in under 200 ms.

---

## Relationship to the JVM Docker build

The `native` profile is **additive** — it does not replace the default JVM build.
Both coexist in the same `pom.xml`:

| Build command | Image produced | When to use |
|---|---|---|
| `mvn package` | `<artifactId>:<version>` | Development, fast iterations |
| `mvn package -Pnative` | `<artifactId>-native:<version>` | Production, low-latency deployments |

See [`add-jvm-docker-jib.md`](add-jvm-docker-jib.md) for the JVM build configuration.

---

## Notes

- The `native` profile is **additive** — it does not replace the default JVM build.
- The `--static-nolibc` build arg creates a mostly-static binary that links glibc
  dynamically. The `ubi10-micro` base image provides glibc, which is why it is chosen
  over `scratch` or distroless images.
- No `<jvmFlags>` are needed in the Jib container config — the container runs a native
  binary, not a JVM process.
- The `JibNativeImageExtension` locates the binary produced by `native-maven-plugin`
  automatically; the `<imageName>` property sets the binary's name inside the image.

---

## Version compatibility

| Component | Tested version |
|---|---|
| `native-maven-plugin` | 0.11.4 |
| `jib-maven-plugin` | 3.5.1 |
| `jib-native-image-extension-maven` | 0.1.0 |
| Base image | `redhat/ubi10-micro:10.1-1762215812` |
| GraalVM / Java | 25 |
| Helidon SE | 4.4.0 |
| avaje-nima | 1.8 |

---

## References

- Jib native image extension: https://github.com/GoogleContainerTools/jib-extensions/tree/master/first-party/jib-native-image-extension-maven
- GraalVM native build tools: https://graalvm.github.io/native-build-tools/latest/maven-plugin.html
- Red Hat UBI micro images: https://catalog.redhat.com/software/containers/ubi10/ubi-micro
- JVM Docker build: [`add-jvm-docker-jib.md`](add-jvm-docker-jib.md)
