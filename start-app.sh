#!/bin/bash

# Notification Service Startup Script
# This script ensures PostgreSQL and Kafka are running before starting the application

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
POSTGRES_DB="notification_db"
POSTGRES_USER="postgres"
POSTGRES_PORT="5432"
KAFKA_PORT="9092"
APP_PORT="8080"
MAX_WAIT_TIME=60 # seconds

# Function to print colored messages
print_message() {
    local color=$1
    local message=$2
    echo -e "${color}[$(date '+%Y-%m-%d %H:%M:%S')] ${message}${NC}"
}

# Function to check if a port is in use
is_port_in_use() {
    local port=$1
    lsof -i :$port >/dev/null 2>&1
}

# Function to wait for service to be ready
wait_for_service() {
    local service_name=$1
    local port=$2
    local max_wait=$3
    local wait_time=0
    
    print_message $YELLOW "Waiting for ${service_name} to be ready on port ${port}..."
    
    while [ $wait_time -lt $max_wait ]; do
        if is_port_in_use $port; then
            print_message $GREEN "${service_name} is ready!"
            return 0
        fi
        sleep 2
        wait_time=$((wait_time + 2))
    done
    
    print_message $RED "${service_name} failed to start within ${max_wait} seconds"
    return 1
}

# Function to check and start PostgreSQL
start_postgresql() {
    print_message $BLUE "Checking PostgreSQL status..."
    
    if is_port_in_use $POSTGRES_PORT; then
        print_message $GREEN "PostgreSQL is already running on port $POSTGRES_PORT"
    else
        print_message $YELLOW "PostgreSQL not running. Starting PostgreSQL..."
        
        # Try different ways to start PostgreSQL based on installation method
        if command -v brew >/dev/null 2>&1; then
            # Homebrew installation
            print_message $YELLOW "Starting PostgreSQL via Homebrew..."
            brew services start postgresql@14 || brew services start postgresql@15 || brew services start postgresql
        elif [ -d "/Applications/Postgres.app" ]; then
            # PostgreSQL.app
            print_message $YELLOW "Starting PostgreSQL via Postgres.app..."
            open -a Postgres
        elif command -v systemctl >/dev/null 2>&1; then
            # System service
            print_message $YELLOW "Starting PostgreSQL via systemctl..."
            sudo systemctl start postgresql
        else
            print_message $RED "Could not determine how to start PostgreSQL. Please start it manually."
            return 1
        fi
    fi

    if ! wait_for_service "PostgreSQL" $POSTGRES_PORT $MAX_WAIT_TIME; then
        print_message $RED "Failed to start PostgreSQL. Please start it manually."
        return 1
    fi

    # Set up database and user as per README
    print_message $BLUE "Ensuring database and user are set up..."
    
    # Create postgres user if it doesn't exist (connect as current system user who should be superuser)
    if command -v createuser >/dev/null 2>&1; then
        createuser -s ${POSTGRES_USER} 2>/dev/null || echo "User '${POSTGRES_USER}' might already exist" >/dev/null
    else
        # Use psql to create user - connect as current system user
        psql postgres -c "CREATE USER ${POSTGRES_USER} WITH SUPERUSER PASSWORD 'password';" >/dev/null 2>&1 || true
    fi
    
    # Create database if it doesn't exist
    if command -v createdb >/dev/null 2>&1; then
        createdb -U ${POSTGRES_USER} ${POSTGRES_DB} >/dev/null 2>&1 || true
    else
        psql -U ${POSTGRES_USER} postgres -c "CREATE DATABASE ${POSTGRES_DB};" >/dev/null 2>&1 || true
    fi
    
    # Grant privileges
    psql -U ${POSTGRES_USER} postgres -c "GRANT ALL PRIVILEGES ON DATABASE \"${POSTGRES_DB}\" TO ${POSTGRES_USER};" >/dev/null 2>&1 || true
    print_message $GREEN "Database setup complete."

    return 0
}

# Function to check and start Kafka (using Homebrew)
start_kafka() {
    print_message $BLUE "Checking Kafka status..."
    
    if is_port_in_use $KAFKA_PORT; then
        print_message $GREEN "Kafka is already running on port $KAFKA_PORT"
        return 0
    fi
    
    print_message $YELLOW "Kafka not running. Starting Kafka via Homebrew..."
    
    # Start Kafka using homebrew
    if command -v brew >/dev/null 2>&1; then
        brew services start kafka
        
        if wait_for_service "Kafka" $KAFKA_PORT $MAX_WAIT_TIME; then
            return 0
        fi
    else
        print_message $YELLOW "Homebrew not found. Please install Kafka manually."
        export KAFKA_ENABLED=false
        return 0
    fi
    
    print_message $RED "Failed to start Kafka. Application will run without Kafka support."
    export KAFKA_ENABLED=false
    return 0
}

# Function to stop the application gracefully
cleanup() {
    print_message $YELLOW "Shutting down application..."
    if [ ! -z "$APP_PID" ]; then
        kill $APP_PID 2>/dev/null || true
        wait $APP_PID 2>/dev/null || true
    fi
    exit 0
}

# Set up signal handlers
trap cleanup SIGINT SIGTERM

# Main startup sequence
main() {
    print_message $BLUE "=== Notification Service Startup ==="
    print_message $BLUE "Checking dependencies..."
    
    # Check if application port is already in use
    if is_port_in_use $APP_PORT; then
        print_message $RED "Port $APP_PORT is already in use. Please stop the existing service."
        exit 1
    fi
    
    # Set Java version - auto-detect or use the correct path
    if [ -z "$JAVA_HOME" ]; then
        export JAVA_HOME=/Users/divyamaheshwari/Library/Java/JavaVirtualMachines/corretto-21.0.2/Contents/Home
        print_message $BLUE "Setting JAVA_HOME to: $JAVA_HOME"
    fi
    
    # Start PostgreSQL
    if ! start_postgresql; then
        print_message $RED "PostgreSQL is required for the application. Exiting."
        exit 1
    fi
    
    # Start Kafka (optional)
    start_kafka
    
    print_message $BLUE "All dependencies are ready. Starting the application..."
    print_message $GREEN "Application will be available at: http://localhost:$APP_PORT"
    print_message $GREEN "Swagger UI: http://localhost:$APP_PORT/swagger-ui.html"
    print_message $YELLOW "Press Ctrl+C to stop the application"
    
    # Start the Spring Boot application
    mvn spring-boot:run &
    APP_PID=$!
    
    # Wait for application to start
    if wait_for_service "Notification Service" $APP_PORT $MAX_WAIT_TIME; then
        print_message $GREEN "=== Notification Service Started Successfully! ==="
        print_message $BLUE "Logs will appear below..."
        echo ""
        
        # Keep the script running and forward application logs
        wait $APP_PID
    else
        print_message $RED "Application failed to start within $MAX_WAIT_TIME seconds"
        cleanup
        exit 1
    fi
}

# Check if we're in the right directory
if [ ! -f "pom.xml" ]; then
    print_message $RED "pom.xml not found. Please run this script from the notification service root directory."
    exit 1
fi

# Run main function
main "$@"