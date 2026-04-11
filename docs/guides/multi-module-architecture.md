# Guide: Multi-Module Architecture (Model + Server + Client)

## Purpose

This guide describes a production-grade multi-module Maven architecture for REST APIs
that will be consumed by multiple Java clients. It separates public API models (DTOs)
from internal server and database details, keeping clients lightweight and decoupled from
server implementation.

When asked to:
- *"Create a new entity and endpoint"*
- *"Add support for another client variant"*
- *"Refactor the project for reusability"*
- *"Consume this API from an external Java app"*

...follow the patterns and module layout described here.

---

## When This Pattern Applies

✅ **Use this architecture if:**
- You're building a REST API that will be consumed by multiple independent Java clients
- You want clients to never depend on server logic, database code, or ORM configuration
- You need to support multiple Java versions (8 + modern) without duplicating logic
- You want type-safe HTTP clients (generated from models) instead of generic HTTP tools
- You prefer immutable Java Records over mutable POJOs

❌ **You probably don't need this if:**
- You're building a small CLI tool or internal utility
- Your API is server-only with no external consumers
- You're not planning multiple client variants

---

## Architecture Overview

The pattern uses **7 modules** organized in layers:

```
project (parent pom)
│
├── Model Layer          [PUBLIC API]
│   └── project-model
│       ├── Java Records (DTOs) — no persistence annotations
│       ├── Package: org.project.model
│       ├── Examples: Device, Driver, Fleet, OrgMachine, User
│       ├── Naming: [Noun], [Noun]Summary, [Noun]Status/Type
│       └── Consumed by: Service + all clients
│
├── Data Access Layer    [INTERNAL]
│   └── project-repository
│       ├── Ebean @Entity classes — ORM configuration
│       ├── Package: org.project.repository.data
│       ├── Naming: D* prefix (Hungarian notation for "Domain")
│       │   E.g., DOrganisationMachine (entity) vs. OrgMachine (model)
│       └── Consumed by: Service only
│
├── Business Logic Layer [INTERNAL]
│   └── project-service
│       ├── REST controllers (@RestController)
│       ├── Service layer (business logic, transactions)
│       ├── Converters (Entity ↔ Model translation)
│       ├── Query builders, validators
│       ├── Main: embedded Helidon SE server
│       └── Depends on: Repository + Model
│
└── Client Layers        [PUBLIC API]
    ├── project-client (Java 17+)
    │   ├── Generated HTTP client API
    │   ├── Package: org.project.client
    │   ├── Depends on: Model only (no server logic, no DB)
    │   └── Safe for external consumption
    │
    ├── project-client-java8 (Java 8)
    │   ├── Same API as Java 17+ client, Java 8 compatible
    │   ├── Useful for legacy codebases
    │   └── Identical logic to modern variant
    │
    └── project-device-client (optional domain-specific)
        └── Specialized client for device-only operations
```

### Dependency Flow (What imports what)

```
Clients     → Model (only)
            ↗
Service     → Model + Repository

Repository  → (Ebean, PostgreSQL, no upstream deps)

Model       → (standalone: @RecordBuilder, @Json only)
```

**Critical rule:** Clients NEVER import Repository. Repository is an implementation detail.

---

## The 4 Core Modules Explained

### 1. Model Module — The Public API Contract

**File:** `project-model/pom.xml`

**Purpose:** Define the data transfer objects (DTOs) that represent your API.

**Characteristics:**
- ✅ Java Records (immutable, no getters/setters)
- ✅ Annotated with `@RecordBuilder` (Avaje) + `@Json` (Avaje JSONB)
- ✅ No persistence annotations (`@Entity`, `@Column`, etc.)
- ✅ No database dependencies
- ✅ Can be used standalone in any Java project

**Naming Conventions:**

