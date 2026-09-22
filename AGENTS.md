# Android Engineering Standards

## Architecture

- Use Clean Architecture where project complexity justifies it.
- Use MVVM or MVI for presentation.
- Follow clear separation of UI, domain and data layers.
- UI must not directly access repositories or data sources.
- Keep business logic outside Composables/Activities/Fragments.
- Prefer unidirectional data flow.

## Kotlin

- Kotlin is the primary language.
- Prefer immutable variables and data classes.
- Use coroutines and Flow for asynchronous operations.
- Avoid unnecessary scope functions.
- Avoid deeply nested functions and conditions.
- Prefer readable code over clever one-liners.

## Jetpack Compose

- Keep Composables small and reusable.
- Hoist state when appropriate.
- Do not put business logic inside Composables.
- Use stable UI state models.
- Handle Loading, Success and Error states explicitly.
- Avoid unnecessary recompositions.

## Dependency Injection

- Use Hilt for dependency injection.
- Avoid service locators and global mutable state.
- Keep dependencies flowing in one direction.

## Networking

- Use Retrofit/OkHttp where appropriate.
- Keep API models separate from domain models when useful.
- Handle network errors explicitly.
- Never hardcode secrets.
- Never log tokens, passwords or sensitive user data.

## Database

- Use Room for local relational storage.
- Keep database entities separate from domain models where appropriate.
- Handle migrations properly.
- Never destroy user data just to solve a schema problem.

## Testing

Every meaningful feature should include appropriate tests.

- Unit tests for ViewModels.
- Unit tests for UseCases/business logic.
- Repository tests where useful.
- UI tests for critical user journeys.
- Test success, loading and failure states.
- Test edge cases.

## Code Quality

Before considering a task complete:

1. Run the project build.
2. Run unit tests.
3. Run lint/static analysis.
4. Inspect the complete Git diff.
5. Check for unnecessary changes.
6. Check for lifecycle problems.
7. Check coroutine cancellation.
8. Check memory leaks.
9. Check error handling.
10. Check security implications.

## Git

- Make focused changes.
- Do not modify unrelated files.
- Do not remove existing functionality without justification.
- Do not commit generated files unnecessarily.
- Never commit secrets.
- Review `git diff` before completing a task.

## AI Agent Rules

Before modifying code:

1. Understand the existing architecture.
2. Search for existing implementations.
3. Reuse existing patterns.
4. Do not introduce a new library when an existing dependency solves the problem.
5. Do not rewrite working code unnecessarily.
6. Explain architectural impact before making large changes.

After modifying code:

1. Build.
2. Test.
3. Run static analysis.
4. Review diff.
5. Report changed files and verification results.
