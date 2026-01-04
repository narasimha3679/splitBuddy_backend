#!/bin/bash

# SplitBuddy Production Deployment Script
echo "🚀 Deploying SplitBuddy to Production..."

# Check if .env file exists
if [ ! -f .env ]; then
    echo "❌ .env file not found. Please create one with production environment variables."
    echo "Required variables:"
    echo "  POSTGRES_PASSWORD=<secure_password>"
    echo "  JWT_SECRET=<secure_jwt_secret>"
    echo "  POSTGRES_DB=<database_name>"
    echo "  POSTGRES_USER=<database_user>"
    exit 1
fi

# Load environment variables
source .env

# Check required variables
if [ -z "$POSTGRES_PASSWORD" ] || [ -z "$JWT_SECRET" ]; then
    echo "❌ Missing required environment variables. Please check your .env file."
    exit 1
fi

# Stop existing containers
echo "🛑 Stopping existing containers..."
docker-compose -f docker-compose.prod.yml down

# Remove old images
echo "🧹 Cleaning up old images..."
docker image prune -f

# Build and start production containers
echo "📦 Starting production containers..."
docker-compose -f docker-compose.prod.yml up -d --build

# Wait for services to be healthy
echo "⏳ Waiting for services to be healthy..."
sleep 30

# Check service health
echo "🏥 Checking service health..."
if docker-compose -f docker-compose.prod.yml ps | grep -q "Up"; then
    echo "✅ All services are running!"
    echo ""
    echo "🌐 Application is available at:"
    echo "  - Backend API: http://localhost:4321"
    echo "  - Health Check: http://localhost:4321/actuator/health"
    echo ""
    echo "📊 To view logs:"
    echo "  docker-compose -f docker-compose.prod.yml logs -f"
    echo ""
    echo "🛑 To stop:"
    echo "  docker-compose -f docker-compose.prod.yml down"
else
    echo "❌ Some services failed to start. Check logs:"
    echo "  docker-compose -f docker-compose.prod.yml logs"
    exit 1
fi
