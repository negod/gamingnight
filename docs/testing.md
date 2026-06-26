# Testing

## Backend

Run:

```bash
cd backend
mvn test
```

Test types included:

- Domain tests: validate `Item` behavior without Spring.
- Use case tests: mock `ItemRepositoryPort` and test workflows.
- Controller tests: use `@WebMvcTest` to verify HTTP behavior, validation, and error mapping.
- Persistence tests: use Testcontainers PostgreSQL to verify the JPA adapter and Flyway schema.

Repository tests require Docker because they start a PostgreSQL container.

## Frontend

Run:

```bash
cd frontend
npm test
```

Frontend tests use Vitest and React Testing Library. They focus on behavior visible to the user, such as form submission, validation errors, empty states, and item links.

## TDD Workflow

1. Write or update a failing test that describes the behavior.
2. Run the smallest relevant test command.
3. Implement the smallest clear change.
4. Refactor while tests stay green.
5. Add edge cases for validation, not-found, and error states.

Keep tests readable. Test behavior, not implementation details.
