# ECOMplus - E-Commerce Microservices Application

A complete e-commerce platform built with Spring Boot microservices and Angular frontend.

## 🏗️ Architecture

This project follows a **microservices architecture** with the following services:

| Service               | Port | Description                         |
| --------------------- | ---- | ----------------------------------- |
| **Discovery Service** | 8761 | Eureka Server for service discovery |
| **Config Service**    | 9999 | Spring Cloud Config Server          |
| **Gateway Service**   | 8888 | API Gateway (Spring Cloud Gateway)  |
| **Customer Service**  | 8081 | Customer management                 |
| **Inventory Service** | 8082 | Product/Inventory management        |
| **Billing Service**   | 8083 | Billing and invoicing               |
| **Angular Frontend**  | 4201 | Web application                     |

## 📋 Prerequisites

- **Java 17** or higher
- **Maven 3.8+** (or use the included Maven wrapper)
- **Node.js 18+** and npm (for the frontend)
- **Git** (for Config Server)

## 🚀 Quick Start

### Option 1: Using Startup Scripts (Windows)

1. **Start all backend services:**

   ```batch
   start-all-services.bat
   ```

   This will open separate terminal windows for each microservice.

2. **Start the frontend:**
   ```batch
   start-frontend.bat
   ```

### Option 2: Manual Startup

**Important:** Start services in the following order:

#### Step 1: Start Discovery Service (Eureka)

```bash
cd discovery-service
./mvnw spring-boot:run
```

Wait for message: `Started DiscoveryServiceApplication`

#### Step 2: Start Config Service

```bash
cd config-service
./mvnw spring-boot:run
```

#### Step 3: Start Gateway Service

```bash
cd gateway-service
./mvnw spring-boot:run
```

#### Step 4: Start Business Services

```bash
# Terminal 1
cd customer-service
./mvnw spring-boot:run

# Terminal 2
cd inventory-service
./mvnw spring-boot:run

# Terminal 3
cd billing-service
./mvnw spring-boot:run
```

#### Step 5: Start Frontend

```bash
cd ecom-web-app
npm install
npm start
```

## 🌐 Service URLs

| Service          | URL                   |
| ---------------- | --------------------- |
| Eureka Dashboard | http://localhost:8761 |
| Config Server    | http://localhost:9999 |
| API Gateway      | http://localhost:8888 |
| Angular App      | http://localhost:4201 |

## 📡 API Endpoints (via Gateway)

| Endpoint              | Description        |
| --------------------- | ------------------ |
| `GET /api/customers`  | List all customers |
| `POST /api/customers` | Create a customer  |
| `GET /api/products`   | List all products  |
| `POST /api/products`  | Create a product   |

## 🔧 Technology Stack

### Backend

- **Spring Boot 3.4.2**
- **Spring Cloud 2024.0.0**
  - Spring Cloud Gateway
  - Spring Cloud Config
  - Spring Cloud Netflix Eureka
  - OpenFeign
- **Spring Data JPA**
- **H2 Database** (in-memory)
- **Lombok**

### Frontend

- **Angular 21**
- **Bootstrap 5**
- **RxJS**

## 📁 Project Structure

```
ECOMplus/
├── config-repo/           # Git repository for centralized configuration
├── config-service/        # Spring Cloud Config Server
├── discovery-service/     # Eureka Server
├── gateway-service/       # Spring Cloud Gateway
├── customer-service/      # Customer microservice
├── inventory-service/     # Inventory/Product microservice
├── billing-service/       # Billing microservice
├── ecom-web-app/          # Angular frontend
├── start-all-services.bat # Windows startup script (backend)
├── start-frontend.bat     # Windows startup script (frontend)
└── README.md
```

## 🛠️ Development Tips

### Testing API Gateway

```bash
# Test customer endpoint
curl http://localhost:8888/api/customers

# Test product endpoint
curl http://localhost:8888/api/products
```

### Viewing H2 Console

Each service has an H2 console available:

- Customer Service: http://localhost:8081/h2-console
- Inventory Service: http://localhost:8082/h2-console
- Billing Service: http://localhost:8083/h2-console

### Health Checks

```bash
# Gateway health
curl http://localhost:8888/actuator/health

# Eureka info
curl http://localhost:8761/actuator/health
```

## 🐛 Troubleshooting

### Services not registering with Eureka

- Ensure Discovery Service is running and healthy
- Check `eureka.client.service-url.defaultZone` in each service

### Config Server issues

- Verify the `config-repo` git repository exists and has configurations
- Check the path in `config-service/src/main/resources/application.properties`

### CORS issues

- The Gateway is configured to allow CORS from `http://localhost:4201`
- Check `gateway-service/src/main/resources/application.yml`

## 📝 License

This project is for educational purposes.
