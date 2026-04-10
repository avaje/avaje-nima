# Guide: Add a JVM Docker Image Build with Jib

## Purpose

This guide provides step-by-step instructions for adding a
[Jib](https://github.com/GoogleContainerTools/jib) Maven plugin configuration to an
**avaje-nima** project so that `mvn package` automatically builds a Docker image for
the JVM (non-native) application — without writing a `Dockerfile`.

When asked to *"add a Docker build"*, *"containerise this app"*, or *"add Jib"* to an
avaje-nima project, follow these steps exactly.

---

## Prerequisites

- Docker Desktop (or Docker Engine) running locally.
- JDK 21+ (the project uses Java 25 / virtual threads via Helidon SE).
- Maven 3.9+.

---

## Step 1 — Locate the insertion point in `pom.xml`

Find the `<build><plugins>` section. Identify the last existing `<plugin>` entry
before the closing `</plugins>` tag. The Jib plugin must be added **inside**
`<build><plugins>...</plugins></build>`.

If an `avaje-inject-maven-plugin` entry is present, insert the Jib plugin **after** it.

---

## Step 2 — Insert the Jib plugin XML

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

---

## Step 3 — Adjust values if needed

| Setting | Default | When to change |
|---|---|---|
| `<version>3.5.1</version>` | `3.5.1` | Update to the latest stable Jib release if available |
| `amazoncorretto:25-al2023-headless` | Corretto 25 headless | Change the major version to match `maven.compiler.release` in the pom (e.g. `amazoncorretto:21-al2023-headless` for Java 21) |
| `-XX:MaxRAMPercentage=70` | 70% of container memory | Adjust if the service has unusual memory requirements |
| `<ports>8080</ports>` | 8080 | Change to match the application's actual HTTP port |
| `${project.artifactId}:${project.version}` | Maven artifact name + version | Override if a different image name/tag convention is in use |

### Configuration rationale

| Setting | Rationale |
|---|---|
| `<goal>dockerBuild</goal>` | Builds to local Docker daemon; use `build` instead to push directly to a registry |
| `<phase>package</phase>` | Runs on `mvn package` — keeps image build part of the standard lifecycle |
| `amazoncorretto:25-al2023-headless` | Amazon Corretto JDK 25 on Amazon Linux 2023; `headless` omits AWT/GUI libs |
| `-XX:MaxRAMPercentage=70` | Heap scales with container memory limit — no explicit `-Xmx` needed |
| `-XX:+UseG1GC` | Good general-purpose GC; low-pause, well-tuned for server workloads |
| `<ports>8080</ports>` | Matches Helidon's default HTTP port |

> **ZGC alternative:** Replace the G1GC flags with the commented-out ZGC block if
> ultra-low GC pause times are needed and you are comfortable setting explicit heap
> bounds. ZGC requires `-Xmx`.

---

## Step 4 — Verify the addition

Check that:
1. The `<plugin>` block is inside `<build><plugins>` and **not** inside any `<profile>`.
2. There is no duplicate `jib-maven-plugin` entry in the default build (a separate Jib
   entry may exist inside the `native` profile — that is expected and correct).
3. The XML is well-formed (no unclosed tags, correct nesting).

---

## Step 5 — Confirm the build works

```bash
mvn package
```

The build should complete and a Docker image named `<artifactId>:<version>` should
appear in the local daemon:

```bash
docker images | grep <artifactId>
```

Run the container to verify it starts:

```bash
docker run --rm -p 8080:8080 <artifactId>:<version>
```

Test with curl:

```bash
curl http://localhost:8080/health
```

---

## Controlling memory at runtime

Because the JVM flags use `MaxRAMPercentage` rather than a fixed heap size, you
control the actual heap size by setting the container's memory limit:

```bash
# Allow 512 MB → JVM uses up to ~358 MB heap
docker run --rm -p 8080:8080 --memory=512m <artifactId>:<version>
```

---

## Notes

- The `dockerBuild` goal pushes to the **local Docker daemon**. To push to a remote
  registry, replace `dockerBuild` with `build` and add
  `<to><image>registry.example.com/…</image></to>`.
- The JVM flags use `MaxRAMPercentage` so the heap scales with the container's memory
  limit — no explicit `-Xmx` is required.
- For GraalVM **native image** Docker builds, see
  [`add-native-docker-jib.md`](add-native-docker-jib.md) — that is handled separately
  via the `native` Maven profile.

---

## Version compatibility

| Component | Tested version |
|---|---|
| `jib-maven-plugin` | 3.5.1 |
| Base image | `amazoncorretto:25-al2023-headless` |
| Java | 25 |
| Helidon SE | 4.4.0 |
| avaje-nima | 1.8 |

---

## References

- Jib documentation: https://github.com/GoogleContainerTools/jib/tree/master/jib-maven-plugin
- Amazon Corretto images: https://hub.docker.com/_/amazoncorretto
- Native image Docker build: [`add-native-docker-jib.md`](add-native-docker-jib.md)
