# Calculator (Wit Software Interview Challenge)

This repository contains a **Java/Spring Boot** multi-module application implementing a simple calculator service. It was developed as a coding exercise for Wit Software to demonstrate proficiency and code patterns with **Java 21**, **Gradle**, **Spring Boot**, and **Docker**. The application consists of two microservices:

- **Calculator Service**: A Spring Boot application that listens for calculation requests via Kafka, computes the result using `BigDecimal`, and replies with the result or an error.
- **REST Service**: A Spring Boot web application that exposes HTTP endpoints for performing arithmetic operations. It sends requests to the Calculator Service over Kafka and returns the result to the client. A request ID (`X-Request-Id`) is added to each request/response for traceability.

The system uses **Apache Kafka** (with Zookeeper) to connect the two services in a request-reply pattern. Docker Compose is provided to run all components (Zookeeper, Kafka broker, Calculator Service, REST Service) locally.



## Prerequisites

- **Java 21**  
- **Gradle** (or use the included Gradle Wrapper)  
- **Docker & Docker Compose**  
- **Ports**:  
  - REST API → `8080`  
  - Calculator service → `8081`  
  - Kafka → `9092`  
  - Zookeeper → `2181`  


## Features & Structure

- **Arithmetic Operations**: Supports addition (`sum`), subtraction (`subtraction`), multiplication (`multiplication`), and division (`division`). Division by zero is handled gracefully by returning an error.
- **Request–Reply via Kafka**: The REST service sends a `CalculatorRequest(op, a, b)` message to the `calculator.requests` Kafka topic and waits for a `CalculatorResponse` from the `calculator.replies` topic. This is implemented using Spring Kafka’s `ReplyingKafkaTemplate`.
- **Spring Boot REST API**: The REST service provides HTTP GET endpoints:
  - `/sum?a=NUM&b=NUM`
  - `/subtraction?a=NUM&b=NUM`
  - `/multiplication?a=NUM&b=NUM`
  - `/division?a=NUM&b=NUM`

- **Request ID Logging**: Every HTTP request is tagged with an `X-Request-Id` header. The `RequestIdFilter` ensures a UUID is generated if the client does not supply one. This ID is stored in the SLF4J MDC so logs from both services include the same request ID for tracing.
- **Configuration**: Both services read Kafka settings and calculator parameters from `application.properties`. For example, `calculator.scale` and `calculator.rounding` can be set to define precision for division.
- **Unit Tests**: JUnit tests are included for the calculator logic (`BigDecimalCalculatorTest`) and the REST controller (`CalculatorControllerTest` with mocked service).

---

## Running the application with Docker

A docker-compose.yaml is provided. To run the entire system:

```bash
docker-compose up --build
```

This will start:

- Zookeeper (2181)
- Kafka (9092)
- Calculator service (8081)
- REST API (8080)

Stop services with Ctrl+C or clean up with:
```bash
docker-compose down
```

## Building the Project

From the project root:

```bash
# Build all modules (compile and run tests)
./gradlew clean :calculator:bootJar :rest:bootJar
```

After building is successful, start the application with:
```bash
docker-compose up --build
```

## Usage Examples

```bash
# Addition
curl "http://localhost:8080/sum?a=10&b=5"
# {"result":"15"}

# Subtraction
curl "http://localhost:8080/subtraction?a=10&b=5"
# {"result":"5"}

# Multiplication
curl "http://localhost:8080/multiplication?a=3&b=4"
# {"result":"12"}

# Division
curl "http://localhost:8080/division?a=20&b=4"
# {"result":"5"}

# Division by zero
curl "http://localhost:8080/division?a=20&b=0"
# {"error":"Division by zero"}

# With specified logId
curl -v -H "X-Request-Id: test-123-afonso" "http://localhost:8080/sum?a=5&b=9"
```

## References 
- https://spring.io/projects/spring-boot
- https://spring.io/projects/spring-kafka
- https://kafka.apache.org/
