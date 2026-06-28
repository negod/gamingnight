# Mistral Vibe Instructions

This project uses Mistral Vibe for AI-assisted development. Follow the canonical instructions in [`docs/ai-instructions.md`](docs/ai-instructions.md) for project-specific rules and conventions.

## My Capabilities & Tools

As Mistral Vibe, I have access to the following tools that complement the project workflow:

- **Code Analysis**: `read`, `grep` for examining existing code patterns
- **Code Modification**: `edit`, `write_file` for implementing changes
- **File Management**: `bash` for system operations and build commands
- **Testing**: Can execute `mvn test`, `npm test` to verify changes
- **Documentation**: Full ability to read and update all project documentation
- **Git Operations**: Can check status, view history, and create commits
- **Project Navigation**: Full access to explore the entire codebase

## My Preferred Workflow for Gaming Night

### 1. Understanding the Task
- Always read the relevant use case from [USE_CASES.md](USE_CASES.md) first
- Examine existing similar features for patterns and conventions
- Check [docs/architecture.md](docs/architecture.md) for architectural context

### 2. Implementation Approach
- **Backend First**: Implement domain models, repository ports, use cases, then infrastructure
- **TDD Compliance**: Write tests alongside implementation as specified in ai-instructions.md
- **Clean Architecture**: Strictly maintain `infrastructure -> application -> domain` dependency flow
- **Layer Separation**: Never mix concerns between layers

### 3. Common Patterns I Use

#### Backend Feature Implementation
```
1. Domain Layer:
   - Model: backend/src/main/java/se/backede/domain/model/
   - Repository Port: backend/src/main/java/se/backede/domain/repository/
   
2. Application Layer:
   - DTOs: backend/src/main/java/se/backede/application/dto/
   - Mappers: backend/src/main/java/se/backede/application/mapper/
   - Use Case Service: backend/src/main/java/se/backede/application/usecase/
   
3. Infrastructure Layer:
   - Entity: backend/src/main/java/se/backede/infrastructure/persistence/
   - Repository Adapter: Jpa*RepositoryAdapter classes
   - Controller: backend/src/main/java/se/backede/infrastructure/web/
```

#### Frontend Feature Implementation
```
1. API Client: frontend/src/features/<feature>/api/
2. Components: frontend/src/features/<feature>/components/
3. Pages: frontend/src/pages/
4. Types: frontend/src/shared/types/
```

### 4. Quick Reference Commands

```bash
# Backend
cd backend
mvn test                          # Run all backend tests
mvn spring-boot:run              # Start backend server
mvn clean package                # Build for production

# Frontend
cd frontend
npm test                          # Run all frontend tests
npm run dev                       # Start development server
npm run build                     # Build for production

# Database
docker compose up -d postgres      # Start PostgreSQL

# Full Stack Testing
cd backend && mvn test            # Backend tests
cd frontend && npm test           # Frontend tests
```

### 5. Project-Specific Reminders

#### Backend Conventions
- **Package Structure**: `se.backede.<layer>.<type>`
- **Naming**: Follow patterns in [ai-instructions.md](docs/ai-instructions.md#naming-conventions)
- **Validation**: Always validate at use case layer, not just controller
- **Error Handling**: Use framework-free exceptions from `shared/exception`

#### Frontend Conventions
- **API Calls**: Never call `fetch` directly in components - use feature-specific API wrappers
- **Component Structure**: Keep UI components focused and reusable
- **Type Safety**: Use TypeScript types for all API responses

#### Database & Migrations
- **Liquibase**: All schema changes must be YAML changesets
- **Naming**: `000N-description.yaml` with zero-padded sequential numbers
- **Registration**: Add to `db.changelog-master.yaml`
- **Testing**: Use Testcontainers for database-dependent tests

### 6. Important Project Context

**Current Implementation Status:**
- ✅ Player Management (full CRUD)
- ✅ Competition Management (CRUD + team generation)
- ✅ Item Management (starter feature - being phased out)
- ✅ Competition Run (start, matches, results)
- ✅ Leaderboards (per-game, total, teams, players)
- 🔄 Game Management (in progress)
- 🔄 Team Management (in progress)

**Technology Stack:**
- **Backend**: Spring Boot 3, Java 21, PostgreSQL, JPA/Hibernate, Liquibase
- **Frontend**: React, Vite, TypeScript, Tailwind CSS
- **Testing**: JUnit 5, AssertJ, Mockito, Testcontainers, Vitest, React Testing Library

### 7. When to Ask for Clarification

I will ask the user when:
- Task requirements are ambiguous or conflicting
- Multiple valid approaches exist and need user preference
- I need to make architectural decisions not covered in existing docs
- I encounter blocking issues that require human judgment

### 8. Documentation Priority

Remember: **Documentation updates are part of every feature implementation**
- Update relevant docs before marking a feature as complete
- Cross-reference between documentation files where helpful
- Maintain the table of contents structure in all major docs

---

**Always defer to [`docs/ai-instructions.md`](docs/ai-instructions.md) for authoritative project rules and conventions.**
