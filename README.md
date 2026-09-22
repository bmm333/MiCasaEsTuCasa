# Mi Casa Es Tu Casa

A native Android application for short-term property rentals. Developed as the final project for the Mobile Applications course (A.A 2025/26) at Università degli Studi del Piemonte Orientale.

## Overview
The application connects property owners (Hosts) with travelers (Renters) through a complete booking ecosystem, including real-time chat, property search via Google Maps, bidirectional reviews, and an administrative dashboard for system moderation.

## Architecture
The project adheres to Clean Architecture principles, enforcing the Dependency Inversion Principle to decouple the core business logic from framework and infrastructure details.

- **Domain Layer**: Contains pure Kotlin business entities and Use Cases. This layer has no dependencies on the Android SDK, Jetpack Compose, or Firebase, allowing for isolated unit testing.
- **Data Layer**: Implements the Domain repository interfaces. Acts as an Anti-Corruption Layer by mapping Firebase-specific Data Transfer Objects (DTOs) to Domain models. Interfaces with Firestore, Firebase Auth, Cloud Storage, and Firebase Cloud Messaging (FCM).
- **UI Layer**: Implements the Unidirectional Data Flow (UDF) pattern using Jetpack Compose and Hilt for dependency injection. ViewModels expose immutable state (`StateFlow<UiState>`) to stateless Compose screens.

## Technical Implementation Details
- **Concurrency & Idempotency**: Booking creation handles race conditions and network unreliability via Optimistic Concurrency Control. It utilizes a separate `property_locks` Firestore collection to act as a temporary semaphore during transactions, and relies on client-generated UUIDs as idempotency keys to safely manage retries without double-booking.
- **Reactive Data Streams**: Real-time features (like chat and session management) use Kotlin Coroutines and `callbackFlow` to adapt Firestore snapshot listeners into lifecycle-aware streams.
- **Event-Driven Denormalization**: Due to the lack of native JOINs and aggregations in Firestore, read-heavy metrics (e.g., average review scores) are computed asynchronously by Firebase Cloud Functions triggering on document writes.

## Testing & Continuous Integration
The repository enforces quality standards through automated testing and CI pipelines.

- **Unit Testing**: Domain Use Cases and Data Repositories are tested using JUnit and MockK. Critical paths, such as concurrent booking attempts and transient network failures, are verified through simulated high-load coroutine tests.
- **Code Coverage**: Enforced via JaCoCo with a strict minimum threshold (70% line coverage) required for successful builds.
- **CI/CD Pipeline**: Configured via GitLab CI (`.gitlab-ci.yml`) using Dockerized Android build environments. Stages include static analysis (ktlint), automated testing, coverage verification, and artifact generation (APK).

## Setup & Configuration
1. Clone repository.
2. Build project using Gradle (`./gradlew assembleDebug`).

## Authors
- Arben Mema
- Luca Lupi
