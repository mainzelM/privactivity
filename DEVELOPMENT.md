# Development Guide

## Architecture Overview

### Gradle Subprojects

- `privactivity-domain/` — shared domain model and value objects
- `privactivity-store-domain/` — use cases and domain model of the privactivity application
- `geojson-domain/` — GeoJSON use cases and domain model
- `microstream-repositories/` — repositories based on Eclipse MicroStream
- `metadata-repository/` — repositories based on JSON files
- `fit-decoder-adapter/` — Garmin FIT decoder adapter
- `gpx-importer-adapter/` — GPX import adapter
- `tcx-importer-adapter/` — TCX import adapter
- `gpx-export-adapter/` — GPX export adapter
- `geotools-town-finder-adapter/` — GeoTools-based town-finder adapter
- `activities-json-adapter/` — JSON activities import adapter
- `privactivity-service/` — service layer and orchestration
- `privactivity-store-rest/` — REST controllers and DTOs
- `privactivity-app/` — main Spring Boot application
- `privactivity-jmh/` — JMH benchmarks
- `angular/` — Angular frontend

### Key Technologies

- **EclipseStore** (not H2/PostgreSQL) - Object database with separate storage managers per module
- **Garmin FIT SDK** - For decoding proprietary Garmin fitness files
- **Spring Boot 4** with OAuth2 JWT security
- **Angular 19** with Material UI and Leaflet maps
- **Gradle multi-project** build with custom conventions

### Layering & Responsibilities

The application follows **clean architecture** / **onion** principles with clear separation of concerns across the
following layers. The layers are listed from outside the onion to the inside. Only inwards dependencies are allowed.

#### Application (`privactivity-app`)

**Purpose**: Application entry point

**Responsibilities**:

- Provides the application as deployable artifacts
- Hosts the static configuration

**Prohibited**:

- ❌ Java code except for the entry point

#### REST Endpoints (`privactivity-store-rest`)

**Purpose**: HTTP protocol adapter layer

**Responsibilities**:

- Convert HTTP requests (parameters, body, headers) to Java domain objects
- Convert Java domain objects to HTTP responses (JSON, XML, status codes)
- Input validation (basic format checks)
- HTTP-specific concerns (content negotiation, caching headers)

**Prohibited**:

- ❌ Business logic
- ❌ Authorization checks
- ❌ Transaction management
- ❌ Direct database/storage access
- ❌ Complex data transformations

**Example**:

```java

@RestController
@RequestMapping("/activities")
public class PrivactivityResource {
    private final PrivactivityStoreService service;

    @GetMapping("")
    public List<ActivityDTO> findActivities(ActivityFilterRequest request) {
        Filter filter = mapFilter(request);
        boolean includeWaypoints = Boolean.TRUE.equals(request.includeWaypoints());
        List<Activity> activities = service.getAllActivities(includeWaypoints, filter, "date", SortDirection.DESC);

        return activities.stream()
                         .map(ActivityDTO::new)
                         .collect(Collectors.toList());
    }
}
```

#### Adapter (in `-adapter` subprojects)

**Purpose**: Infrastructure and integration layer

**Responsibilities**:

- Implement interfaces defined by domain layer
- Wrap storage, file formats, and external libraries
- Translate infrastructure-specific data into domain objects
- Keep framework and vendor code out of the domain layer

**Examples**:

- `microstream-repositories/` and `metadata-repository/` for EclipseStore persistence
- `fit-decoder-adapter/`, `gpx-importer-adapter/`, and `tcx-importer-adapter/` for importers
- `gpx-export-adapter/` for GPX export
- `geotools-town-finder-adapter/` for GeoTools-based town lookup

**Prohibited**:

- ❌ Business logic
- ❌ HTTP concerns
- ❌ Direct orchestration of services

#### Services (`privactivity-service` subproject)

**Purpose**: Cross-functional orchestration layer

**Responsibilities**:

- Authorization and authentication checks
- Transaction management (`@Transactional`)
- Audit logging
- Performance monitoring
- Orchestration of multiple use cases
- Error handling and exception translation
- Instantiate UseCase object during service method invocation

**Prohibited**:

- ❌ Business logic (domain rules, calculations)
- ❌ HTTP-specific concerns
- ❌ Direct domain model manipulation
- ❌ Fields that hold UseCase objects

**Example**:

