# Mistral Vibe Instructions

This project uses Mistral Vibe for AI-assisted development. Follow the canonical instructions in [`docs/ai-instructions.md`](docs/ai-instructions.md) for project-specific rules and conventions.

## My Capabilities & Tools

As Mistral Vibe, I have access to the following tools and skills that enable me to work effectively on this project:

### 📁 Code Analysis & Navigation
- **`read`**: Read files with line numbers for precise code examination
- **`grep`**: Search across the entire codebase using regex patterns
- **File system access**: Full navigation through project directories
- **Cross-file analysis**: Ability to trace dependencies and understand code flow

### ✏️ Code Modification & Creation
- **`edit`**: Precise string replacement with exact matching
- **`write_file`**: Create new files with proper content
- **Bulk operations**: Can handle multiple file changes in sequence
- **Atomic changes**: Ensure changes are applied correctly and safely

### 🧪 Testing & Validation
- **Test execution**: Can run `mvn test`, `npm test` to verify changes
- **Build verification**: Can execute `mvn clean package`, `npm run build`
- **Code validation**: Check compilation and runtime behavior
- **Test-driven development**: Follow TDD workflow as specified in ai-instructions.md

### 📝 Documentation & Communication
- **Full documentation access**: Read and update all markdown files
- **Structured writing**: Create clear, well-organized documentation
- **Cross-referencing**: Maintain links between related documentation
- **Table of contents**: Create and maintain navigation structures

### 🔧 System & Development Tools
- **`bash`**: Execute shell commands with proper timeouts
- **Git operations**: Status, history, commits, diffs
- **Docker operations**: Container management for local development
- **Environment setup**: Configure and verify development environment

### 🏗️ Project-Specific Skills
- **Clean Architecture**: Understand and maintain layer separation (domain -> application -> infrastructure)
- **Spring Boot**: Java backend development with Spring ecosystem
- **React/TypeScript**: Frontend development with modern frameworks
- **JPA/Hibernate**: Database operations and entity mapping
- **Liquibase**: Database migration management
- **Testcontainers**: Integration testing with real databases
- **REST API Design**: Proper endpoint design and HTTP semantics

### 🔍 Quality Assurance
- **Code review**: Identify patterns, anti-patterns, and improvements
- **Security awareness**: Apply OWASP principles and secure coding practices
- **Performance consideration**: Evaluate efficiency and scalability
- **Error handling**: Proper exception management and user feedback

### 📊 Workflow Management
- **Task decomposition**: Break down complex tasks into manageable steps
- **Progress tracking**: Maintain awareness of task completion status
- **Dependency management**: Handle interdependent changes properly
- **Risk assessment**: Identify potential issues before they occur

### 🎯 Required Tools for Optimal Performance
- **Code search**: `grep` with regex for finding patterns
- **File reading**: `read` with line numbers for precise analysis  
- **File editing**: `edit` with exact string matching for safe changes
- **System commands**: `bash` for build tools and git operations
- **Documentation tools**: Full markdown editing and formatting

### ⚡ Performance Requirements
- **Large file handling**: Can process files up to 2000 lines efficiently
- **Multi-file operations**: Can work across multiple files simultaneously
- **Memory management**: Efficient context handling for complex tasks
- **Timeout awareness**: Proper command timeout management for long-running operations

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

## Project Context I Need Access To

### Repository Structure
- Full access to `/backend/` - Spring Boot Java application
- Full access to `/frontend/` - React TypeScript application  
- Full access to `/docs/` - Project documentation
- Full access to root configuration files (README.md, docker-compose.yml, etc.)

### Build & Development Tools
- Maven (`mvn`) for backend builds and testing
- Node.js/npm for frontend development
- Docker and Docker Compose for local services
- Java 21 runtime
- PostgreSQL database

### Codebase Knowledge
- Domain models and business rules
- Clean Architecture layer boundaries
- REST API endpoints and contracts
- Database schema and migrations
- Testing strategies and patterns
- Frontend component structure

### External Dependencies
- Spring Boot ecosystem (Spring Data JPA, Spring Web, etc.)
- React ecosystem (Vite, TypeScript, Tailwind CSS)
- Testing frameworks (JUnit 5, AssertJ, Mockito, Testcontainers, Vitest)
- Database tools (Liquibase, PostgreSQL)

---


**Always defer to [`docs/ai-instructions.md`](docs/ai-instructions.md) for authoritative project rules and conventions.**

## What I Need From You

To work most effectively, please provide:

### Clear Instructions
- **Specific tasks**: "Add validation to Player entity" rather than "Improve the code"
- **Acceptance criteria**: "The API should return 400 for invalid input" 
- **Scope boundaries**: "Only modify the backend, don't touch frontend"
- **Priority context**: "This is a critical security fix needed for production"

### Context & Background
- **User stories** or use case references
- **Technical constraints** or requirements
- **Design decisions** that have already been made
- **Existing patterns** I should follow

### Feedback Loop
- **One task at a time**: As you've directed - let me complete one task before giving the next
- **Progress confirmation**: Let me know when I'm on the right track
- **Course correction**: Tell me if I'm heading in the wrong direction
- **Completion verification**: Confirm when tasks are properly finished

### Decision Making
- **Architectural choices**: Please specify when you want me to make design decisions vs. follow existing patterns
- **Trade-offs**: Indicate when cost vs. complexity vs. timeline considerations matter
- **Risk tolerance**: Let me know the acceptable level of risk for changes

## Success Metrics

I consider a task complete when:
- ✅ Code changes are implemented and tested
- ✅ Documentation is updated (if applicable)
- ✅ All tests pass
- ✅ The change follows project conventions
- ✅ Cross-references and links work correctly
- ✅ The solution is minimal and focused

**Always defer to [`docs/ai-instructions.md`](docs/ai-instructions.md) for authoritative project rules and conventions.**
