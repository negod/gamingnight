# Architecture

## Backend Layers

```text
infrastructure -> application -> domain
```

The domain is the center of the application. It contains the `Item` model and the repository port. It has no Spring, JPA, database, or HTTP annotations.

The application layer contains use cases and DTOs. `ItemUseCaseService` owns the user-facing workflows and coordinates validation, persistence, and mapping.

The infrastructure layer contains adapters:

- `ItemController` maps HTTP requests to use cases.
- `JpaItemRepositoryAdapter` implements the domain repository port.
- `ItemEntity` is the persistence model and is separate from `Item`.
- `GlobalExceptionHandler` converts application exceptions into HTTP responses.

## Dependency Rules

- Domain must not import Spring, JPA, web, or database packages.
- Application may depend on domain and framework-free shared code.
- Infrastructure may depend on application, domain, and Spring.
- Controllers must not contain business logic.
- Persistence entities must not be returned from use cases or controllers.

## Item Flow

1. HTTP request enters `ItemController`.
2. Controller validates request DTO shape and calls `ItemUseCaseService`.
3. Use case creates or updates the domain `Item`.
4. Use case persists through `ItemRepositoryPort`.
5. JPA adapter maps domain model to `ItemEntity`.
6. Use case maps domain model to `ItemResponse`.

## Frontend Layers

The frontend keeps route screens, feature components, API calls, and shared utilities separate.

- `pages` contains route-level screens.
- `features/items/api` contains item API calls.
- `features/items/components` contains focused item UI components.
- `shared/api` contains the generic API client.
- `shared/types` contains shared TypeScript types.

UI components do not call `fetch` directly.