```java

@Service
public class PrivactivityStoreService {
    private final ActivityRepository activityRepository;
    private final MetaDataRepository metaDataRepository;

    public PrivactivityStoreService(ActivityRepository activityRepository,
                                    MetaDataRepository metaDataRepository) {
        this.activityRepository = activityRepository;
        this.metaDataRepository = metaDataRepository;
    }

    @Transactional(readOnly = true)
    public List<Activity> getAllActivities(boolean includeWaypoints, Filter filter,
                                           String sortBy, SortDirection sortDirection) {
        FindActivitiesUseCase useCase = new FindActivitiesUseCase(activityRepository, metaDataRepository);
        return useCase.findFilteredAndSortedActivities(includeWaypoints, filter, sortBy, sortDirection);
    }
}
```

#### Domain (in `-domain` subprojects)

**Purpose**: Business logic layer

**Responsibilities**:

- Entry point is always via a UseCase class
- Actual business logic and domain rules
- Complex domain operations
- Data filtering, sorting, aggregation
- Domain-specific calculations
- Coordination with adapters (repositories)
- Define adapter interfaces

**Prohibited**:

- ❌ HTTP concerns (request/response objects)
- ❌ Framework-specific annotations (except dependency injection)
- ❌ Authorization logic
- ❌ Transaction management

**Example**:

```java
public class FindActivitiesUseCase {
    private final ActivityRepository activityRepository;
    private final MetaDataRepository metaDataRepository;

    public List<Activity> findFilteredActivities(boolean attachWaypoints, Filter filter) {
        List<Activity> activities = activityRepository.getAll(attachWaypoints);

        if (filter == null) {
            return activities;
        }

        return activities.stream()
                         .filter(filter::apply)
                         .map(this::addMetaData)
                         .toList();
    }

    private Activity addMetaData(Activity activity) {
        Optional<MetaData> md = metaDataRepository.getMetaDataForActivity(activity);
        return md.map(data -> activity.withTitle(data.manualTitle().orThrow()))
                 .orElse(activity);
    }
}
```

#### Layer Dependencies

According to onion architecture

**Key Principles**:

- Each layer only knows about the layer directly below it
- Domain should be framework-agnostic (testable without Spring)
- REST endpoints never call Domain but only Services
- Services never contain business logic
- Do not use `static` methods unless the method has genuine static context (no dependency on instance state, and no
  logical reason to be overridden). Utility-style helpers that logically belong to a class instance must be instance
  methods.

## Development Workflow

### Build & Run Commands

```bash
# Development server (with Angular rebuild)
./bootRun.sh                           # Runs with --spring.profiles.active=dev

# Build everything including Angular
./gradlew build                        # Builds all modules + Angular frontend


# Docker image
./build-docker-image.sh               # Creates Docker image and pushes
```

## REST API Structure

All REST controllers are **prefixed with `/api`** via `RestConfig.configurePathMatch()`.

### Endpoint Catalog

