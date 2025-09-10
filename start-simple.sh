#!/bin/bash

# Simple startup script for Notification Service
# Ensures PostgreSQL is running before starting the app

set -e

echo "🚀 Starting Notification Service..."

# Set Java version
export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home

# Check PostgreSQL
echo "📦 Checking PostgreSQL..."
if ! lsof -i :5432 >/dev/null 2>&1; then
    echo "🔄 Starting PostgreSQL..."
    brew services start postgresql || {
        echo "❌ Failed to start PostgreSQL. Please start it manually."
        exit 1
    }
    
    # Wait for PostgreSQL to be ready
    echo "⏳ Waiting for PostgreSQL..."
    sleep 3
fi

# Check if database exists
if ! psql -h localhost -U sandeepkumaryadav -d notification_db -c '\q' >/dev/null 2>&1; then
    echo "🗄️  Creating database..."
    createdb -U sandeepkumaryadav notification_db 2>/dev/null || echo "Database might already exist"
fi

# Check if Kafka is running, start if needed
if ! lsof -i :9092 >/dev/null 2>&1; then
    echo "🔄 Starting Kafka (optional)..."
    brew services start kafka || echo "⚠️  Kafka startup failed, continuing without it"
    sleep 2
fi

echo "✅ All dependencies ready!"
echo "🌐 Starting application on http://localhost:8080"
echo "📖 Swagger UI: http://localhost:8080/swagger-ui.html"
echo ""
echo "Press Ctrl+C to stop"

# Start the application
mvn spring-boot:run