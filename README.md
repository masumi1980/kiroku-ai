# Kiroku AI

> **Privacy-first Offline AI Meeting Assistant**

Kiroku AI is an Android application that records meetings, transcribes speech locally, summarizes discussions using on-device AI, and stores everything on the user's device.

**No cloud. No external AI services. No meeting data leaves your device.**

---

## Vision

Modern AI meeting assistants often require uploading confidential meeting audio to cloud services.

Kiroku AI takes the opposite approach.

The goal is to build a professional AI meeting assistant that works entirely offline while keeping all meeting data private.

---

## Core Principles

- Privacy First
- Offline First
- Open Source
- Android Native
- Production Quality
- Clean Architecture

---

## Features

### Current

- Meeting recording
- Recording history
- Room database
- Navigation Compose
- MVVM architecture
- Repository Pattern

### Planned

- Offline speech recognition (Whisper.cpp)
- Offline AI summarization (llama.cpp)
- Markdown export
- PDF export
- Meeting search
- Tags
- Favorites
- Database encryption
- Biometric authentication

---

## Architecture

```
Presentation
      │
      ▼
 ViewModel
      │
      ▼
 Repository
      │
      ▼
 Data Source
```

Business logic never exists inside UI components.

---

## Technology Stack

| Category | Technology |
|----------|------------|
| Language | Kotlin |
| UI | Jetpack Compose |
| Design | Material 3 |
| Architecture | MVVM |
| Pattern | Repository Pattern |
| Dependency Injection | Hilt (planned) |
| Database | Room |
| Speech Recognition | Whisper.cpp (planned) |
| Local LLM | llama.cpp (planned) |
| Build System | Gradle Kotlin DSL |

---

## Project Structure

```
app/
├── ai/
├── audio/
├── data/
├── database/
├── domain/
├── model/
├── navigation/
├── ui/
├── util/
└── viewmodel/
```

---

## Development Status

| Version | Status |
|---------|--------|
| v0.1.0 | Recording Foundation |
| v0.2.0 | Whisper.cpp Integration (planned) |
| v0.3.0 | Local LLM Integration (planned) |
| v0.4.0 | Export Features (planned) |
| v0.5.0 | Productivity Features (planned) |
| v1.0.0 | First Stable Release |

---

## Roadmap

### v0.2.0

- Whisper.cpp integration
- Offline speech recognition

### v0.3.0

- llama.cpp integration
- Offline AI meeting summarization

### v0.4.0

- Markdown export
- PDF export
- Share functionality

### v0.5.0

- Search
- Tags
- Favorites
- Recording management

### v0.6.0

- Database encryption
- Biometric authentication
- Security improvements

### v1.0.0

- Enterprise-ready offline AI meeting assistant

---

## Privacy

Kiroku AI is designed around privacy.

By default:

- No cloud communication
- No meeting uploads
- No external AI services
- Local-only processing
- Local-only storage

Meeting information belongs to the user.

---

## Build

Requirements

- Android Studio
- JDK 17+
- Android SDK
- Gradle

Clone the repository.

```bash
git clone https://github.com/masumi1980/kiroku-ai.git
```

Open the project in Android Studio.

Build the project.

```bash
./gradlew build
```

Run the application on an Android device or emulator.

---

## Development Workflow

Development follows an Issue-driven workflow.

```
GitHub Issue

↓

Implementation

↓

Code Review

↓

Build & Test

↓

Commit

↓

Push

↓

Release

↓

Close Issue
```

One Issue represents one logical feature.

---

## Contributing

Please read:

- CONTRIBUTING.md
- AGENTS.md

before contributing.

---

## Changelog

Project history is maintained in:

- CHANGELOG.md

---

## License

This project is released under the MIT License.

---

## Project Goals

Kiroku AI aims to become a professional offline AI meeting assistant suitable for enterprise environments.

The project prioritizes:

- Privacy
- Reliability
- Maintainability
- Performance
- Long-term sustainability

Every design decision should support these goals.