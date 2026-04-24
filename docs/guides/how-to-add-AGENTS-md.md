# How to Add AGENTS.md to Existing Projects

This guide standardizes the process for adding an AGENTS.md to any Avaje Nima (or similar) project, ensuring both AI and human developers have fast, accurate access to actionable, framework-specific instructions.

## Purpose
AGENTS.md is a developer/AI agent onboarding file. It:
- Points to official, agent/developer step-by-step guides for all major frameworks used (e.g., Avaje Nima, Ebean ORM)
- Lists key tasks (adding controllers, tests, models, migrations, etc.)
- Ensures consistency and discoverability for both AI and human contributors

## Steps
1. **Clarify the Audience and Purpose**
   - Confirm AGENTS.md is for developer/AI agent onboarding (not background jobs).
2. **Check for Reference AGENTS.md**
   - Look for AGENTS.md in sibling projects (e.g., central-notifications) and align style/content.
3. **Use the Template Below**
   - Update framework links and project-specific notes as needed.
4. **Review and Commit**
   - Get feedback from a maintainer or lead before merging.

## Template

```
# AI Agent Instructions

This project uses [avaje-nima](https://avaje.io/nima/) (Helidon SE + avaje-inject) for the web layer and [Ebean ORM](https://ebean.io) for database access.

Before performing a library-related task, fetch and follow the relevant guide below.

---

## avaje-nima

Guide index: https://github.com/avaje/avaje-nima/tree/main/docs/guides/

Key guides (fetch and follow when performing the relevant task):

- Archetype scaffolding: https://raw.githubusercontent.com/avaje/avaje-nima/main/docs/guides/archetype-getting-started.md
- Multi-module architecture: https://raw.githubusercontent.com/avaje/avaje-nima/main/docs/guides/multi-module-architecture.md
- JVM Docker build (Jib): https://raw.githubusercontent.com/avaje/avaje-nima/main/docs/guides/add-jvm-docker-jib.md
- Native image Docker build (Jib): https://raw.githubusercontent.com/avaje/avaje-nima/main/docs/guides/add-native-docker-jib.md
- Global exception handler: https://raw.githubusercontent.com/avaje/avaje-nima/main/docs/guides/add-global-exception-handler.md
- Controller testing: https://raw.githubusercontent.com/avaje/avaje-nima/main/docs/guides/add-controller-test.md

---

## Ebean ORM

Guide index: https://github.com/ebean-orm/ebean/tree/HEAD/docs/guides/

Key guides (fetch and follow when performing the relevant task):

- Maven POM setup: https://raw.githubusercontent.com/ebean-orm/ebean/HEAD/docs/guides/add-ebean-postgres-maven-pom.md
- Database configuration: https://raw.githubusercontent.com/ebean-orm/ebean/HEAD/docs/guides/add-ebean-postgres-database-config.md
- Test container setup: https://raw.githubusercontent.com/ebean-orm/ebean/HEAD/docs/guides/add-ebean-postgres-test-container.md
- Write queries with query beans: https://raw.githubusercontent.com/ebean-orm/ebean/HEAD/docs/guides/writing-ebean-query-beans.md
- Persisting and transactions: https://raw.githubusercontent.com/ebean-orm/ebean/HEAD/docs/guides/persisting-and-transactions-with-ebean.md
- Testing with TestEntityBuilder: https://raw.githubusercontent.com/ebean-orm/ebean/HEAD/docs/guides/testing-with-testentitybuilder.md
- DB migration generation: https://raw.githubusercontent.com/ebean-orm/ebean/HEAD/docs/guides/add-ebean-db-migration-generation.md
- Entity bean creation: https://raw.githubusercontent.com/ebean-orm/ebean/HEAD/docs/guides/entity-bean-creation.md
```

## Example AI Prompt

To add an AGENTS.md to this project, use the following prompt with your AI assistant:

```
Add an AGENTS.md to this project. Follow the process and template in https://github.com/avaje/avaje-nima/blob/HEAD/docs/guides/how-to-add-AGENTS-md.md. It should guide both AI and human developers to the official agent/developer guides for all major frameworks used (e.g., Avaje Nima, Ebean ORM). Use the latest links and match the style of AGENTS.md in central-notifications if available.
```

## Checklist
- [ ] Audience and purpose clarified
- [ ] Template used and links updated
- [ ] Style matches sibling projects
- [ ] Reviewed by maintainer/lead

---
_Keep this guide up to date as frameworks and best practices evolve._
