# DATABASE DESIGN

# Kiroku AI Database Design Specification

Version: 1.0

Status: Active

---

# Purpose

This document defines the database architecture of Kiroku AI.

Room entities, relationships, repositories, and migrations must follow this specification.

This document is the single source of truth for database design.

---

# Design Principles

Database design follows these principles.

1. Store original data.
2. Derived data can be regenerated.
3. Never duplicate source data.
4. Never store AI state.
5. Support future migrations.
6. Keep entities simple.
7. Normalize where practical.

---

# Database

Room Database

Database Name

```
kiroku.db
```

Current Version

```
1
```

---

# Entity Relationship

```
Meeting

│

├── Transcript

├── Summary

├── Decisions

├── Discussion

├── NextActions

└── Risks
```

Version 1 stores these as fields inside Meeting.

Future versions may normalize them.

---

# Entity

## Meeting

Table

```
meeting
```

Primary Key

```
id
```

---

# Columns

## id

Type

```
Long
```

Auto Generate

```
true
```

---

## title

Type

```
String
```

Nullable

```
false
```

Description

Meeting title.

---

## createdAt

Type

```
Long
```

Unix Epoch Milliseconds.

---

## updatedAt

Type

```
Long
```

Automatically updated whenever meeting data changes.

---

## durationMillis

Type

```
Long
```

Meeting duration.

---

## status

Enum

```
Recording

Completed

Summarized

Archived
```

---

## transcript

Type

```
String
```

Source of Truth.

Never automatically modified.

Only Whisper updates this field.

---

## summary

Type

```
String
```

Derived data.

Can always be regenerated.

---

## decisions

Type

```
String
```

JSON Array.

Example

```
[
  "Release v1.0 approved",
  "Use Whisper Tiny"
]
```

---

## discussion

Type

```
String
```

JSON Array.

---

## nextActions

Type

```
String
```

JSON Array.

---

## risks

Type

```
String
```

JSON Array.

---

## whisperModel

Type

```
String
```

Example

```
ggml-tiny.en.bin
```

---

## llamaModel

Type

```
String
```

Example

```
stories15M-q4_0.gguf
```

Future models may replace this.

---

## language

Type

```
String
```

ISO-639-1.

Example

```
ja

en
```

---

## checksum

Type

```
String
```

SHA-256 of transcript.

Used to detect accidental corruption.

---

# Indexes

```
createdAt

title

status
```

Future

```
language
```

---

# Repository

MeetingRepository

Responsibilities

- Create meeting
- Update meeting
- Delete meeting
- Read meeting

MeetingRepository never calls AI.

---

SpeechRepository

Responsibilities

- Persist transcript
- Read transcript

SpeechRepository never calls llama.cpp.

---

LlmRepository

Responsibilities

- Execute inference

LlmRepository never writes Room.

---

MeetingSummaryRepository

Responsibilities

- Save generated summary
- Read generated summary

No prompt generation.

---

# Source Of Truth

Transcript

is always the source.

Summary

can always be regenerated.

If transcript changes,

summary becomes invalid.

---

# Persistence Flow

```
Recording

↓

Whisper

↓

Transcript

↓

Room

↓

LLM

↓

Summary

↓

Room
```

Transcript is always stored first.

---

# Transactions

Summary update

must be executed inside a Room transaction.

Fields updated together

- summary
- decisions
- discussion
- nextActions
- risks
- updatedAt

---

# Migration Rules

Never delete columns.

Only

- add columns
- migrate data

Future versions

```
MeetingDecision

MeetingAction

MeetingRisk
```

may become separate tables.

---

# Constraints

Transcript

Maximum

```
1 MB
```

Summary

Maximum

```
256 KB
```

Title

Maximum

```
200 characters
```

---

# Future Tables

Version 2+

```
MeetingTag
```

```
MeetingAttachment
```

```
MeetingParticipant
```

```
MeetingAction
```

```
MeetingDecision
```

```
MeetingRisk
```

```
MeetingEmbedding
```

These are intentionally excluded from Version 1.

---

# Security

Database contains confidential information.

Requirements

- No cloud synchronization
- No telemetry
- No analytics

Future

Optional

SQLCipher encryption.

---

# Backup

Future support

Manual export only.

No automatic cloud backup.

---

# Testing

Repository Tests

DAO Tests

Migration Tests

Instrumentation Tests

All must pass.

---

# Performance

Goals

Insert Meeting

<100ms

Read Meeting

<50ms

Update Summary

<100ms

Database startup

<1 second

---

# Non-Goals

Version 1 does not support

- Multi-user
- Cloud sync
- Remote database
- Shared editing
- Vector database
- Embedding storage

These belong to future versions.

---

# Database Lifecycle

```
Recording

↓

Meeting created

↓

Transcript stored

↓

Summary stored

↓

Meeting completed

↓

Archive
```

Meeting data remains editable.

Transcript is never overwritten automatically.

Summary may be regenerated any number of times.

End of Specification.