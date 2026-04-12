# Guides

Step-by-step guides written as instructions for AI agents and developers working in
**avaje-nima** projects.

## Project context

**avaje-nima** projects are Java applications built on:
- [Helidon SE](https://helidon.io/) virtual-thread HTTP server
- [avaje-inject](https://avaje.io/inject/) for dependency injection
- [avaje-jsonb](https://avaje.io/jsonb/) for JSON serialisation
- Maven as the build tool
- Java 21+ (typically Java 25)

Applications listen on port **8080** by default.

---

## Getting Started

| Guide | Description |
|-------|-------------|
| [Archetype Getting Started](archetype-getting-started.md) | Use the `io.avaje.archetype:avaje-nima-archetype-minimal-rest` Maven archetype to scaffold a new avaje-nima REST service; includes generation, project structure, compilation, customization patterns, and AI agent command patterns |

---

## Docker builds

| Guide | Description |
|-------|-------------|
| [Add a JVM Docker build with Jib](add-jvm-docker-jib.md) | Add `jib-maven-plugin` to `pom.xml` so `mvn package` builds a local Docker image for the JVM application |
| [Add a native image Docker build with Jib](add-native-docker-jib.md) | Add a `native` Maven profile that compiles a GraalVM native binary and packages it into a Docker image with Jib |

## Web layer

| Guide | Description |
|-------|-------------|
| [Add a global exception handler](add-global-exception-handler.md) | Add `ErrorResponse` + `GlobalExceptionController` to catch all exceptions, return structured JSON, and set correct HTTP status codes |
| [Add a controller test](add-controller-test.md) | Write integration tests for a controller using `avaje-nima-test` and `@InjectTest`; covers raw `HttpClient` and the generated typed API |

---

## Helping AI agents find these guides

AI coding agents can only follow these guides if they know they exist. Below are
copy-paste snippets for the most common AI tooling configurations.


### Project `README.md` (universal)

```markdown
## AI Agent Instructions

This project uses [avaje-nima](https://avaje.io/nima/). Step-by-step guides for
common tasks (Docker builds, exception handling, controller testing) are at:

**https://github.com/avaje/avaje-nima/tree/main/docs/guides/**
```

### GitHub Copilot — `.github/copilot-instructions.md`

`docs/guides/README.md` (this file) is the single source of truth for AI agent
instructions in this repository. For **your project** that uses avaje-nima as a
dependency, add the following to your `.github/copilot-instructions.md`:

```markdown
## avaje-nima

This project uses [avaje-nima](https://avaje.io/nima/). Step-by-step guides for
common tasks are at: https://github.com/avaje/avaje-nima/tree/main/docs/guides/

Key guides (fetch and follow these when performing the relevant task):
- Archetype scaffolding: https://raw.githubusercontent.com/avaje/avaje-nima/main/docs/guides/archetype-getting-started.md
- JVM Docker build: https://raw.githubusercontent.com/avaje/avaje-nima/main/docs/guides/add-jvm-docker-jib.md
- Native Docker build: https://raw.githubusercontent.com/avaje/avaje-nima/main/docs/guides/add-native-docker-jib.md
- Global exception handler: https://raw.githubusercontent.com/avaje/avaje-nima/main/docs/guides/add-global-exception-handler.md
- Controller testing: https://raw.githubusercontent.com/avaje/avaje-nima/main/docs/guides/add-controller-test.md
```

### Claude Code — `CLAUDE.md`

Same content as above — Claude Code reads `CLAUDE.md` at the project root.

### Cursor — `.cursor/rules/avaje-nima.mdc`

```markdown
---
description: avaje-nima task guidance
globs: ["**/*.java", "**/pom.xml"]
alwaysApply: false
---

## avaje-nima

This project uses avaje-nima (Helidon SE + avaje-inject). Before performing any
avaje-nima-related task, fetch and follow the relevant guide from:
https://github.com/avaje/avaje-nima/tree/main/docs/guides/
```
