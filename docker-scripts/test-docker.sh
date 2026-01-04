#!/bin/bash

# SplitBuddy Docker Test Script
echo "🧪 Testing SplitBuddy Docker Setup..."

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo "❌ Docker is not running. Please start Docker and try again."
    exit 1
fi

# Check if docker-compose is available
if ! command -v docker-compose &> /dev/null; then
    echo "❌ docker-compose is not available. Please install Docker Compose."
    exit 1
fi

echo "✅ Docker and Docker Compose are available"

# Test building the image
echo "🔨 Testing Docker build..."
if docker build -t splitbuddy-test . > /dev/null 2>&1; then
    echo "✅ Docker build successful"
else
    echo "❌ Docker build failed"
    exit 1
fi

# Clean up test image
docker rmi splitbuddy-test > /dev/null 2>&1

# Test docker-compose syntax
echo "📋 Testing docker-compose configuration..."
if docker-compose config > /dev/null 2>&1; then
    echo "✅ docker-compose.yml is valid"
else
    echo "❌ docker-compose.yml has errors"
    exit 1
fi

if docker-compose -f docker-compose.prod.yml config > /dev/null 2>&1; then
    echo "✅ docker-compose.prod.yml is valid"
else
    echo "❌ docker-compose.prod.yml has errors"
    exit 1
fi

echo ""
echo "🎉 All Docker tests passed! Your setup is ready to use."
echo ""
echo "Next steps:"
echo "1. Run: docker-compose up -d"
echo "2. Check logs: docker-compose logs -f"
echo "3. Test API: curl http://localhost:4321/actuator/health"
