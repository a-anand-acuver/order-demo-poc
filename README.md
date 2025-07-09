# Acuver Demo Micro-services

This repository contains three Spring Boot micro-services that communicate over Kafka and share a common PostgreSQL instance (with three databases):

1. **order-demo**  – Order management & orchestration
2. **payment-demo** – Payment authorization / charging
3. **shipment-demo** – Shipment creation & processing

They all publish/consume JSON events on the single Kafka topic `order-topic` using different consumer groups.

---
## Prerequisites

* **Java 24** (or Java tool-chain 24 as defined in each `build.gradle.kts`)
* **Docker + Docker Compose** – used for PostgreSQL, Kafka & Zookeeper

---
## How to Run

### 1. Start **order-demo** (will spin up infrastructure)
```bash
cd order-demo
./gradlew bootRun
```
This will automatically:
* Launch Docker Compose (PostgreSQL + Kafka/Zookeeper) defined in `compose.yaml`
* Apply Flyway migrations creating three databases: `order`, `payment`, `shipment`
* Create the Kafka topic `order-topic` (via AdminClient)

> Leave this service running in its terminal.

### 2. Start **payment-demo**
Open a new terminal:
```bash
cd payment-demo
./gradlew bootRun
```
The payment service will listen on port **8081**.

### 3. Start **shipment-demo**
```bash
cd shipment-demo
./gradlew bootRun
```
The shipment service will listen on port **8082**.

---
## Testing the Flow

### 1. Create an Order
```bash
curl -X POST http://localhost:8080/api/events/create-order \
     -H 'Content-Type: application/json' \
     -d '{
           "customerId":"C1",
           "productId":"P1",
           "quantity":2,
           "price":50,
           "totalAmount":100
         }'
```
* Order service stores order → publishes `ORDER_CREATED`
* Payment service consumes → publishes `ORDER_AUTHORIZED` or `AUTH_FAILED`
* Shipment service (on even orders) consumes `ORDER_AUTHORIZED`, creates shipment → publishes `SHIPMENT_CREATED`

### 2. Process a Shipment Pick-up
Pick a `shipmentId` returned from previous step:
```bash
curl -X POST "http://localhost:8082/api/shipments/process?shipmentId=<SHIP_ID>"
```
* Shipment status moves to **PICKED** → publishes `SHIPMENT_PICKED`
* Payment service consumes and publishes `ORDER_CHARGED`
* Order service consumes and sets payment status **SETTLED**

### 3. List Shipments
```bash
curl http://localhost:8082/api/shipments
```

---
## Swagger & Kafka UI

Each Spring Boot service contains the springdoc-openapi dependency (or you can add it) which exposes interactive API docs at:

| Service | Swagger UI |
|---------|------------|
| Order   | http://localhost:8080/swagger-ui/index.html |
| Payment | http://localhost:8081/swagger-ui/index.html |
| Shipment| http://localhost:8082/swagger-ui/index.html |

Kafka topics can be inspected with a UI by running the open-source [Kafka UI](https://github.com/provectus/kafka-ui):

```
UI Tool | URL |
|---------|-----|
| Kafka UI | http://localhost:9090 |

```
