# Privactivity Activity Store - AI Coding Agent Instructions

## Reference Documentation

**For comprehensive technical documentation:**
- **[DEVELOPMENT.md](../DEVELOPMENT.md)** — Architecture overview, layering, development workflow, REST API, configuration, testing, and frontend integration
- **[README.md](../README.md)** — Project overview, installation, and quick start

### Startup Checklist (Mandatory For Every New Chat)

- Before proposing changes, read [DEVELOPMENT.md](../DEVELOPMENT.md#layering--responsibilities) for layer boundaries and coding patterns
- If the task touches `angular/`, follow the Angular Command Policy below
- If the task touches REST/Service/UseCase code, enforce the layer boundaries in DEVELOPMENT.md
- If instructions here conflict with DEVELOPMENT.md, prefer DEVELOPMENT.md

## Layer Boundaries (Non-Negotiable)

See [DEVELOPMENT.md](../DEVELOPMENT.md#layering--responsibilities) for full architectural details.

**Quick summary:**
- **REST endpoints**: HTTP adapters only—map request/response, basic validation only
- **Services**: Cross-functional concerns (auth, transactions, orchestration)—no business logic
- **Use Cases**: All business logic, domain rules, filtering, calculations—no HTTP or framework concerns
- **Adapters**: Infrastructure and integration—repositories, file formats, external libraries

**Key principle**: REST → Services → Use Cases; do not bypass layers.

## AI-Specific Coding Conventions

### Angular Command Policy

For work in `angular/`, use Gradle-managed Node tasks **only**. Do not run global `npm` or `ng` commands.

Commands:
- `./gradlew :angular:npmInstall` — Install dependencies
- `./gradlew :angular:npm -PnpmArgs="<args>"` — Run any npm command
- `./gradlew :angular:ng -PngArgs="<args>"` — Run Angular CLI
- `./gradlew :angular:ngServe` — Start dev server
- Add `-PnpmInstallFirst=true` when dependencies should be installed first

### Testing Conventions

- Always name the class under test as `testee` in test methods
- Use **JUnit 5** + **AssertJ** for assertions
- Integration tests: Use `@SpringBootTest` with `@ActiveProfiles("inttest")`
- Prefer creating real test scenarios over mocking when testing domain logic

### Code Generation Guidelines

- **REST endpoints**: Map HTTP to domain objects only—no business logic
- **Services**: Handle orchestration, authorization, transactions—not business logic
- **Use Cases**: Place all business logic here (filtering, calculations, domain operations)
- **EclipseStore**: Use `@Qualifier` when injecting multiple instances; check for null root on startup
- **Optionals**: Prefer `Maybe<T>` returns and `.orThrow()` calls over null checks
- **Domain model**: Testable without Spring; no HTTP or framework annotations in domain layer

### Module-Specific Patterns

- Each storage module has separate EclipseStore instances with `@Qualifier` beans
- Storage configuration pattern: `{module}_config`, `{module}_storage`
- REST API endpoints auto-prefixed with `/api` via `RestConfig.configurePathMatch()`

### Error Handling & Logging

- Use descriptive error messages identifying the error layer
- Log business logic errors in Use Cases, not REST endpoints

### Build & Profiles Quick Rules

- `./bootRun.sh` — Local development (auto-reloads)
- `./gradlew build` — Full build (all modules + Angular)
- Profiles: `dev` (local), `docker` (container), `inttest` (tests)