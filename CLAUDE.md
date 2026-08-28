# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**bahmni-core** is a multi-module OpenMRS-based EMR system with core APIs, UI components, and administrative tools. It's built on Java 8, Maven, Spring 5.2, and OpenMRS 2.5.12.

This is a CURE International fork (cureinternational/bahmni-core) that extends Bahmni with CURE-specific features. Main development branch is `CURE-Product-Master`.

## Module Structure

The project is organized as a Maven multi-module build with the following key modules:

- **bahmnicore-api**: Core API services, DAOs, contract classes, encounter transaction handling, forms, and validation logic
- **bahmnicore-omod**: OpenMRS module wrapper and web controllers that expose APIs as REST endpoints
- **bahmni-emr-api**: EMR-specific API services
- **bahmnicore-ui**: UI layer and service classes for UI operations
- **admin**: Administrative tools including CSV import/export, configuration management, encounter import
- **reference-data**: Reference data handling
- **bahmni-mapping**: Location and provider mapping services
- **obs-relation**: Observation relationships handling
- **bahmni-test-commons**: Shared test utilities and fixtures

## Build & Development Commands

### Prerequisites
- JDK 1.8
- Maven 3.6+ (or use `./mvnw` wrapper)

### Build the entire project
```bash
./mvnw clean install
```

### Build a specific module
```bash
./mvnw clean install -pl bahmnicore-api
./mvnw clean install -pl bahmnicore-omod
./mvnw clean install -pl bahmnicore-ui
```

### Run tests for a module
```bash
./mvnw test -pl bahmnicore-api
./mvnw test -pl bahmnicore-ui
```

### Run a specific test class
```bash
./mvnw test -pl bahmnicore-api -Dtest=EncounterTransactionMapperTest
```

### Integration tests (using IT profile)
```bash
./mvnw verify -P IT
```

### Build without tests
```bash
./mvnw clean install -DskipTests
```

### Compile and check with Maven (no install)
```bash
./mvnw clean compile
```

## Code Architecture

### Layered Architecture Pattern

The codebase follows a standard service-oriented architecture:

1. **Contract Layer** (`contract/` packages): Data Transfer Objects (DTOs) for API requests/responses
2. **Service Layer** (`service/` packages): Business logic and orchestration
3. **DAO Layer** (`dao/` packages): Data Access Objects for persistence
4. **Model/Entity Layer** (`model/` packages): Domain entities (usually OpenMRS model extensions)
5. **Controller/Web Layer** (in `bahmnicore-omod`): REST endpoints that use services
6. **Resource Layer** (`resource/`): Additional resource handling for REST APIs
7. **Utility/Helper** (`util/`, `mapper/`): Transformation and helper logic

### Key Architectural Patterns

**Encounter Transaction**: The `encounterTransaction` package implements a complex transaction pattern for handling patient encounters with observations, drug orders, diagnoses, and encounters in a single transaction. The `EncounterTransactionMapper` is central to this pattern.

**Service-based Extensions**: Many services follow an interface-implementation pattern with OpenMRS service wrappers (e.g., `BahmniObsService`, `BahmniProgramWorkflowService`).

**Mappers**: Mapper classes (e.g., `EncounterTransactionMapper`, `ObsMapper`) handle conversion between entities and contracts using a visitor/strategy pattern.

**Event-driven**: The `events/` package shows event-based architecture for handling domain events.

### Important Base Packages

- `org.bahmni.module.bahmnicore.service`: Service interfaces and implementations
- `org.bahmni.module.bahmnicore.dao`: Data access logic
- `org.bahmni.module.bahmnicore.contract`: API contracts/DTOs
- `org.bahmni.module.bahmnicore.mapper`: Entity/DTO transformations
- `org.bahmni.module.bahmnicore.encounterTransaction`: Complex encounter transaction handling
- `org.bahmni.module.bahmnicore.forms2`: Form handling and processing
- `org.bahmni.module.bahmnicore.obs`: Observation-related logic
- `org.bahmni.module.bahmnicore.validator`: Validation logic for various entities

## Testing

### Test Organization

Tests are located in `src/test/` directories and follow the package structure of main code.

- **Unit tests** (JUnit 4, Mockito): Basic service and utility tests
- **Integration tests** (marked with IT suffix, use `mvn verify`): Tests that interact with OpenMRS/database
- **Test data**: XML files in `src/test/resources/` provide test fixtures and metadata

