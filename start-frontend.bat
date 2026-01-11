@echo off
REM ============================================================
REM ECOMplus Angular Frontend Startup Script
REM ============================================================

echo =================================================
echo       ECOMplus Frontend Startup
echo =================================================
echo.

cd ecom-web-app

REM Check if node_modules exists
if not exist "node_modules" (
    echo Installing npm dependencies...
    call npm install
    echo.
)

echo Starting Angular development server...
call npm start

pause
