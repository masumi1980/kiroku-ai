# PRODUCT

# Kiroku AI Product Specification

Version: 1.0

Status: Active

---

# Product Vision

Kiroku AI is a privacy-first AI meeting assistant that runs entirely on the user's device.

The product exists to eliminate the need to upload confidential meeting data to cloud AI services.

All core functionality must work offline.

---

# Mission

Enable professionals to record, transcribe, summarize and manage meetings securely without compromising privacy.

---

# Product Goals

The product must:

- Record meetings
- Transcribe speech locally
- Generate meeting summaries locally
- Store all meeting information locally
- Export meeting results
- Remain easy to use
- Be suitable for enterprise environments

---

# Product Principles

The following principles always take priority.

1. Privacy First
2. Offline First
3. Reliability
4. Simplicity
5. Maintainability
6. Performance

If two implementation choices exist, choose the one that better satisfies these principles.

---

# Target Users

Primary users

- Business professionals
- Engineers
- Project managers
- Consultants
- Researchers
- Government organizations
- Educational institutions

Secondary users

- Students
- Freelancers
- Open source communities

---

# Problems We Solve

Existing AI meeting assistants often require:

- uploading recordings
- cloud processing
- subscription services
- Internet connectivity

These introduce privacy concerns.

Kiroku AI removes these requirements.

---

# Unique Value Proposition

Kiroku AI provides:

- Completely offline operation
- Local AI processing
- Local speech recognition
- Local LLM summarization
- Enterprise-friendly privacy

Meeting data always remains under the user's control.

---

# Non-Goals

The following are intentionally outside the scope of this product.

- Cloud-first architecture
- Mandatory user accounts
- Mandatory Internet connection
- Advertising
- User tracking
- Telemetry
- Data collection

These may only be introduced as optional features in future versions.

---

# Minimum Viable Product

The first usable version includes:

- Meeting recording
- Offline transcription
- Offline AI summarization
- Meeting history
- Markdown export
- PDF export

---

# Long-Term Vision

Kiroku AI should become the standard offline AI meeting assistant.

The project should support:

- Android
- Windows
- Linux
- macOS

using the same core architecture.

---

# Functional Requirements

The application shall provide:

- Meeting recording
- Meeting history
- Search
- AI transcription
- AI summarization
- Export
- Settings
- Model management

---

# Non-Functional Requirements

The application should provide:

- Fast startup
- Low memory usage
- Responsive UI
- Offline capability
- Stable performance
- Long-term maintainability

---

# Privacy Requirements

Meeting information is confidential.

By default:

- No uploads
- No cloud processing
- No analytics
- No telemetry
- No advertising
- No external tracking

All meeting data remains on the user's device.

---

# Security Requirements

The application should support:

- Database encryption
- Biometric authentication
- PIN lock
- Secure export

Security must never be reduced for convenience.

---

# AI Requirements

Speech recognition

- Must run locally
- Must be replaceable

LLM

- Must run locally
- Must be replaceable

Prompt generation

- Must be independent of LLM implementation

Changing AI models must not require UI changes.

---

# Supported AI Engines

Speech Recognition

- Whisper.cpp

Local LLM

- llama.cpp

Future engines may be added without changing the architecture.

---

# Success Metrics

The project is considered successful when users can:

- Record meetings
- Obtain accurate transcripts
- Generate useful summaries
- Keep all meeting data private
- Use the application without Internet access

---

# Release Strategy

Development follows incremental releases.

Typical progression:

- Foundation
- Recording
- Database
- Speech Recognition
- AI Summarization
- Export
- Security
- Stable Release

---

# Decision Rules

When making product decisions:

1. Privacy is more important than convenience.
2. Offline capability is more important than cloud integration.
3. Simplicity is more important than feature count.
4. Maintainability is more important than clever implementations.
5. User trust is more important than rapid feature delivery.

Every product decision should follow these priorities.

---

# Product Scope

Core product

- Offline meeting assistant

Optional future extensions

- Local network processing
- Enterprise deployment
- Team collaboration
- Plugin system

These extensions must never compromise the product principles.

---

# Product Statement

Kiroku AI exists to prove that modern AI meeting assistance can be powerful without sacrificing user privacy.

Every feature should strengthen that vision.