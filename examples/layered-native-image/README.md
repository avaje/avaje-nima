# Layered Native Image Example

This example shows how to build an `avaje-nima` application as:

1. A normal standalone GraalVM native executable
2. A layered native image using a reusable `java.base` layer

The layered build is intended for a **Linux GitHub Actions runner** using a recent
**Oracle GraalVM EA** build. The standalone build works on macOS too, but layered
image creation does not currently work on `darwin/aarch64`.

This example is intentionally **not** included in the top-level Maven reactor. Build it
directly when you want to exercise it.

If you want the example to use the **current checkout** of `avaje-nima` and
`avaje-nima-generator`, install those modules first:

```bash
mvn -pl avaje-nima,avaje-nima-generator -am -DskipTests install
```

## Layout

- `pom.xml` - the main `avaje-nima` application
- `base-layer/pom.xml` - builds the reusable `java.base` layer archive
- `src/main/java/` - the example application sources

## Prerequisites

- Java 21+
- Maven 3.9+
- GraalVM with `native-image`
- For layered builds: a **Linux** runner and a recent **GraalVM EA**

## Build

Build the example module on the JVM:

```bash
mvn -f examples/layered-native-image/pom.xml clean package
```

Build the standalone native executable:

```bash
mvn -f examples/layered-native-image/pom.xml -Pstandalone clean package
```

Build the base layer:

```bash
mvn -f examples/layered-native-image/base-layer/pom.xml clean install
```

Build the application layer:

```bash
mvn -f examples/layered-native-image/pom.xml -Papp-layer clean package
```

## Run

Standalone executable:

```bash
./examples/layered-native-image/target/standalone-app
```

Layered executable:

```bash
./examples/layered-native-image/app-layer-target/layered-app
```

Then call:

```bash
curl http://localhost:8080/hi
curl http://localhost:8080/hi/data
```

## Notes

- The base layer uses `-H:LayerCreate=...` to create a reusable `.nil` archive.
- The application layer uses `-H:LayerUse=...` to build a thin executable on top of
  that archive.
- `-H:LinkerRPath=$ORIGIN` assumes a Linux runner and keeps the shared layer next to
  the executable.
