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

- Full how-to guide: [`docs/jib-jvm-docker.md`](docs/jib-jvm-docker.md)
- Jib documentation: https://github.com/GoogleContainerTools/jib/tree/master/jib-maven-plugin
- Amazon Corretto images: https://hub.docker.com/_/amazoncorretto
