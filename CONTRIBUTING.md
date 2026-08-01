# Contributing to Kiroku AI

Thank you for contributing to Kiroku AI.

Kiroku AI is an **offline-first**, **privacy-first** AI meeting assistant.

Our primary goal is to build a production-quality application that keeps all meeting data on the user's device.

---

# Core Principles

Every contribution must respect the following principles.

## Privacy First

Meeting data must never be transmitted to external services unless the user explicitly enables such functionality in a future release.

The default behavior is completely offline.

---

## Offline First

All core functionality should work without Internet access.

Examples:

- Audio recording
- Speech recognition
- AI summarization
- Database
- Search
- Export

---

## Clean Architecture

The project follows MVVM and Repository Pattern.

Presentation

↓

ViewModel

↓

Repository

↓

Data Source

Business logic must never exist inside UI components.

---

# Development Workflow

Every feature starts with a GitHub Issue.

Workflow:

1. Create Issue
2. Design
3. Implement
4. Review
5. Build
6. Test
7. Commit
8. Push
9. Release
10. Close Issue

One Issue should represent one logical feature.

Avoid mixing unrelated changes.

---

# Git Workflow

Main branch must always remain stable.

Commit frequently.

Push only after successful build.

Every commit should compile successfully.

---

# Commit Messages

Use Conventional Commits.

Examples:

```text
feat: add recording screen
fix: resolve recording crash
refactor: split navigation
docs: update README
test: add repository tests
chore: update dependencies
```

---

# Branch Strategy

Small personal development may commit directly to `main`.

For larger changes or collaborative work, use feature branches.

Naming examples:

```text
feature/recording
feature/whisper
feature/llama
fix/navigation
refactor/database
```

---

# Code Style

Use Kotlin.

Do not introduce Java code.

Prefer immutable data.

Keep functions small.

Avoid duplicated logic.

Use meaningful names.

Avoid magic numbers.

Prefer composition over inheritance.

---

# UI Guidelines

Use:

- Jetpack Compose
- Material 3

UI should be:

- Simple
- Accessible
- Responsive
- Consistent

Business logic must never exist inside Composable functions.

---

# Architecture Rules

UI must never:

- access Room directly
- access MediaRecorder directly
- access Whisper directly
- access llama.cpp directly

UI communicates only with ViewModels.

Repositories hide implementation details.

Interfaces should be preferred over concrete implementations.

---

# AI Development Rules

AI engines must remain replaceable.

Speech recognition should be abstracted behind an interface.

LLM implementations should be abstracted behind an interface.

Never hardcode a specific AI implementation inside UI code.

---

# Database Rules

Use Room.

Entity classes remain inside the database layer.

Repositories convert Entities into domain models.

UI should never know Entity classes.

---

# Dependency Policy

Prefer official Android libraries.

Avoid unnecessary dependencies.

Every new dependency should have a clear justification.

---

# Testing

Every new feature should be verified by:

- Successful build
- Unit tests
- Manual verification

A feature is not considered complete until it has been tested.

---

# Documentation

Update documentation whenever project behavior changes.

Relevant documents include:

- README.md
- CHANGELOG.md
- AGENTS.md
- CONTRIBUTING.md

---

# Definition of Done

A task is complete only when:

- Acceptance Criteria are satisfied
- Project builds successfully
- No new warnings are introduced
- Documentation is updated when necessary
- Code is committed
- Code is pushed
- GitHub Issue is closed

---

# Security Policy

Meeting data is confidential.

Never introduce code that sends meeting data to external services without explicit project approval.

Security takes priority over convenience.

---

# Project Vision

Kiroku AI aims to become a professional offline AI meeting assistant.

The project values:

- Privacy First
- Offline First
- Maintainability
- Simplicity
- Reliability
- Open Source
- Long-term sustainability

Every contribution should support these goals.