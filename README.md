# Java LLD Designs

An interview-focused collection of Low-Level Design implementations in Java. Each design is an independent Maven module beneath a small parent/aggregator project, so it can be understood, tested, and extended in isolation.

The repository prioritizes clarity, correctness, extensibility, testability, and design quality. Core designs remain framework-independent; Spring Boot will only be introduced when an application boundary such as a REST API adds genuine value.

## Modules

| Module | Focus |
| --- | --- |
| `rate-limiter` | Strategy-based, per-client token bucket |
| `parking-lot` | Typed spots, pluggable allocation, live occupancy |
| `movie-ticket-booking` | Expiring seat locks and atomic booking |
| `notification-system` | Multi-channel strategies and delivery observers |
| `in-memory-cache` | O(1) LRU and LFU eviction |
| `elevator-system` | Scheduling, direction, stops, and state transitions |
| `logger` | Severity filtering, formatting, appenders, and structured context |
| `task-scheduler` | Delayed execution, cancellation, lifecycle, and failure isolation |

### Rate Limiter

#### Problem statement

Decide whether a client may consume one or more permits while enforcing a configured request rate. Clients are identified independently (for example by user ID or API key), so one client's traffic cannot consume another client's allowance.

#### Functional requirements

- Accept a client identifier and a positive permit count.
- Allow requests while the client has sufficient capacity.
- Reject requests that exceed the available capacity.
- Report remaining permits and an estimated retry delay.
- Maintain independent state for multiple clients.

#### Non-functional requirements

- Safe under concurrent access.
- Algorithm details hidden behind a common abstraction.
- No global mutable state or external infrastructure.
- Injectable time and configuration for deterministic tests.
- Extensible without changing callers.

#### Supported algorithms

| Algorithm | Status |
| --- | --- |
| Token bucket | Implemented |
| Fixed window | Planned |
| Sliding window | Planned |

The token bucket allows short bursts up to its capacity and replenishes permits continuously at the configured rate.

#### Design approach

`RateLimiterStrategy` is the algorithm boundary. `RateLimiterService` depends only on that interface and remains unchanged when a different implementation is supplied. `TokenBucketRateLimiter` owns per-client bucket state in a concurrent map. Configuration lives separately in the immutable `TokenBucketConfig` value object.

#### Important classes and interfaces

- `RateLimitRequest` — immutable client ID and requested permit count.
- `RateLimitResult` — immutable decision, remaining permits, and retry delay.
- `RateLimiterStrategy` — common extension point for all algorithms.
- `TokenBucketRateLimiter` — current thread-safe strategy.
- `TokenBucketConfig` — capacity and refill-rate configuration.
- `RateLimiterService` — client-facing facade using dependency inversion.

#### Thread safety

Client buckets are held in a `ConcurrentHashMap`. Each refill-and-consume decision is performed atomically with `compute`, preventing concurrent requests for the same client from overspending permits. Different clients can proceed independently. Configuration and API models are immutable.

## Prerequisites

- JDK 21
- Maven 3.9+

## Build and test

From the repository root:

```bash
mvn clean verify
```

Build only this module and required parent projects:

```bash
mvn -pl rate-limiter -am package
```

Run all tests, or only rate-limiter tests:

```bash
mvn test
mvn -pl rate-limiter test
```

All modules are framework-independent libraries and intentionally have no standalone servers yet.

### Parking Lot

Models motorcycle, car, and truck spots. `SpotAllocationStrategy` makes allocation replaceable, while atomic spot occupancy prevents two vehicles from claiming the same place. `ParkingLot` issues tickets, handles exits, and exposes live availability.

### Movie Ticket Booking

`ShowSeatService` manages one show's seats. Multi-seat locks are all-or-nothing, expire after a configured duration, and can only be confirmed by their owner. A per-show lock serializes state transitions to prevent double booking and partial reservations.

### Notification System

`NotificationSender` is the channel strategy, with Email, SMS, and Push implementations backed by injected gateways. `NotificationService` routes to requested channels and publishes successful deliveries to registered observers.

### In-Memory Cache

`Cache` defines the common contract. `LruCache` combines a hash map with a custom doubly linked list. `LfuCache` uses frequency buckets with recency ordering. Both provide average O(1) get/put/eviction and synchronize compound mutations.

### Elevator System

Each `Elevator` is a state machine with idle, moving, and maintenance states, ordered stops, direction changes, and bounded floors. `ElevatorSelectionStrategy` separates dispatch policy; the initial nearest-car strategy favors elevators already traveling toward a pickup.

### Logger

`Logger` creates immutable log events, filters them by severity, and fans them out to pluggable appenders. Formatting and output are separate strategies. Console output is synchronized, while the in-memory appender supports concurrent writers and deterministic tests. Events support exceptions and structured context without relying on a logging framework.

### Task Scheduler

`TaskScheduler` defines one-time scheduling independently of the execution mechanism. `InMemoryTaskScheduler` uses a configurable worker pool, tracks task state through `TaskHandle`, supports cancellation, isolates task failures, and cleanly cancels pending work when closed.

## Running one module

Replace `<module>` with any module name from the table:

```bash
mvn -pl <module> -am test
```
