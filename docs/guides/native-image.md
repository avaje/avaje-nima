# GraalVM Native Images

How to build nima applications as native images.

## Setup

Add native image plugin:

```xml
<plugin>
  <groupId>org.graalvm.buildtools</groupId>
  <artifactId>native-maven-plugin</artifactId>
  <version>0.10.0</version>
</plugin>
```

## Building Native Image

```bash
mvn -Pnative clean package
```

This produces `target/myapp` executable.

## Layered Native Image Example

There is a Linux-focused layered native image example in
[`examples/layered-native-image`](../../examples/layered-native-image/README.md).

The example keeps the reusable `java.base` image layer in a separate `base-layer/`
project and builds the application layer from the main example module.
It is intentionally **not** part of the top-level Maven reactor, so it only runs when
invoked explicitly.

To exercise the example against the **current checkout** of `avaje-nima`, install the
local framework modules first:

```bash
mvn -pl avaje-nima,avaje-nima-generator -am -DskipTests install
```

Build the standalone native image:

```bash
mvn -f examples/layered-native-image/pom.xml -Pstandalone clean package
```

Build the reusable base layer (Linux + GraalVM EA required):

```bash
mvn -f examples/layered-native-image/base-layer/pom.xml clean install
```

Build the layered application:

```bash
mvn -f examples/layered-native-image/pom.xml -Papp-layer clean package
```

The layered build is currently intended for Linux runners. macOS arm64 exposes the
flags in recent GraalVM builds, but layered image creation itself is not currently
supported there.

## Performance

Native images provide:

- **Startup**: < 50ms (vs 2-5 seconds for JVM)
- **Memory**: 30-50MB (vs 200-500MB for JVM)
- **No warm-up**: Full performance from first request

## Native Image Dockerfile

```dockerfile
FROM ghcr.io/graalvm/native-image:latest as build
WORKDIR /build
COPY . .
RUN mvn -Pnative clean package

FROM debian:bookworm-slim
COPY --from=build /build/target/myapp /app/myapp
EXPOSE 8080
ENTRYPOINT ["/app/myapp"]
```

## Configuration in Native Image

All configuration must be known at build time or via environment variables:

```yaml
server:
  port: ${SERVER_PORT:8080}
```

## Next Steps

- See [native image guide](../../../docs/guides/native-image.md) for detailed info
- See [troubleshooting](troubleshooting.md)
