# Contributing to Vector Launcher

Thank you for your interest in contributing to Vector Launcher! This document provides guidelines and instructions for contributing.

## Core Principles

Before contributing, please review our [Product Definition](VECTOR_LAUNCHER_PRODUCT_DEFINITION.md) and ensure your contribution aligns with these non-negotiable principles:

1. **Minimalism over decoration** - Only essential UI elements
2. **Productivity over feature count** - Focus on getting things done
3. **Speed over visual complexity** - Performance is a feature
4. **Low battery consumption** - Minimal background activity
5. **User control over forced defaults** - Customization without bloat
6. **Context preservation** - Project-scoped work stays scoped
7. **One primary launcher surface** - Dashboard-centric design
8. **Powerful internals with simple interface** - Complexity hidden from users
9. **Broad Android compatibility** - Works across device tiers
10. **Every visible control must work** - No dead UI
11. **No dead UI** - All elements are functional
12. **No feature without purpose** - Intentional development

## Getting Started

### Prerequisites

- Android Studio Hedgehog or later
- JDK 17 or later
- Android SDK 29+ (API level)
- Kotlin 2.2.10

### Setup

1. Clone the repository
2. Open in Android Studio
3. Sync Gradle files
4. Build the project: `./gradlew assembleDebug`

## Code Style

### Kotlin Conventions

- Follow [Kotlin coding conventions](https://kotlinlang.org/docs/coding-conventions.html)
- Use meaningful variable and function names
- Keep functions small and focused (ideally < 30 lines)
- Prefer immutability (`val` over `var`)
- Use data classes for model objects

### Architecture

- Follow MVVM architecture pattern
- Keep business logic in ViewModels
- Use Repository pattern for data access
- Keep Compose UI components declarative and stateless where possible

### Naming Conventions

- Classes: PascalCase (`DashboardViewModel`)
- Functions/Variables: camelCase (`activeTasks`)
- Constants: UPPER_SNAKE_CASE (`MAX_TASKS`)
- Test classes: `*Test.kt` suffix
- Test functions: descriptive snake_case (`testTaskCreation_GeneratesStableUUID`)

## Testing Requirements

All new features and bug fixes must include tests:

### Unit Tests

- Place in `app/src/test/java/com/vector/launch/`
- Mirror production package structure
- Use JUnit 4 with Mockito-Kotlin for mocking
- Aim for >80% code coverage on new code

### Test Categories

1. **Repository Tests** - Test data operations
2. **ViewModel Tests** - Test business logic
3. **Model Tests** - Test data integrity
4. **UI Tests** - Test Compose components (instrumented tests)

### Running Tests

```bash
# Run all unit tests
./gradlew testDebugUnitTest

# Run specific test class
./gradlew testDebugUnitTest --tests "com.vector.launch.core.repository.TaskRepositoryTest"

# Run with coverage
./gradlew jacocoTestReport
```

## Pull Request Process

### Before Submitting

1. Ensure all tests pass
2. Run lint checks: `./gradlew lintDebug`
3. Update documentation if needed
4. Rebase on latest main branch
5. Squash commits logically

### PR Description Template

```markdown
## Changes
- Brief description of changes

## Type of Change
- [ ] Bug fix
- [ ] New feature
- [ ] Performance improvement
- [ ] Documentation update
- [ ] Refactoring

## Testing
- [ ] Unit tests added/updated
- [ ] Manual testing completed
- [ ] Tested on physical device

## Checklist
- [ ] Code follows style guidelines
- [ ] Self-review completed
- [ ] No new warnings introduced
- [ ] Performance impact considered
```

## Feature Requests

Features must align with Vector Launcher's philosophy:

### Good Fit
- Performance improvements
- Accessibility enhancements
- Battery optimization
- Productivity-focused additions
- Bug fixes

### Poor Fit
- Social features
- AI assistants
- Complex widget systems
- Visual decorations without function
- Features that add complexity without clear productivity benefit

Submit feature requests as GitHub Issues with:
- Clear problem statement
- Proposed solution
- Alignment with product principles
- Alternative solutions considered

## Bug Reports

When reporting bugs, include:

1. **Steps to reproduce**
2. **Expected behavior**
3. **Actual behavior**
4. **Device information** (model, Android version)
5. **Vector Launcher version**
6. **Screenshots/videos** (if applicable)

## Documentation

### Code Documentation

- Document public APIs with KDoc
- Explain complex algorithms
- Include example usage for utility functions

```kotlin
/**
 * Toggles task completion status and updates timestamp.
 *
 * @param task The task to toggle
 * @return Updated task with new completion state
 */
suspend fun toggleTaskCompleted(task: Task): Task
```

### README Updates

Update README.md when:
- Adding new features
- Changing build requirements
- Modifying installation steps
- Updating architecture

## Release Process

### Version Numbering

Follow semantic versioning: `MAJOR.MINOR.PATCH`

- **MAJOR**: Breaking changes
- **MINOR**: New features (backwards compatible)
- **PATCH**: Bug fixes only

### Pre-Release Checklist

- [ ] All tests passing
- [ ] Lint clean
- [ ] Performance benchmarks met
- [ ] Documentation updated
- [ ] Changelog updated
- [ ] Version number updated in `build.gradle.kts`

## Questions?

- Check existing issues and documentation first
- Ask in GitHub Discussions for general questions
- Use GitHub Issues for bugs and feature requests

## License

By contributing, you agree that your contributions will be licensed under the same license as the project.
