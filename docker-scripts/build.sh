#!/bin/bash

# SplitBuddy Docker Build Script
echo "🚀 Building SplitBuddy Docker containers..."

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo "❌ Docker is not running. Please start Docker and try again."
    exit 1
fi

# Build the application
echo "📦 Building SplitBuddy backend..."
docker build -t splitbuddy-backend:latest .

if [ $? -eq 0 ]; then
    echo "✅ Backend build successful!"
else
    echo "❌ Backend build failed!"
    exit 1
fi

echo "🎉 All containers built successfully!"
echo ""
echo "To run the application:"
echo "  docker-compose up -d"
echo ""
echo "To view logs:"
echo "  docker-compose logs -f"
echo ""
echo "To stop the application:"
echo "  docker-compose down"