### Test Dependencies

- JUnit 4.13
- Mockito 3.5.11
- Hamcrest 1.3
- PowerMock 2.0.7
- OpenMRS test utilities

### Running Tests

```bash
# Unit tests only
mvn test

# Integration tests
mvn verify -P IT

# Specific test
mvn test -Dtest=ServiceNameTest

# Skip tests during build
mvn install -DskipTests
```

## Dependencies & Versions

Key dependency versions (defined in parent pom.xml):

- **OpenMRS Core**: 2.5.12
- **OpenMRS Web Services**: 2.44.0
- **Spring**: 5.2.14.RELEASE
- **Atomfeed**: 1.10.1
- **EMR API Module**: 1.36.0
- **Rules Engine**: 1.1.0-SNAPSHOT
- **Bahmni Commons**: 1.2.0
- **Lombok**: 1.18.20
- **Log4j**: 2.17.1

When adding new dependencies, add them to the parent `pom.xml` properties section to ensure consistency across modules.

## Key Configuration

### Properties File

`bahmnicore.properties` contains module configuration:
- OpenERP connection settings
- Data migration mode flag
- Image/document directory paths

### Spring Context

Test context configured in `TestingApplicationContext.xml` in bahmnicore-ui. Application context is configured via OpenMRS module activation.

## Common Development Tasks

### Adding a New REST Endpoint

1. Create a contract class in `bahmnicore-api/contract/`
2. Implement service logic in `bahmnicore-api/service/`
3. Create a controller in `bahmnicore-omod/` that exposes the service
4. Register the controller in OpenMRS web module configuration

### Adding a New Service

1. Create interface in `bahmnicore-api/service/`
2. Create implementation class
3. Add spring bean configuration (usually via @Service or Spring XML config)
4. Write unit tests using Mockito for DAO mocking

### Working with Observations

Use `BahmniObsService` and related classes in `org.bahmni.module.bahmnicore.service`. The `ObsMapper` handles conversion between OpenMRS Obs entities and contracts.

### Working with Encounter Transactions

The `EncounterTransactionMapper` is central. It orchestrates conversion of encounters with all nested data (observations, orders, diagnoses) between the contract and entity layers.

## Git & Branch Strategy

- **Main development branch**: `CURE-Product-Master`
- **Feature branches**: Named after Hive/JIRA tickets (e.g., `draft-form`, `Hive-106849`)
- **Pull requests**: Required for merging to master
- Recent work: Draft forms, rules engine, disease summaries

## Code Style & Conventions

- **Java**: Standard Java conventions (camelCase for methods/variables, PascalCase for classes)
- **Naming**: Service classes end with `Service`, DAO classes with `Dao`, mappers with `Mapper`, contracts with appropriate suffixes
- **Spring annotations**: Use @Service, @Repository, @Autowired for dependency injection
- **Testing**: Mock external dependencies, use descriptive test names with Given-When-Then pattern when clear
- **Comments**: Javadoc for public APIs, inline comments for non-obvious logic only

## Known Patterns & Gotchas

1. **Encounter Transaction Complexity**: The encounter transaction pattern is complex. When modifying it, understand the full flow through EncounterTransactionMapper before making changes.

2. **OpenMRS Service Wrappers**: Many Bahmni services wrap OpenMRS services. Check both when debugging issues.

3. **Test Database**: Integration tests use an in-memory OpenMRS test database. Test data is loaded from XML fixtures.

4. **Lazy Loading**: Be aware of Hibernate lazy loading issues with OpenMRS entities. Sometimes need explicit queries or eager loading.

5. **Rules Engine**: Rules engine (version 1.1.0-SNAPSHOT) is used for resolving specific versions and applying business rules. Check `rules-engine-repository` for resolver implementations.

6. **Data Migration Mode**: `bahmnicore.datamigration.mode` property controls whether the system is in migration mode. Some logic behaves differently based on this flag.

## Debugging Tips

- Use Maven `-X` flag for verbose output: `mvn -X clean install`
- Check OpenMRS logs in test runs for database-related issues
- Use IDE debugger to step through EncounterTransactionMapper for transaction issues
- Check @Transactional annotations if you encounter transaction-related bugs
- Verify Spring bean wiring if autowiring fails

## CI/CD

Project uses GitHub Actions for Java CI with Maven (visible in README badge). Pull requests trigger automated builds that run tests and style checks.
