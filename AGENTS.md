# AGENTS.md

# Kiroku AI Development Guide

## Project Overview

Kiroku AI is an offline-first AI meeting assistant for Android.

The application records meetings, transcribes speech locally, summarizes discussions using on-device AI, and extracts action items.

Privacy is the highest priority.

---

## Development Principles

- Production quality over prototype quality.
- Prefer maintainability over clever code.
- Keep the architecture clean.
- Every feature should be testable.
- Never sacrifice readability.

---

## Tech Stack

- Kotlin
- Jetpack Compose
- Material 3
- MVVM
- Repository Pattern
- Hilt
- Room
- Whisper.cpp
- llama.cpp

---

## Architecture

Presentation
↓
ViewModel
↓
Repository
↓
Data Source

Business logic must never exist inside Composable functions.

---

## UI Guidelines

- Compose only
- Material 3
- Japanese UI
- Simple and clean
- Accessibility first

---

## Coding Rules

- Kotlin only
- No Java
- Small functions
- Avoid duplicate code
- Prefer immutable data
- Use descriptive names
- No magic numbers

---

## AI Rules

Speech recognition must be replaceable.

Summarization engine must be replaceable.

Never hardcode AI implementations.

Use interfaces for every AI provider.

---

## Database

Use Room.

Never access Room directly from UI.

---

## Testing

Business logic should always be unit-testable.

---

## Git Workflow

Every implementation starts from a GitHub Issue.

One Issue = One Pull Request

---

## Future Vision

Kiroku AI should become the best offline AI meeting assistant for Android.

---

## Product Vision

This project values:

- Offline-first
- Privacy-first
- Japanese UX
- Fast startup
- Simple operation
- Business usability
- AI should assist, not replace, the user

When multiple implementation options exist,
prefer the simpler and more maintainable solution.

---

## Issue Workflow

Before implementing a feature:

1. Read the GitHub Issue.
2. Implement only the requested scope.
3. Do not introduce unrelated changes.
4. Keep commits focused.
5. If requirements are unclear, ask for clarification instead of guessing.

---

## Project Structure

app/
├── ui/
├── navigation/
├── feature/
├── domain/
├── data/
├── ai/
├── audio/
├── database/
└── util/

Every new feature should follow this structure.

---

## Commit Message Convention

Use Conventional Commits.

Examples:

- feat: add audio recording
- fix: resolve recording crash
- refactor: simplify repository
- docs: update README
- chore: update dependencies
- test: add unit tests

---

## Dependency Policy

Do not introduce new libraries unless necessary.

Prefer AndroidX and official Google libraries.

Discuss large dependency additions before implementation.

---

## AI Coding Policy

Before writing code:

- Reuse existing code whenever possible.
- Do not duplicate implementations.
- Prefer extension functions over utility classes.
- Keep public APIs minimal.
- Explain major architectural decisions in comments.

---

## Definition of Done

A task is considered complete only if:

- The project builds successfully.
- No new warnings are introduced.
- The implementation follows the architecture.
- Documentation is updated when necessary.
- The code is ready for review.

---

## Scope

This document defines long-term development rules.

Feature-specific requirements belong in GitHub Issues or the docs/ directory, not in this file.