# Kiroku AI Architecture

## Overview

Kiroku AI is an offline-first AI meeting assistant for Android.

The architecture is designed around the following goals:

- Privacy First
- Offline First
- Clean Architecture
- High Maintainability
- Replaceable AI Engines
- Testability

All meeting data remains on the user's device.

No cloud services are required for core functionality.

---

# Design Principles

The project follows these principles.

- Separation of concerns
- Single responsibility
- Dependency inversion
- Interface-driven design
- Composition over inheritance
- Immutable data whenever possible

Business logic must never depend on UI frameworks.

---

# Architecture

```
                 UI (Compose)
                       │
                       ▼
                 ViewModel
                       │
                       ▼
                 Use Case (optional)
                       │
                       ▼
                 Repository
                       │
         ┌─────────────┼─────────────┐
         ▼             ▼             ▼
      Database       Audio          AI
         │             │             │
         ▼             ▼             ▼
       Room      MediaRecorder   Whisper.cpp
                                   llama.cpp
```

Dependencies always point downward.

Upper layers never depend on lower implementations.

---

# Package Structure

```
app/

├── ai/
│   ├── whisper/
│   ├── llama/
│   ├── prompt/
│   └── summary/
│
├── audio/
│
├── data/
│
├── database/
│
├── domain/
│
├── model/
│
├── navigation/
│
├── ui/
│
├── util/
│
└── viewmodel/
```

Each package has one clear responsibility.

---

# Layer Responsibilities

## UI

Responsible for:

- displaying data
- collecting user input
- navigation

Must not:

- contain business logic
- access Room
- access MediaRecorder
- access Whisper.cpp
- access llama.cpp

UI communicates only with ViewModels.

---

## ViewModel

Responsible for:

- UI state
- screen logic
- calling repositories
- exposing immutable state

Must not:

- know Room entities
- know AI implementation details

---

## Use Case

Optional layer.

Use when business logic becomes complex.

Examples:

- StartRecordingUseCase
- GenerateSummaryUseCase
- ExportMarkdownUseCase

---

## Repository

Repositories provide a stable interface between business logic and implementations.

Responsibilities:

- data conversion
- caching
- orchestration
- persistence

Repositories hide implementation details.

---

## Database

Uses Room.

Contains:

- Entity
- DAO
- Database

Database classes never leave this layer.

---

## Audio

Responsible only for audio recording.

Current implementation:

- MediaRecorder

Future implementations may replace it without affecting upper layers.

---

## AI

Responsible for AI processing.

Contains independent modules.

### Whisper

Speech recognition.

Input

Audio file

Output

Transcript

---

### LLM

Meeting summarization.

Input

Transcript

Output

Meeting summary

AI engines must be replaceable.

---

# Data Flow

Recording

```
User

↓

RecordingScreen

↓

RecordingViewModel

↓

AudioRecorder

↓

MediaRecorder

↓

Audio File

↓

Repository

↓

Room
```

---

Speech Recognition

```
Audio File

↓

Whisper.cpp

↓

Transcript

↓

Repository

↓

Room
```

---

Summarization

```
Transcript

↓

Prompt

↓

llama.cpp

↓

Summary

↓

Repository

↓

Room
```

---

Export

```
Meeting

↓

Markdown

↓

PDF

↓

Share
```

---

# Dependency Rules

Allowed

```
UI

↓

ViewModel

↓

Repository

↓

Data Source
```

Forbidden

```
UI → Room

UI → MediaRecorder

UI → Whisper.cpp

UI → llama.cpp

ViewModel → Entity

Composable → Repository
```

---

# Model Rules

Domain models represent business data.

Database entities represent storage.

These must remain separate.

Repositories perform conversions.

---

# State Management

ViewModels expose immutable UI state.

Compose observes state.

Business logic never lives inside Composable functions.

---

# AI Design Rules

Speech recognition must implement an interface.

LLM engines must implement an interface.

Prompt generation must be independent from model implementation.

Changing AI engines must not affect UI code.

---

# Error Handling

Errors should propagate upward.

Repository

↓

ViewModel

↓

UI

UI displays user-friendly messages.

---

# Threading

Long-running work must not execute on the UI thread.

Examples:

- recording
- transcription
- summarization
- export

---

# Security

Meeting data is confidential.

Requirements:

- Local storage only
- No automatic uploads
- No telemetry
- No analytics
- No background network communication

Future enhancements:

- database encryption
- biometric authentication
- secure export

---

# Scalability

The architecture should support future additions without restructuring.

Examples:

- new AI models
- cloud sync (optional)
- desktop version
- plugin system
- multiple export formats

---

# Architecture Decision

When multiple implementations are possible:

1. Prefer simplicity.
2. Prefer readability.
3. Prefer maintainability.
4. Prefer replaceable components.
5. Prefer offline implementations.
6. Privacy takes precedence over convenience.

Every architectural decision should follow these priorities.