# VoidRunner — Distributed Order Fulfillment Engine

A distributed, event-driven e-commerce order fulfillment system built with Java, Spring Boot, and Apache Kafka — simulating how real platforms like Amazon or Flipkart process orders across multiple backend services.

**The core engineering challenge this project solves:** guaranteeing that 100 concurrent customers cannot all successfully purchase the same last unit of stock — without sacrificing performance.

---

## Architecture

```
                         ┌─────────────────┐
                         │   React + TS    │   (planned)
                         │   Web Dashboard │
                         └────────┬────────┘
                                  │
                           REST / WebSocket
                                  │
                         ┌────────▼────────┐
                         │   API Gateway   │   (planned)
                         └────────┬────────┘
                                  │
             ┌────────────────────┼────────────────────┬──────────────────┐
             │                    │                     │                  │
             ▼                    ▼                     ▼                  ▼
      ┌─────────────┐      ┌─────────────┐      ┌──────────────┐  ┌──────────────┐
      │    Order    │      │  Inventory  │      │   Payment    │  │ Fulfillment  │
      │   Service   │      │   Service   │      │   Service    │  │   Service    │
      │  (order_db) │      │(inventory_db)│     │ (payment_db) │  │(fulfillment_db)
      └──────┬──────┘      └──────┬──────┘      └──────┬───────┘  └──────┬───────┘
             │                    │                     │                 │
             └────────────────────┴─────────┬───────────┴─────────────────┘
                                             │
                                      ┌──────▼──────┐
                                      │    Kafka    │
                                      └──────┬──────┘
                                             │
                                      ┌──────▼──────┐
                                      │  Shipping   │
                                      │   Service   │
                                      │(shipping_db)│
                                      └─────────────┘
```

Each service owns its own PostgreSQL database (**database-per-service** pattern) and communicates exclusively through **Kafka events** — never through direct synchronous calls to one another. This keeps services loosely coupled: if one service is temporarily down, the others keep working, and messages simply wait in the queue.

---

## The Saga Flow

An order moves through the system as a chain of asynchronous events — a textbook **saga pattern** for distributed transactions:

```
POST /api/orders
      │
      ▼
Order created  (status: CREATED)
      │  Kafka: order.created
      ▼
Inventory reserved  (status: CONFIRMED)
      │  Kafka: inventory.reserved
      ▼
Payment processed  (status: PAID)
      │  Kafka: payment.completed
      ▼
Fulfillment completed  (status: FULFILLED)
      │  Kafka: fulfillment.completed
      ▼
Shipment dispatched  (status: SHIPPED)
```

If any step fails (e.g. insufficient stock, payment declined), the corresponding `*.failed` event is published instead, and the order's status reflects the failure (`FAILED` / `PAYMENT_FAILED`) rather than silently hanging.

---

## The Core Problem: Preventing Overselling Under Concurrency

**The question:** if a product has exactly 1 unit left in stock, and 100 customers click "buy" at the same instant, how do you guarantee only one succeeds?

**The solution:** optimistic locking via Hibernate's `@Version` annotation, backed by a database-level safety net.

- Every inventory row carries a `version` column.
- When a reservation is attempted, Hibernate issues an `UPDATE ... WHERE id = ? AND version = ?`.
- If another request already modified that row, the version no longer matches — the update affects zero rows, and Hibernate throws `OptimisticLockingFailureException`.
- The service catches this, retries a bounded number of times (max 3) with a small backoff, and fails cleanly if contention persists.
- As a last line of defense, a `CHECK (available_qty >= 0)` constraint at the database level guarantees stock can never go negative — even in the face of an application bug.

This was verified with an actual concurrency test: 100 parallel requests fired at 1 unit of stock, using PowerShell's `ForEach-Object -Parallel`.

**Result: exactly 1 succeeded, 99 were safely rejected — zero overselling.**

Idempotency keys are also used throughout the saga so that duplicate Kafka deliveries (an inherent possibility with at-least-once delivery semantics) never cause double-processing.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 (LTS) |
| Framework | Spring Boot 4 |
| Data Access | Spring Data JPA / Hibernate |
| Database | PostgreSQL (one database per service) |
| Schema Migrations | Flyway |
| Messaging | Apache Kafka |
| Containerization | Docker / Docker Compose |
| Build Tool | Maven (multi-module) |
| CI/CD | GitHub Actions |

---

## Services

| Service | Port | Database | Responsibility |
|---|---|---|---|
| Order Service | 8081 | `order_db` | Order creation, order lifecycle / final status |
| Inventory Service | 8082 | `inventory_db` | Stock management, concurrency-safe reservations |
| Payment Service | 8084 | `payment_db` | Payment processing (simulated) |
| Fulfillment Service | 8083 | `fulfillment_db` | Pick/pack simulation |
| Shipping Service | 8085 | `shipping_db` | Shipment dispatch simulation |
| Notification Service | 8086 | — | *(planned)* |
| API Gateway | 8080 | — | *(planned)* |

---

## Running Locally

### Prerequisites
- Java 21
- Maven
- Docker Desktop

### 1. Start infrastructure
```bash
docker compose up -d
```
This starts PostgreSQL, Redis, and Kafka in containers.

### 2. Build the project
```bash
mvn clean install -DskipTests
```

### 3. Start each service (separate terminals)
```bash
cd order-service && mvn spring-boot:run
cd inventory-service && mvn spring-boot:run
cd payment-service && mvn spring-boot:run
cd fulfillment-service && mvn spring-boot:run
cd shipping-service && mvn spring-boot:run
```

### 4. Try it out

Add stock:
```bash
curl -X POST http://localhost:8082/api/inventory/stock \
  -H "Content-Type: application/json" \
  -d '{"sku":"ITEM-001","warehouseId":"00000000-0000-0000-0000-000000000001","quantity":10}'
```

Create an order:
```bash
curl -X POST http://localhost:8081/api/orders \
  -H "Content-Type: application/json" \
  -d '{"customerName":"Test Customer","items":[{"sku":"ITEM-001","quantity":1,"unitPrice":100.00}]}'
```

Check order status (after a few seconds, once the saga completes):
```bash
curl http://localhost:8081/api/orders/{orderId}
```

---

## API Endpoints

**Order Service**
- `POST /api/orders` — create an order
- `GET /api/orders/{id}` — get order by ID
- `GET /api/orders` — list all orders

**Inventory Service**
- `POST /api/inventory/stock` — add stock (testing/admin use)
- `POST /api/inventory/reserve` — reserve stock directly (testing use)

---

## What's Next

- API Gateway (Spring Cloud Gateway) as a single entry point
- Notification Service
- Resilience4j circuit breakers for fault tolerance
- React + TypeScript dashboard with real-time WebSocket order tracking
- Expanded automated test suite (Testcontainers-based integration tests)

---

## A Debugging Story Worth Mentioning

During development, the Kafka saga flow stopped working silently — consumers would subscribe to topics but never receive messages, with no errors thrown. After systematically verifying the producer (events were being published successfully), the topic itself (messages were landing in Kafka), and the consumer group status via Kafka's CLI tools, the root cause turned out to be a classic single-node Kafka gotcha: the default replication factor for Kafka's internal `__consumer_offsets` topic is 3, but only one broker was running. This silently blocked consumer group coordination. The fix was setting `KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR=1` in the Docker Compose configuration for single-node setups.

---

## License

This is a personal portfolio/learning project.