| Pattern | Purpose | Examples |
|---------|---------|----------|
| `[Noun]` | Full domain model | `Device`, `Driver`, `Fleet`, `User`, `OrgMachine`, `Terminal` |
| `[Noun]Summary` | Lightweight view (3–8 fields) | `DriverSummary`, `OrgMachineSummary` |
| `[Noun]Status` or `[Noun]Type` | Reference/enum data | `DeviceStatus`, `OrgMachineAssetType` |
| `[Model1][Model2]` | Association/relationship | `DeviceMachine`, `OrgMachineDevice`, `DeviceFirmware` |

**Example:**

```java
package project.model;

import io.avaje.jsonb.Json;
import io.avaje.nima.core.record.RecordBuilder;
import java.time.Instant;
import java.util.UUID;

@RecordBuilder
@Json
public record Device(
    long id,
    UUID gid,
    int version,
    String description,
    DeviceStatus status,
    Instant created,
    Instant lastModified
) {
    public static DeviceBuilder builder() {
        return DeviceBuilder.builder();
    }

    public static DeviceBuilder builder(Device from) {
        return DeviceBuilder.builder(from);
    }
}
```

**Boilerplate for every Record:**
- `public static [Record]Builder builder()` — for construction
- `public static [Record]Builder builder([Record] from)` — for copying (optional)

---

### 2. Repository Module — Internal Data Access (Hidden from Clients)

**File:** `project-repository/pom.xml`

**Purpose:** Define Ebean `@Entity` classes for database mapping.

**Characteristics:**
- ✅ Ebean `@Entity` classes with full ORM configuration
- ✅ Database-specific logic (queries, lifecycle callbacks)
- ✅ **D* prefix naming (Hungarian notation)** to distinguish from public API models
- ✅ No REST annotations; server-side only
- ✅ Consumed exclusively by the Service layer

**Naming Convention — D* Prefix (Recommended Default):**

D stands for "Domain" (entity classes represent domain data at the database level).

| Public Model | Internal Entity |
|--------------|-----------------|
| `OrgMachine` | `DOrganisationMachine` |
| `Device` | `DDevice` |
| `User` | `DUser` |

**Why the prefix?**
- Avoids name clashing: `OrgMachine` (DTO) vs. `DOrganisationMachine` (JPA entity)
- Makes it immediately clear which is safe to expose to clients (unprefixed) vs. internal (D* prefix)
- Prevents accidental leakage of ORM implementation details

*Note:* D* is the recommended default prefix for entity classes, but you can use other prefixes as desired.

**Example:**

```java
package project.repository.data;

import io.ebean.annotation.DbDefault;
import io.ebean.annotation.WhenCreated;
import io.ebean.annotation.WhenModified;
import jakarta.persistence.*;
import lombok.Data;
import java.time.Instant;

@Data
@Entity
@Table(name = "device")
public class DDevice {

    @Id
    private long id;

    @Version
    private int version;

    @Column(length = 255)
    private String description;

    @DbDefault("'ACTIVE'")
    @Column(length = 20)
    private String status;

    @WhenCreated
    private Instant created;

    @WhenModified
    private Instant lastModified;
}
```

**Key Differences from Model:**
- ✅ Has `@Entity`, `@Table`, `@Column` annotations
- ✅ Uses Lombok for getters/setters
- ✅ Includes ORM lifecycle hints (`@WhenCreated`, `@WhenModified`, `@DbDefault`)
- ✅ Tailored to database schema

---

### 3. Service Module — Business Logic & REST API

**File:** `project-service/pom.xml`

**Purpose:** Implement the REST API, orchestrate Repository + Model layers.

**Characteristics:**
- ✅ REST controllers (`@RestController`) with endpoints
- ✅ Service classes (business logic, transactions)
- ✅ Converters (translate Entity → Model for clients)
- ✅ Query builders, validators, custom logic
- ✅ Main application entry point

**Layout:**

