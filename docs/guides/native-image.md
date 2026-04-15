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
