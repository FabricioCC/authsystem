# AGENTS.md - AI Coding Agent Guide

## Project Overview
**AuthSystem** is a Spring Boot 4.0.6 RBAC (Role-Based Access Control) authentication system written in Java 21.
- **Framework**: Spring Boot (WebMVC, Data JPA)
- **Database**: PostgreSQL (Docker-composed)
- **Cache**: Redis (Docker-composed)
- **Build Tool**: Maven
- **Code Generation**: Lombok for entity boilerplate

## Architecture Patterns

### Entity Relationships (Three-Tier RBAC)
```
User --M:M--> Role --M:M--> Permission
      user_roles        role_permissions
```
**Key Points**:
- User IDs are `UUID` (generated); Role/Permission IDs are `Long` (SEQUENCE)
- All many-to-many relationships use **LAZY fetching** (performance optimization)
- See: `src/main/java/com/fabriciodev/authsystem/domain/entity/`

### Enum-Driven Design
- **RoleType**: `ADMIN`, `USER`, `MODERATOR` (entity constraint, not string)
- **AuditAction**: `LOGIN`, `LOGOUT`, `PASSWORD_RESET` (audit logging actions)
- Used via `@Enumerated(EnumType.STRING)` in JPA entities

### Audit Trail
AuditLog entity tracks security events:
- Links to User via many-to-one relationship
- Records action type, IP address, and success flag
- Uses `@CreationTimestamp` for immutable audit dates
- Supports compliance/debugging workflows

## Development Workflow

### Build & Run Locally
```bash
# Maven clean build
./mvnw clean package

# Run Spring Boot app (uses application.properties defaults)
./mvnw spring-boot:run

# Run tests
./mvnw test
```

### Docker Deployment
```bash
# Start stack (PostgreSQL + Redis + Spring app)
docker-compose up -d

# PostgreSQL: postgres://app_user:app_password@localhost:5432/app_db
# Redis: redis://localhost:6379
# Spring App: http://localhost:8080
```

### Database Profile
- Default: local PostgreSQL
- Docker: auto-configured via `SPRING_PROFILES_ACTIVE=docker` environment variable

## Code Conventions

### Entity Boilerplate
- ALL entities use `@Getter` + `@Setter` (Lombok)
- Lombok annotation processor configured in compiler plugin (pom.xml lines 90-95)
- Constructor logic should avoid JPA proxies—use factory methods or `@PostLoad`

### ID Generation Strategy
- **User**: `@GeneratedValue(strategy = GenerationType.UUID)` → UUID type
- **Role/Permission**: `@GeneratedValue(strategy = GenerationType.SEQUENCE)` → Long type
- **AuditLog**: `@GeneratedValue(strategy = GenerationType.AUTO)`
- Rationale: UUID provides distributed uniqueness for core User entity

### Relationships Default Behavior
1. Always declare `fetch = FetchType.LAZY` explicitly (even if default)
2. Use `new ArrayList<>()` for List initialization (User → Roles)
3. Use `new HashSet<>()` for Set initialization (Role → Permissions, Permission → Roles)
4. Add `@JoinColumn` or `@JoinTable` annotations for clarity

### Date/Time Handling
- Use `LocalDate` (not `LocalDateTime`) for persistence
- `@CreationTimestamp` auto-sets immutable created_at fields (Hibernate annotation)

## Critical Files

| File | Purpose |
|------|---------|
| `pom.xml` | Maven config; Lombok processor setup; Spring Boot 4.0.6 parent |
| `docker-compose.yaml` | PostgreSQL (`:5432`), Redis (`:6379`), Spring app (`:8080`) |
| `src/main/java/com/fabriciodev/authsystem/domain/` | Entity models and enums for RBAC |
| `application.properties` | Minimal config; Docker overrides via environment variables |

## Dependency Notes

### Key Dependencies
- **Spring Boot Starter WebMVC**: HTTP handling
- **Spring Boot Starter Data JPA**: ORM + Hibernate
- **PostgreSQL Driver**: Runtime dependency for local dev
- **Lombok**: Compile-time annotation processing

### Security Considerations
1. User passwords stored as `passwordHash` (never plain text)
2. Audit logs immutable (creation timestamps, no updates)
3. Relationships lazy-loaded to prevent N+1 queries
4. IP address field supports IPv6 (length 45)

## Extension Points

### Adding New Features
1. **New Entity**: Add to `domain/entity/`, follow Lombok + JPA conventions
2. **New Role**: Add to `RoleType` enum (database migration not needed for enum-based columns)
3. **New Audit Action**: Add to `AuditAction` enum
4. **Services/Controllers**: Create under `com.fabriciodev.authsystem` (standard Spring package structure)

### Database Migrations
- No Flyway/Liquibase configured yet; SQL init scripts can be placed in `src/main/resources/db/migration/`
- Ensure DDL init via Spring: `spring.jpa.hibernate.ddl-auto=create-drop` (or `update`)

## Performance Patterns

- **LAZY fetching**: All relationships lazy-loaded; N+1 queries require explicit `@EntityGraph` or `JOIN FETCH`
- **Redis available**: Integration not yet implemented; opportunity for session/cache layer
- **UUID for User**: Suitable for distributed systems; use in API responses

## Testing Structure
- Location: `src/test/java/com/fabriciodev/authsystem/`
- Framework: Spring Boot Test (WebMVC test + Data JPA test starters)
- Unit test example: `AuthsystemApplicationTests.java`