```
project-service/
├── src/main/java/nz/co/eroad/central/access/
│   ├── Main.java                          # App entry point
│   ├── web/
│   │   ├── DeviceController.java          # REST endpoints
│   │   ├── DriverController.java
│   │   └── FleetController.java
│   ├── service/
│   │   ├── DeviceService.java             # Business logic
│   │   ├── DriverService.java
│   │   └── FleetService.java
│   ├── converter/
│   │   ├── DeviceConverter.java           # Entity → Model
│   │   ├── DriverConverter.java
│   │   └── FleetConverter.java
│   ├── query/
│   │   └── [query builders, search criteria]
│   ├── repository/
│   │   └── [Spring/Ebean data access]
│   └── configuration/
│       └── [beans, security, etc.]
└── pom.xml
```

**Example Controller:**

```java
package project.service.web;

import io.avaje.nima.core.inject.Container;
import io.avaje.nima.core.http.*;
import org.project.model.Device;
import org.project.service.DeviceService;
import jakarta.inject.Inject;

@RestController
@Path("/devices")
public class DeviceController {

    private final DeviceService deviceService;

    @Inject
    DeviceController(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @Get("/:id")
    public Device getDevice(long id) {
        return deviceService.findById(id);
    }

    @Get
    public List<Device> listDevices() {
        return deviceService.list();
    }

    @Post
    public Device createDevice(Device device) {
        return deviceService.save(device);
    }
}
```

**Example Converter:**

```java
package project.service.converter;

import org.project.model.Device;
import org.project.repository.data.DDevice;

public class DeviceConverter {

    public static Device toModel(DDevice entity) {
        if (entity == null) return null;
        return new Device(
            entity.getId(),
            entity.getGid(),
            entity.getVersion(),
            entity.getDescription(),
            DeviceStatus.valueOf(entity.getStatus()),
            entity.getCreated(),
            entity.getLastModified()
        );
    }

    public static DDevice toEntity(Device model) {
        if (model == null) return null;
        DDevice entity = new DDevice();
        entity.setId(model.id());
        entity.setDescription(model.description());
        entity.setStatus(model.status().name());
        return entity;
    }
}
```

---

### 4. Client Modules — Generated HTTP Clients

**Files:**
- `project-client/pom.xml` (Java 17+)
- `project-client-java8/pom.xml` (Java 8)

**Purpose:** Provide a type-safe HTTP client for consuming the API.

**Characteristics:**
- ✅ Auto-generated from the REST API (via Maven plugin or manual)
- ✅ Depends on Model only (zero coupling to Server/Repository)
- ✅ Type-safe methods matching endpoints (e.g., `api.devices().get(id)`)
- ✅ Immutable Java Records for responses (Java 17+) or equivalent (Java 8)
- ✅ Can be used in any external Java application

**Example Client Usage:**

```java
package com.example;

import io.avaje.http.client.HttpClient;
import org.project.client.CentralAccessApi;
import org.project.model.Device;

public class MyApp {

    public static void main(String[] args) {
        HttpClient httpClient = HttpClient.builder()
            .baseUrl("http://localhost:8080")
            .build();

        CentralAccessApi api = new CentralAccessApi(httpClient);

        // Type-safe API calls
        Device device = api.devices().get(123L);
        System.out.println("Device: " + device.description());

        List<Device> all = api.devices().list();
        System.out.println("Found " + all.size() + " devices");
    }
}
```

**How to use the client in external projects:**

1. Add dependency to `pom.xml`:

```xml
<dependency>
    <groupId>org.project</groupId>
    <artifactId>project-client</artifactId>
    <version>1.0.0</version>
</dependency>
```

2. Create `HttpClient` configured with your server URL
3. Instantiate the generated API class
4. Call type-safe methods — no manual JSON parsing needed

---

## Step-by-Step Recipes

### Recipe 1: Add a New Entity + Endpoint + Model

**Scenario:** You want to add support for a new resource, e.g., "Geofence".

**Step 1: Create the public Model** (in `project-model`)

```java
package org.project.model;

import io.avaje.jsonb.Json;
import io.avaje.nima.core.record.RecordBuilder;
import java.time.Instant;
import java.util.UUID;

@RecordBuilder
@Json
public record Geofence(
    long id,
    UUID gid,
    int version,
    String name,
    String wktGeometry,
    Instant created,
    Instant lastModified
) {
    public static GeofenceBuilder builder() {
        return GeofenceBuilder.builder();
    }

    public static GeofenceBuilder builder(Geofence from) {
        return GeofenceBuilder.builder(from);
    }
}
```

