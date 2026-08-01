# SYSTEM DESIGN

# Kiroku AI System Design Specification

Version: 1.0

Status: Active

---

# Purpose

This document defines the complete system architecture of Kiroku AI.

Every implementation, GitHub Issue, AI agent, and contributor must follow this specification.

This document is the single source of truth for system design.

---

# Product Vision

Kiroku AI is a completely offline AI meeting assistant.

All user data remains on the user's device.

No cloud services are required.

---

# Design Principles

The following principles are mandatory.

1. Privacy First
2. Offline First
3. Local AI Only
4. Simple Architecture
5. Replaceable AI Engines
6. Testability
7. Maintainability

---

# High Level Architecture

```
                UI (Compose)

                      │

                ViewModel

                      │

               Repository Layer

                      │

               Domain Services

        ┌─────────────┴─────────────┐
        │                           │
        ▼                           ▼

 Speech Recognition          Meeting Summary

        │                           │
        ▼                           ▼

   Whisper.cpp               llama.cpp

        │                           │

        └─────────────┬─────────────┘

                      ▼

                   Room DB
```

Dependencies always point downward.

No circular dependencies.

---

# Package Structure

```
app/

├── ai/
│   ├── speech/
│   ├── llm/
│   ├── summary/
│   ├── prompt/
│   └── parser/
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
├── repository/
│
├── ui/
│
├── viewmodel/
│
├── navigation/
│
└── util/
```

---

# Screen Flow

```
Splash

↓

Home

↓

Recording

↓

Transcript

↓

Meeting Summary

↓

Meeting Detail

↓

Export
```

History opens Meeting Detail directly.

---

# Recording Flow

```
Microphone

↓

PCM

↓

Recorder

↓

WAV

↓

Whisper.cpp

↓

Transcript

↓

Room
```

Transcript must always be stored before any LLM processing.

---

# AI Pipeline

```
Transcript

↓

Prompt Builder

↓

LlmRepository

↓

Llama.cpp

↓

JSON

↓

MeetingSummaryParser

↓

MeetingSummary

↓

Room

↓

UI
```

The UI never receives raw JSON.

---

# Data Model

## Meeting

Fields

- id
- title
- createdAt
- duration
- status
- transcript
- summary
- decisions
- discussion
- nextActions
- risks

The transcript is the source of truth.

Summary can always be regenerated.

---

# Domain Models

## Transcript

```
text
language
duration
```

---

## MeetingSummary

```
summary
decisions
discussion
nextActions
risks
```

Immutable.

---

# Repository Responsibilities

MeetingRepository

- Meeting CRUD

SpeechRepository

- Speech recognition

LlmRepository

- LLM inference

MeetingSummaryRepository

- Summary generation

Repositories never expose JNI.

---

# Whisper Responsibilities

Whisper is responsible only for

- Loading audio
- Speech recognition
- Returning Transcript

Whisper never

- Generates summaries
- Parses JSON
- Writes UI

---

# Llama Responsibilities

Llama is responsible only for

- Loading GGUF
- Running inference
- Returning generated text

Llama never

- Loads audio
- Accesses Room
- Updates UI

---

# Prompt Builder

Responsibilities

- Build prompts
- Escape transcript
- Maintain prompt version

Prompt Builder never

- Executes inference
- Parses JSON

---

# JSON Parser

Responsibilities

- Validate JSON
- Parse JSON
- Create MeetingSummary

Never call llama.cpp directly.

---

# Room

Meeting is the primary table.

Transcript is persisted immediately after Whisper finishes.

Summary is persisted after successful LLM generation.

Summary may be regenerated.

Transcript is never modified automatically.

---

# Assets

Assets contain

- Whisper models
- GGUF models

Assets never contain

- User recordings
- User transcripts
- User summaries

---

# Native Layer

```
Kotlin

↓

JNI

↓

whisper.cpp

Kotlin

↓

JNI

↓

llama.cpp
```

Whisper and llama are completely independent.

---

# Dependency Injection

Every implementation must depend on interfaces.

Example

SpeechRecognizer

↓

WhisperSpeechRecognizer

LlmEngine

↓

LlamaCppEngine

UI never depends on implementations.

---

# Error Handling

Recoverable errors

- Missing model
- Invalid WAV
- Invalid JSON
- JNI initialization failure
- Out of memory

Errors must never crash the application.

---

# Security

Requirements

- No telemetry
- No analytics
- No cloud APIs
- No OpenAI API
- No background upload
- No user tracking

Everything runs locally.

---

# Performance

Goals

Application startup

< 2 seconds

Meeting summary

< 30 seconds

Memory

Stable

No memory leaks.

---

# Testing

Every feature requires

- Unit Tests
- Integration Tests
- Instrumentation Tests

Required build targets

- Debug
- Release

Required quality checks

- Lint
- Native tests

---

# Coding Rules

Use

- MVVM
- Repository Pattern
- Immutable Models
- Constructor Injection
- Kotlin Coroutines
- StateFlow

Avoid

- Static mutable state
- Business logic in UI
- JNI calls from UI

---

# Versioning

Semantic Versioning

Major

Architecture changes

Minor

Features

Patch

Bug fixes

---

# Development Workflow

Product Design

↓

GitHub Issue

↓

Architecture Review

↓

Implementation

↓

Build

↓

Tests

↓

Lint

↓

Code Review

↓

Commit

↓

Push

↓

Release

Implementation never starts without an approved GitHub Issue.

---

# Current Roadmap

Completed

- Project foundation
- Recording
- Room
- Whisper.cpp
- llama.cpp

Next

- Transcript Persistence
- Instruction Model
- Meeting Summarization
- Meeting Detail
- Markdown Export
- PDF Export
- Search

---

# Future

Possible future features

- Calendar Integration
- Email Integration
- RAG
- Local Embeddings
- Vector Search
- Speaker Diarization
- Multi-language

These are outside Version 1.0.

---

# Non-Goals

Kiroku AI will never require

- Internet connectivity
- Cloud AI
- User accounts
- Subscription services
- External data storage

Offline-first is a permanent architectural decision.