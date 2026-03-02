# Bet Action Engine

A Kotlin-based service that consumes market price events from NATS and determines trading actions (BET_UP, BET_DOWN, WAIT, NO_ACTION).

## Features

- Consumes `MarketAggregatedPrice` events via NATS
- Determines action status based on market conditions
- Publishes action status changes via NATS using Protobuf
- Deduplication: Only publishes when status changes
- Clean Code & SOLID principles
- Full Protobuf serialization support

## Technology Stack

- **Language**: Kotlin with Java 21
- **Messaging**: NATS
- **Serialization**: Protobuf
- **Build**: Gradle

## Getting Started

### Prerequisites

- Java 21+
- NATS server running on `localhost:4222` (or configured via `application.properties`)

### Build

```bash
./gradlew build
```

### Run

```bash
./gradlew run
```

## Configuration

Edit `src/main/resources/application.properties` to configure:
- NATS server address
- Input and output topic names

## Project Structure

```
src/main/kotlin/com/polytradings/betaction/
├── BetActionApplication.kt          # Entry point
├── config/
│   └── NatsConfig.kt                # NATS configuration
├── domain/
│   ├── model/
│   │   └── ActionStatus.kt          # Action status enum
│   └── port/
│       ├── MarketEventPort.kt       # Input port
│       └── ActionPublisherPort.kt   # Output port
├── infrastructure/
│   └── nats/
│       ├── NatsMarketEventListener.kt
│       └── NatsActionPublisher.kt
└── application/
    └── BetActionService.kt          # Business logic orchestration
```

## Next Steps

1. Implement `determineAction()` logic in `BetActionService.kt`
2. Add unit tests
3. Configure CI/CD pipeline
4. Deploy to production environment

## License

Polytradings
