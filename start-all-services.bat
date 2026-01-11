@echo off
REM ============================================================
REM ECOMplus Microservices Startup Script
REM Run this script to start all microservices in the correct order
REM ============================================================

echo =================================================
echo       ECOMplus Microservices Startup
echo =================================================
echo.

REM Start Discovery Service (Eureka) - Must start first
echo [1/6] Starting Discovery Service (Eureka - Port 8761)...
cd discovery-service
start "Discovery Service (8761)" cmd /k "mvnw spring-boot:run"
cd ..
echo      Waiting 20 seconds for Eureka to initialize...
timeout /t 20 /nobreak >nul

REM Start Config Service
echo [2/6] Starting Config Service (Port 9999)...
cd config-service
start "Config Service (9999)" cmd /k "mvnw spring-boot:run"
cd ..
echo      Waiting 15 seconds for Config Server to initialize...
timeout /t 15 /nobreak >nul

REM Start Gateway Service
echo [3/6] Starting Gateway Service (Port 8888)...
cd gateway-service
start "Gateway Service (8888)" cmd /k "mvnw spring-boot:run"
cd ..
echo      Waiting 10 seconds for Gateway to initialize...
timeout /t 10 /nobreak >nul

REM Start Customer Service
echo [4/6] Starting Customer Service (Port 8081)...
cd customer-service
start "Customer Service (8081)" cmd /k "mvnw spring-boot:run"
cd ..
echo      Waiting 10 seconds for Customer Service to initialize...
timeout /t 10 /nobreak >nul

REM Start Inventory Service
echo [5/6] Starting Inventory Service (Port 8082)...
cd inventory-service
start "Inventory Service (8082)" cmd /k "mvnw spring-boot:run"
cd ..
echo      Waiting 10 seconds for Inventory Service to initialize...
timeout /t 10 /nobreak >nul

REM Start Billing Service
echo [6/7] Starting Billing Service (Port 8083)...
cd billing-service
start "Billing Service (8083)" cmd /k "mvnw spring-boot:run"
cd ..
echo      Waiting 10 seconds for Billing Service to initialize...
timeout /t 10 /nobreak >nul

REM Start Chatbot Service
echo [7/7] Starting Chatbot Service (Port 8084)...
cd chatbot-service
start "Chatbot Service (8084)" cmd /k "mvnw spring-boot:run"
cd ..

REM Start Kafka Producer, Consumer, Streams, and Supplier
echo [8-11/11] Starting Kafka Ecosystem Services...
start "Kafka Producer (8085)" cmd /k "cd kafka-producer-service && ..\mvnw spring-boot:run"
start "Kafka Consumer (8086)" cmd /k "cd kafka-consumer-service && ..\mvnw spring-boot:run"
start "Kafka Streams (8087)" cmd /k "cd kafka-streams-service && ..\mvnw spring-boot:run"
start "Kafka Supplier (8088)" cmd /k "cd kafka-supplier-service && ..\mvnw spring-boot:run"

echo.
echo =================================================
echo All microservices are starting in separate windows
echo =================================================
echo.
echo Service URLs:
echo   Eureka Dashboard:  http://localhost:8761
echo   Config Server:     http://localhost:9999
echo   API Gateway:       http://localhost:8888
echo   Customer Service:  http://localhost:8081
echo   Inventory Service: http://localhost:8082
echo   Billing Service:   http://localhost:8083
echo   Chatbot Service:   http://localhost:8084
echo.
echo API Endpoints (via Gateway):
echo   Customers: http://localhost:8888/api/customers
echo   Products:  http://localhost:8888/api/products
echo   Chatbot:   http://localhost:8888/api/chatbot
echo.
echo Press any key to exit this window...
pause >nul