**Step 2: Create the internal Entity** (in `project-repository`)

```java
package org.project.repository.data;

import io.ebean.annotation.WhenCreated;
import io.ebean.annotation.WhenModified;
import jakarta.persistence.*;
import lombok.Data;
import java.time.Instant;

@Data
@Entity
@Table(name = "geofence")
public class DGeofence {

    @Id
    private long id;

    @Version
    private int version;

    @Column(length = 255)
    private String name;

    @Column(columnDefinition = "geometry")
    private String wktGeometry;

    @WhenCreated
    private Instant created;

    @WhenModified
    private Instant lastModified;
}
```

**Step 3: Create the Converter** (in `project-service`)

```java
package org.project.converter;

import org.project.model.Geofence;
import org.project.repository.data.DGeofence;

public class GeofenceConverter {

    public static Geofence toModel(DGeofence entity) {
        if (entity == null) return null;
        return new Geofence(
            entity.getId(),
            entity.getGid(),
            entity.getVersion(),
            entity.getName(),
            entity.getWktGeometry(),
            entity.getCreated(),
            entity.getLastModified()
        );
    }

    public static DGeofence toEntity(Geofence model) {
        if (model == null) return null;
        DGeofence entity = new DGeofence();
        entity.setId(model.id());
        entity.setName(model.name());
        entity.setWktGeometry(model.wktGeometry());
        return entity;
    }
}
```

**Step 4: Create the Controller** (in `project-service`)

```java
package org.project.web;

import io.avaje.nima.core.http.*;
import org.project.model.Geofence;
import org.project.service.GeofenceService;
import jakarta.inject.Inject;

@RestController
@Path("/geofences")
public class GeofenceController {

    private final GeofenceService geofenceService;

    @Inject
    GeofenceController(GeofenceService geofenceService) {
        this.geofenceService = geofenceService;
    }

    @Get("/:id")
    public Geofence get(long id) {
        return geofenceService.findById(id);
    }

    @Get
    public List<Geofence> list() {
        return geofenceService.list();
    }

    @Post
    public Geofence create(Geofence geofence) {
        return geofenceService.save(geofence);
    }
}
```

**Step 5: Create the Service** (in `project-service`)

```java
package org.project.service;

import io.ebean.Database;
import org.project.converter.GeofenceConverter;
import org.project.model.Geofence;
import org.project.repository.data.DGeofence;
import jakarta.inject.Inject;
import java.util.List;

public class GeofenceService {

    private final Database database;

    @Inject
    GeofenceService(Database database) {
        this.database = database;
    }

    public Geofence findById(long id) {
        DGeofence entity = database.find(DGeofence.class, id);
        return GeofenceConverter.toModel(entity);
    }

    public List<Geofence> list() {
        return database.find(DGeofence.class)
            .findList()
            .stream()
            .map(GeofenceConverter::toModel)
            .toList();
    }

    public Geofence save(Geofence geofence) {
        DGeofence entity = GeofenceConverter.toEntity(geofence);
        database.save(entity);
        return GeofenceConverter.toModel(entity);
    }
}
```

**Step 6: Regenerate Clients** (Maven plugin or manual generation)

Run:
```bash
mvn clean generate-sources
```

The generated `CentralAccessApi` will now include `geofences()` methods.

**Step 7: Update integration tests** (in `project-integration-test`)

```java
@InjectTest
class GeofenceControllerTest {

    @Inject CentralAccessApi api;

    @Test
    void testCreateAndRetrieveGeofence() {
        Geofence created = api.geofences().create(
            new Geofence(0, UUID.randomUUID(), 0, "Test", "POLYGON(...)", null, null)
        );
        assertThat(created.id()).isGreaterThan(0);

        Geofence fetched = api.geofences().get(created.id());
        assertThat(fetched.name()).isEqualTo("Test");
    }
}
```

---