| Method | Path                                   | Purpose                                                                                                                                                                                                                        |
|--------|----------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| GET    | `/api/activities`                      | List activities (supports filter/sort query params such as `includeWaypoints`, `minDistance`, `maxDistance`, `minAscent`, `maxAscent`, `lat`, `lon`, `radius`, `title`, `tileId`, `sortBy`, `sortDirection` with `asc`/`desc`) |
| GET    | `/api/activities/{trackId}`            | Get one activity                                                                                                                                                                                                               |
| GET    | `/api/activities/{activityId}/export`  | Export one activity as GPX (`application/xml`), with optional `speedFactor` and `startDiffHours`                                                                                                                               |
| POST   | `/api/activities/{trackId}/export.csv` | Export one activity as CSV (`text/csv`) with optional JSON body config                                                                                                                                                         |
| DELETE | `/api/activities`                      | Remove all activities                                                                                                                                                                                                          |
| GET    | `/api/aggregations`                    | Total aggregations (optional `months` query param as comma-separated month numbers)                                                                                                                                            |
| GET    | `/api/aggregations/ytd`                | Year-to-date aggregations                                                                                                                                                                                                      |
| POST   | `/api/auth`                            | Issue JWT token for authenticated user                                                                                                                                                                                         |
| GET    | `/api/climbs`                          | List climbs                                                                                                                                                                                                                    |
| POST   | `/api/climbs`                          | Create climb                                                                                                                                                                                                                   |
| DELETE | `/api/climbs`                          | Remove all climbs                                                                                                                                                                                                              |
| GET    | `/api/climb-pointers`                  | List all climb pointers                                                                                                                                                                                                        |
| GET    | `/api/climb-pointers/{activityId}`     | Get climb pointers of one activity                                                                                                                                                                                             |
| POST   | `/api/climb-pointers/{activityId}`     | Replace/add climb pointers for one activity                                                                                                                                                                                    |
| DELETE | `/api/climb-pointers`                  | Remove all climb pointers                                                                                                                                                                                                      |
| GET    | `/api/geojson/{id}`                    | Get one activity as GeoJSON (optional `climbs=true\|false`)                                                                                                                                                                    |
| GET    | `/api/maxpower`                        | Max power metrics across all activities                                                                                                                         |
| GET    | `/api/maxpower/{activityId}`           | Max power metrics for one activity                                                                                                                                                                                             |
| GET    | `/api/tiles`                           | Tile summary (query params: `metric`, optional `south`, `north`, `west`, `east`)                                                                                                                                               |
| GET    | `/api/townfinder/{activityId}`         | Main towns for one activity                                                                                                                                                                                                    |
| GET    | `/api/townfinder/{activityId}/bounds`  | Towns for activity bounds                                                                                                                                                                                                      |
| GET    | `/api/townfinder/{activityId}/corners` | Towns for activity corners                                                                                                                                                                                                     |
| POST   | `/api/townfinder/addMissing`           | Add missing town metadata                                                                                                                                                                                                      |
| GET    | `/api/admin/system-info`               | Runtime/system info (**ADMIN** role required)                                                                                                                                                                                  |
| POST   | `/api/admin/import-all`                | Import all activities (**ADMIN**, optional `removeAllBeforeImport`)                                                                                                                                                            |
| POST   | `/api/admin/clear-cache`               | Trigger cache cleanup/GC (**ADMIN**)                                                                                                                                                                                           |
| POST   | `/api/admin/rebuild-tiles`             | Rebuild tiles snapshot (**ADMIN**)                                                                                                                                                                                             |
| POST   | `/api/admin/addMissingTowns`           | Add missing towns and return counts (**ADMIN**)                                                                                                                                                                                |
| POST   | `/api/admin/change-password`           | Change current authenticated user's password (**ADMIN**)                                                                                                                                                                       |

## Security Architecture (Summary)

### Security Stack

- Spring Security with stateless sessions (`SessionCreationPolicy.STATELESS`)
- OAuth2 Resource Server for Bearer JWT validation
- HTTP Basic authentication for login/token issuance
- CSRF protection enabled via `CookieCsrfTokenRepository`

### Authentication Flow

1. Client calls `GET /api/auth/csrf` to receive a CSRF token.
2. Client calls `POST /api/auth` with Basic credentials + `X-XSRF-TOKEN`.
3. Backend returns a signed JWT containing the authenticated user's authorities.
4. Client uses `Authorization: Bearer <token>` for subsequent `/api/**` requests.

### Authorization Model

- `/api/auth/csrf` is public (token bootstrap endpoint)
- `/api/auth/**` requires authentication
- `/api/admin/**` requires `ROLE_ADMIN`
- `/actuator/**` requires `ROLE_ADMIN` (except `/actuator/health`, which is public)
- all other `/api/**` endpoints require authentication
- non-API routes are public (static frontend resources)

Method security is enabled (`@EnableMethodSecurity`) and should be used as defense in depth in addition to
HTTP-level route protection.

### Authorities and JWT Mapping

- JWT authorities are read from the `scope` claim.
- Authorities are comma-delimited and used without a `SCOPE_` prefix.
- Admin checks therefore use `ROLE_ADMIN`.

### Profile and Keying Expectations

- No profile is selected by default in `application.properties`; deployments must set
  `SPRING_PROFILES_ACTIVE` (or equivalent).
- JWT key material is profile-dependent:
  - `dev`: read from configured files or generate/persist locally on first startup
  - `inttest`: generated in-memory on startup
  - `docker`: read from configured key resources; startup fails on invalid/missing keys

## Configuration & Profiles

### Spring Profiles

- **`dev`** - Development with local file paths (`application-dev.properties`)
- **`docker`** - Container deployment (`application-docker.properties`)
- **`inttest`** - Integration tests with temp directories

