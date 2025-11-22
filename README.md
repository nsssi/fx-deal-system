# **FX Deal System – README**

## **Overview**

FX Deal System is a complete Spring Boot application that validates, processes, and stores foreign exchange (FX) deal transactions.
It fulfills all requirements of the SDET assignment, including:

✔ REST API endpoints
✔ Full validation rules
✔ Error handling with global exception mapping
✔ Unit, API, and integration testing
✔ Testcontainers (real database tests)
✔ Docker & Docker Compose support
✔ JaCoCo test coverage
✔ K6 performance/load testing
✔ Postman collection & environment

This project is built as a real-world enterprise microservice connected to MySQL.

---

## **Tech Stack**

* **Java 17**
* **Spring Boot 3.4**
* **MySQL 8**
* **Hibernate / JPA**
* **Docker & Docker Compose**
* **JUnit 5 & RestAssured**
* **Testcontainers**
* **Jacoco**
* **K6 (Load Testing)**
* **Postman**

---

## **Project Structure**

```
src/
 ├── main/java/com/progressoft/fxdealsystem
 │    ├── controller/        → REST API endpoints
 │    ├── service/           → Business logic & validations
 │    ├── repository/        → Spring Data JPA repository
 │    ├── model/             → Entities (Deal)
 │    ├── dto/               → Request/Response DTOs
 │    └── exception/         → Global exception handling
 │
 └── test/java/com/progressoft/fxdealsystem
      ├── api/               → RestAssured API tests
      ├── service/           → Unit tests (service logic)
      └── repository/        → Integration tests (Testcontainers)
```

---

##  **Testing**

### **Unit Tests**

Located in:

```
src/test/java/.../service
```

Covers:

* Mandatory fields
* ISO currency validation
* Future timestamp detection
* Duplicate prevention
* Business logic correctness

---

### **API Tests (RestAssured)**

Tests:

* POST `/api/deals`
* POST `/api/deals/bulk`
* GET `/api/deals/{dealUniqueId}`
* GET `/api/deals`
* Full error responses

---

###  **Integration Tests (Testcontainers)**

Located in `/repository`.

Runs MySQL **inside Docker automatically**.
Verifies:

* Table creation
* Insert + read
* Unique constraints
* Data persistence integrity

---

###  **Jacoco Coverage**

Generate:

```
mvn clean test
mvn jacoco:report
```

Report:

```
target/site/jacoco/index.html
```

---

##  **Docker Instructions**

### 1 Start Application with Database

```
docker compose up --build
```

This launches:

| Service  | Port | Description     |
| -------- | ---- | --------------- |
| fx_mysql | 3307 | MySQL Database  |
| fx_app   | 8080 | Spring Boot API |

---

### 2 Stop & Cleanup

```
docker compose down -v
```

---

## **Profiles & Configuration**

### Active Profiles:

* **local** (default)
* **docker** (activated by Docker Compose)

### application-docker.properties

```
spring.datasource.url=jdbc:mysql://fx_mysql:3306/fx_deals_db
spring.datasource.username=root
spring.datasource.password=root
spring.jpa.hibernate.ddl-auto=update
```

---

## **API Endpoints**

### Health Check

```
GET /api/deals/health
```

**Response:**

```
FX Deal System is running!
```

---

### Import Single Deal

```
POST /api/deals
```

Body:

```
{
  "dealUniqueId": "D1001",
  "fromCurrencyIsoCode": "USD",
  "toCurrencyIsoCode": "EUR",
  "dealTimestamp": "2024-01-01T10:00:00",
  "dealAmount": 1000
}
```

---

### Import Bulk Deals

```
POST /api/deals/bulk
```

---

### Get All Deals

```
GET /api/deals
```

---

### Get Deal by Unique ID

```
GET /api/deals/{dealUniqueId}
```

---



## **Postman Collection**

Files included:

```
/postman/fx-deal-system.postman_collection.json
```

Contains:

✔ All endpoints
✔ Valid + invalid tests
✔ Duplicate test


---

## **K6 Load Test**

File: **K6_test.js**

Run:

```
k6 run K6_test.js
```

Simulates:

* High concurrency
* Multiple POST requests
* Unique deal IDs per iteration

---

## **Makefile**

```
make up         # docker compose up
make down       # docker compose down -v
make test       # run all tests
make coverage   # generate jacoco
make k6         # run k6 load test
```

---

## **Requirements → Test Mapping**

| Requirement         | Test File                     | Status |
| ------------------- | ----------------------------- | ------ |
| Single import       | DealApiTest                   | ✔      |
| Bulk import         | DealApiTest                   | ✔      |
| Validations         | DealServiceTest               | ✔      |
| Duplicate handling  | DealServiceTest               | ✔      |
| No rollback on bulk | DealApiTest                   | ✔      |
| Integration with DB | DealRepositoryIntegrationTest | ✔      |
| Docker support      | docker-compose.yml            | ✔      |
| Load testing        | loadtest.js                   | ✔      |
| Documentation       | README.md                     | ✔      |

---

## **Final Notes**

This project is **production-ready**, fully aligned with SDET expectations, and includes:

* Clean architecture
* Complete validation flow
* High test coverage
* Real MySQL testing
* Dockerized application
* Performance testing
* Postman scripts
* Professional documentation


---

**Created by: Nassim Megrini**
**nassimmegrini51@gmail.com**

