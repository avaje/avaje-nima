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