### JWT key management by profile

- **`dev`**: loads JWT key pair from local files, or generates and persists one on first startup:
    - `jwt.dev.private-key-path` (default `${HOME}/tmp/privactivity/jwt/dev/app.key`)
    - `jwt.dev.public-key-path` (default `${HOME}/tmp/privactivity/jwt/dev/app.pub`)
- **`inttest`**: JWT RSA key pair is generated in-memory on every startup (throwaway keys).
- **`docker`**: JWT keys must be provided via environment variables:
    - `JWT_PRIVATE_KEY_PATH` (PKCS#8 private key file)
    - `JWT_PUBLIC_KEY_PATH` (X.509 public key file)
    - Create a matching key pair with:

      ```bash
      mkdir -p "${HOME}/tmp/privactivity/jwt/docker"
      openssl genpkey -algorithm RSA -pkeyopt rsa_keygen_bits:4096 \
        -out "${HOME}/tmp/privactivity/jwt/docker/app.key"
      openssl pkey -in "${HOME}/tmp/privactivity/jwt/docker/app.key" \
        -pubout -out "${HOME}/tmp/privactivity/jwt/docker/app.pub"
      export JWT_PRIVATE_KEY_PATH="${HOME}/tmp/privactivity/jwt/docker/app.key"
      export JWT_PUBLIC_KEY_PATH="${HOME}/tmp/privactivity/jwt/docker/app.pub"
      ```
- In `docker`, startup fails if configured key files are missing or unreadable.

### Key Configuration Properties

```properties
privactivity.store.{module}.storage-directory=/path/to/storage
privactivity.fit.import.directory=/fit-import-directory
server.port=${PORT:8090}
```

## Testing

### Integration Tests

Use `@SpringBootTest` with `@ActiveProfiles("inttest")` for full Spring context:

```java

@SpringBootTest
@ActiveProfiles("inttest")
class FitImporterTest {
    @Autowired
    private ImportActivitiesService testee;

    @Autowired
    private ActivityRepository activityRepository;
}
```

### Unit Tests

Modules use **JUnit 5** + **AssertJ** for assertions:

```java

@Test
void activityContainsExpectedMetrics() {
    Activity testee = activity;

    assertThat(testee.waypoints()).hasSize(6_129);
    assertThat(testee.averages().cadence().orThrow()).isEqualTo(91);
}
```

### Testing Conventions

- The class under test should always be named `testee`
- Prefer `Optional<T>` returns and `.orThrow()` calls rather than null checks

## Frontend Integration

### Angular Developer Commands

Use Gradle tasks for all Angular work. Do not rely on globally installed `npm` or `ng`.

Typical goals and commands:

- Install/update frontend dependencies: `./gradlew :angular:npmInstall`
- Build the frontend for production: `./gradlew :angular:npm -PnpmArgs="run build"`
- Start the Angular dev server: `./gradlew :angular:ngServe`
- Run Angular tests: `./gradlew :angular:test`
- Run any Angular CLI command (for example generate component):
  `./gradlew :angular:ng -PngArgs="generate component my-feature"`
- Run any npm command directly: `./gradlew :angular:npm -PnpmArgs="<args>"`

Useful options:

- Add `-PnpmInstallFirst=true` when a command should install dependencies first.

### Angular Build Process

The Angular app in `angular/` is built into `dist/` and copied to Spring Boot's `static/` resources via Gradle:

```gradle
processResources {
    into('static') { from configurations.webApp }
}
```

### Frontend-Backend Communication

Angular services call REST APIs with `/api` prefix. Key services:

- `ActivitiesService.getActivity(id)` → `/api/activities/{id}`
- `MaxPowerService.getMaxPowerOfActivity(id)` → `/api/maxpower/{id}`
- `TownFinderService.getTownsOfActivity(id)` → `/api/townfinder/{id}`

### Domain Model

All modules work with `org.privactivity:privactivity-domain` shared library providing:

- `Activity` - Core activity with waypoints, bounds, totals, averages
- `Waypoint` - GPS point with timestamp, power, heart rate, cadence
- `Bounds` - Geographic boundaries (north, south, east, west)
- `Maybe` - Wrapper for optional values. Similar to Java's Optional but intended to be used in fields as well.

## Common Patterns

### Error Handling

Prefer `Optional<T>` returns and `.orThrow()` calls rather than null checks.
