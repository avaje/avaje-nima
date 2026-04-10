# Building a JVM Docker Image with Jib

This guide explains how to add a [Jib](https://github.com/GoogleContainerTools/jib) Maven plugin configuration to an **avaje nima** project so that `mvn package` automatically builds a Docker image for the JVM (non-native) application — without writing a `Dockerfile`.

---

## Why Jib?

- **No Dockerfile required.** Jib builds an OCI-compliant image directly from your Maven project.
- **Reproducible, layered images.** Dependencies, resources, and classes are placed in separate layers for efficient rebuilds and pulls.
- **Daemon or registry.** Use `dockerBuild` to push to the local Docker daemon, or `build` to push directly to a registry.

---

## Prerequisites

- Docker Desktop (or Docker Engine) running locally.
- JDK 21+ (the project uses Java 25 / virtual threads via Helidon SE).
- Maven 3.9+.

---

## The Jib Plugin Configuration

Add the following plugin inside the `<build><plugins>` section of your `pom.xml`. Place it **after** the `avaje-inject-maven-plugin` entry.

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

### Configuration decisions

| Setting | Value | Rationale |
|---|---|---|
| `<goal>dockerBuild</goal>` | Build to local Docker daemon | Use `build` instead to push directly to a registry |
| `<phase>package</phase>` | Runs on `mvn package` | Keeps the image build part of the standard lifecycle |
| Base image | `amazoncorretto:25-al2023-headless` | Amazon Corretto JDK 25 on Amazon Linux 2023; `headless` omits AWT/GUI libs |
| `-XX:MaxRAMPercentage=70` | Heap = 70% of container memory limit | Container-memory-aware; no explicit `-Xmx` needed |
| `-XX:+UseG1GC` | G1 garbage collector | Good general-purpose GC; low-pause, well-tuned for server workloads |
| `<ports>8080</ports>` | HTTP port | Matches Helidon's default port |
| Image tag | `${project.artifactId}:${project.version}` | Names the image after the Maven artifact |

> **ZGC alternative:** If you need ultra-low pause times and are comfortable setting explicit heap bounds, replace the G1GC flags with ZGC (see the commented-out block above). ZGC requires an explicit `-Xmx`.

---

## Building the Image

```bash
mvn package
```

Jib builds the image during the `package` phase. No separate step is needed.

Verify the image was created:

```bash
docker images | grep <artifactId>
```

---

## Running the Container

```bash
docker run --rm -p 8080:8080 <artifactId>:<version>
```

Test with curl:

```bash
curl http://localhost:8080/health
```

---

## Controlling Memory at Runtime

Because the JVM flags use `MaxRAMPercentage` rather than a fixed heap size, you control actual heap size by setting the container's memory limit:

```bash
# Allow 512 MB → JVM uses up to ~358 MB heap
docker run --rm -p 8080:8080 --memory=512m <artifactId>:<version>
```

---

## Native Image Docker Build

Refer to docs/jib-native-docker.md

---

## Version Compatibility

| Component | Tested version |
|---|---|
| `jib-maven-plugin` | 3.5.1 |
| Base image | `amazoncorretto:25-al2023-headless` |
| Java | 25 |
| Helidon SE | 4.4.0 |
| avaje-nima | 1.8 |
