# Changelog

All notable changes to Kiroku AI will be documented in this file.

The format is based on **Keep a Changelog** (https://keepachangelog.com/)
and this project follows **Semantic Versioning** (https://semver.org/).

---

## [Unreleased]

### Added

-

### Changed

-

### Deprecated

-

### Removed

-

### Fixed

-

### Security

-

---

## [0.1.0] - 2026-08-01

### Added

#### Project

- Initial project structure
- GitHub repository
- GitHub Issues workflow
- GitHub Releases
- README
- AGENTS.md
- CONTRIBUTING.md
- Release management

#### User Interface

- Home screen
- Recording screen
- History screen
- Settings screen

#### Navigation

- Navigation Compose
- AppNavigation
- Screen routing

#### Architecture

- MVVM architecture
- Repository pattern
- Clean package structure

#### Audio

- Recording architecture
- AudioRecorder interface
- MediaRecorder implementation
- RecordingViewModel

#### Database

- Room Database
- Meeting entity
- Meeting DAO
- Meeting repository
- HistoryViewModel

#### Storage

- Local meeting persistence
- Recording metadata storage

### Verified

- Successful project build
- Unit tests passed
- Navigation verified
- Recording workflow verified
- Room persistence verified

### Security

- Offline-first architecture
- Local-only data storage
- No cloud dependency
- No external AI services required

---

## Release Roadmap

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

- Meeting search
- Tags
- Favorites
- Recording management

### v0.6.0

- Security improvements
- Database encryption
- Biometric authentication
- PIN lock

### v1.0.0

First stable release.

Features include:

- Fully offline AI meeting assistant
- Local speech recognition
- Local LLM summarization
- Meeting management
- Export functions
- Enterprise-ready privacy architecture