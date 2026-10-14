## Architecture Diagram 

## Component Breakdown & Key Responsibilities

## 1.Authentication Layer (Firebase Auth)
Validates user credentials before entry.
Maintains active operator sessions across restarts.

## 2.State & UI Engine (MainActivity)
Listens directly to /jobs via WebSockets.
Populates the active Spinner whenever a job is created in CreateJobActivity.
Enforces the valid state machine engine (Assigned $\to$ Accepted $\to$ In Progress $\to$ Completed/Failed).

## 3.Data Creation (CreateJobActivity)
Generates new unique push keys in Firebase Realtime Database.
Handshakes with Retrofit API client to replicate job objects on external servers.

## 4.Network & Resilience Layer (ApiClient / OkHttp Interceptor)
Idempotency Guard: Attaches a UUID to X-Request-ID to avoid duplicate API creation requests on network drops.
Automated Retry: Automatically re-attempts up to 3 times on failed network requests with a 30-second connection timeout.
Offline Persistence: Retains cached jobs via setPersistenceEnabled(true) so the operator can interact with UI offline.