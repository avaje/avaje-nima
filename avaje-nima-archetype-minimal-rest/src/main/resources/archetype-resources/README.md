# ${artifactId}

A minimal REST service built with [avaje-nima](https://github.com/avaje/avaje-nima) (Helidon virtual-thread HTTP server).

## Endpoints

| Method | Path | Response |
|--------|------|----------|
| `GET` | `/hi` | `"hi"` — `text/plain` |
| `GET` | `/hi/data` | `{"message":"…","timestamp":…}` — `application/json` |

---

## Build

```bash
mvn compile
```

---

## Run

```bash
mvn exec:java -Dexec.mainClass=${package}.Main
```

The server starts on port **8080**.

---

## Test with curl

```bash
# Plain-text endpoint
curl http://localhost:8080/hi

# JSON endpoint
curl http://localhost:8080/hi/data
```

---

## Run tests

```bash
mvn test
```

---

## Compile to native executable

Install GraalVM via [SDKMAN](https://sdkman.io/):

```bash
sdk install java 25.0.2-graal
sdk use java 25.0.2-graal
```

Confirm GraalVM is active:

```bash
mvn --version   # should show GraalVM in the JVM line
```

Then build the native executable:

```bash
mvn package -Pnative
```

The native executable is written to `target/${artifactId}`.

Run it:

```bash
./target/${artifactId}
```

Test with curl (same endpoints, server still on port 8080):

```bash
curl http://localhost:8080/hi

curl http://localhost:8080/hi/data
```