### Recipe 2: Add a New Client Variant (e.g., Java 11)

**Scenario:** You want to provide a client for Java 11 (between Java 8 and Java 17+).

**Step 1: Create a new Maven module**

```
project-client-java11/
├── pom.xml
├── src/main/java/nz/co/eroad/central/access/client/
│   ├── CentralAccessApi.java
│   ├── MachinesApi.java
│   └── ...
└── src/test/java/nz/co/eroad/central/access/client/
```

**Step 2: Add the module to parent `pom.xml`**

```xml
<modules>
    ...
    <module>project-client-java11</module>
</modules>
```

**Step 3: Set Java version and dependencies in `pom.xml`**

```xml
<properties>
    <maven.compiler.release>11</maven.compiler.release>
</properties>

<dependencies>
    <dependency>
        <groupId>org.project</groupId>
        <artifactId>project-model</artifactId>
        <version>1.0.0</version>
    </dependency>
    <!-- Same as Java 8 client but targeting Java 11 -->
</dependencies>
```

**Step 4: Copy client source code from Java 8 variant**

Make minimal adjustments for Java 11 features (e.g., `var` keyword if desired, local class syntax).

**Step 5: Publish to Maven repo**

```bash
mvn deploy
```

**Step 6: Update documentation**

Add reference to the new client variant in README and architecture guide.

---

## Key Principles for AI Agents

When working with this architecture, ask yourself:

1. **Is this a public API class or internal database class?**
   - Public: No prefix, lives in `project-model` package
   - Internal: D* prefix (or project-specific like C*), lives in `project-repository` package

2. **Where should this code go?**
   - Model (DTOs, immutable data): `project-model`
   - Entity mapping, queries: `project-repository`
   - Business logic, conversion, controllers: `project-service`
   - HTTP client, call orchestration: `project-client*`

3. **Should I add both a public model AND an @Entity?**
   - **Yes**, almost always. The public model is the API contract; the entity is the database representation.
   - They may have different fields, naming, or structure.
   - The Service layer translates between them.

4. **Who should import Repository?**
   - **Only the Service module.** Clients, models, and external apps should never reference the repository.

5. **How do I test this?**
   - Use the generated client API (Approach 2 from the controller testing guide).
   - Inject the generated API (`@Inject CentralAccessApi`) into tests.
   - Call type-safe methods; no manual HTTP construction.

---

## Troubleshooting

### "Cannot find CentralAccessApi"

**Cause:** Client code was not generated or dependencies are missing.

**Fix:**
1. Ensure `project-model` is in the classpath
2. Run `mvn clean generate-sources` to regenerate
3. Check that the REST controller exists and is properly annotated

### "Import DOrganisationMachine in my client"

**Cause:** Trying to use internal @Entity class in external code.

**Fix:**
- Import `OrgMachine` from `project-model` instead
- Use the converter in the service to translate between @Entity and model

### "My client can't connect to the server"

**Cause:** Wrong base URL or server not running.

**Fix:**
```java
HttpClient httpClient = HttpClient.builder()
    .baseUrl("http://localhost:8080")  // ← Correct URL + port
    .build();
```

---

## Summary Table

| Aspect | Model | Repository | Service | Client |
|--------|-------|------------|---------|--------|
| **Naming** | [Noun] | D[Noun] | (business logic) | Generated API |
| **Annotations** | @RecordBuilder, @Json | @Entity, @Table, @Column | @RestController, @Service | (generated) |
| **Database code?** | No | Yes | Yes (via Repository) | No |
| **Mutable?** | No (Records) | Yes (Lombok) | Mixed | No (Records) |
| **Imported by** | Service + Clients | Service only | (standalone) | External apps |
| **Safe to expose?** | ✅ Yes | ❌ No | ❌ No | ✅ Yes |

---

## See Also

- [Guide: Add a Controller Test](add-controller-test.md) — write integration tests using the generated client API
- [Model Naming Conventions](../../README.md) — detailed naming conventions for Records and types
- Ebean documentation — entity mapping, queries, transactions
