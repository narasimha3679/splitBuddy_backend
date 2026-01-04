# SplitBuddy Docker Guide

This guide explains how to use Docker with your SplitBuddy backend project for both development and production environments.

## 🐳 What is Docker and Why Use It?

Docker is a platform that packages your application and all its dependencies into standardized units called containers. Here's why it's beneficial for your SplitBuddy project:

### Benefits:
- **Consistency**: Same environment across development, testing, and production
- **Isolation**: Your app runs in its own container, separate from your system
- **Portability**: Easy to deploy anywhere Docker is available
- **Scalability**: Simple to scale up or down
- **Dependency Management**: No need to install Java, Maven, or PostgreSQL locally
- **Team Collaboration**: Everyone on your team gets the exact same setup

## 🚀 Quick Start

### Prerequisites
1. Install [Docker Desktop](https://www.docker.com/products/docker-desktop/)
2. Make sure Docker is running

### Development Environment

1. **Build and run the entire stack:**
   ```bash
   docker-compose up -d
   ```

2. **View logs:**
   ```bash
   docker-compose logs -f
   ```

3. **Stop services:**
   ```bash
   docker-compose down
   ```

4. **Rebuild after code changes:**
   ```bash
   docker-compose up -d --build
   ```

### Production Environment

1. **Create environment file:**
   ```bash
   cp env.example .env
   # Edit .env with your production values
   ```

2. **Deploy to production:**
   ```bash
   chmod +x docker-scripts/deploy-prod.sh
   ./docker-scripts/deploy-prod.sh
   ```

## 📁 File Structure

```
splitbuddy-backend/
├── Dockerfile                    # Multi-stage build for the app
├── docker-compose.yml           # Development environment
├── docker-compose.prod.yml      # Production environment
├── .dockerignore                # Files to exclude from Docker build
├── docker-scripts/              # Helper scripts
│   ├── build.sh                # Build script
│   └── deploy-prod.sh          # Production deployment
├── env.example                  # Environment variables template
└── src/main/resources/
    ├── application-docker.yml   # Docker-specific config
    └── application-production.yml # Production config
```

## 🔧 Configuration Files Explained

### Dockerfile
- **Multi-stage build**: Builds your app in a Maven container, then creates a lightweight runtime image
- **Security**: Runs as non-root user
- **Health checks**: Monitors application health
- **Optimized**: Uses Alpine Linux for smaller image size

### docker-compose.yml (Development)
- **PostgreSQL**: Database with persistent storage
- **SplitBuddy Backend**: Your Spring Boot application
- **Networking**: Isolated network for services
- **Health checks**: Ensures services are ready before starting dependent services

### docker-compose.prod.yml (Production)
- **Security**: Binds ports to localhost only
- **Performance**: Optimized database connection pooling
- **Monitoring**: Prometheus metrics and health endpoints
- **Nginx**: Optional reverse proxy for SSL termination

## 🌍 Environment Variables

### Development (.env file)
```bash
# Database
POSTGRES_DB=splitbuddy_db
POSTGRES_USER=postgres
POSTGRES_PASSWORD=postgres

# JWT
JWT_SECRET=your_secret_key
JWT_EXPIRATION=5184000000

# App
SERVER_PORT=4321
SPRING_PROFILES_ACTIVE=docker
```

### Production (.env file)
```bash
# Use strong, unique passwords and secrets
POSTGRES_PASSWORD=very_secure_password_123
JWT_SECRET=256_bit_random_secret_key_here
```

## 🚀 Deployment to Production

### 1. Prepare Your Server
- Install Docker and Docker Compose
- Set up firewall rules
- Configure SSL certificates (if using Nginx)

### 2. Deploy
```bash
# Copy your project to the server
git clone <your-repo>
cd splitbuddy-backend

# Set up environment
cp env.example .env
# Edit .env with production values

# Deploy
./docker-scripts/deploy-prod.sh
```

### 3. Monitor
```bash
# Check service status
docker-compose -f docker-compose.prod.yml ps

# View logs
docker-compose -f docker-compose.prod.yml logs -f

# Health check
curl http://localhost:4321/actuator/health
```

## 🔍 Troubleshooting

### Common Issues

1. **Port already in use:**
   ```bash
   # Check what's using the port
   netstat -tulpn | grep :4321
   # Stop conflicting service or change port in docker-compose.yml
   ```

2. **Database connection failed:**
   ```bash
   # Check if PostgreSQL is running
   docker-compose logs postgres
   # Ensure environment variables are correct
   ```

3. **Build fails:**
   ```bash
   # Clean Docker cache
   docker system prune -a
   # Rebuild
   docker-compose up -d --build
   ```

### Useful Commands

```bash
# View running containers
docker ps

# View container logs
docker logs <container_name>

# Execute commands in running container
docker exec -it <container_name> /bin/bash

# View resource usage
docker stats

# Clean up unused resources
docker system prune
```

## 📊 Monitoring and Health Checks

Your application includes:
- **Health endpoint**: `/actuator/health`
- **Metrics**: `/actuator/metrics`
- **Prometheus**: `/actuator/prometheus`

### Health Check Commands
```bash
# Application health
curl http://localhost:4321/actuator/health

# Database health
docker exec splitbuddy-postgres pg_isready -U postgres
```

## 🔒 Security Best Practices

1. **Never commit .env files** to version control
2. **Use strong passwords** for database and JWT secrets
3. **Bind ports to localhost** in production
4. **Run containers as non-root** users
5. **Regularly update** base images
6. **Scan images** for vulnerabilities

## 🚀 Scaling

### Horizontal Scaling
```bash
# Scale backend service
docker-compose up -d --scale splitbuddy-backend=3

# Use load balancer (Nginx) to distribute traffic
```

### Database Scaling
- Use connection pooling (already configured)
- Consider read replicas for heavy read workloads
- Implement database clustering for high availability

## 📚 Additional Resources

- [Docker Official Documentation](https://docs.docker.com/)
- [Docker Compose Documentation](https://docs.docker.com/compose/)
- [Spring Boot Docker Guide](https://spring.io/guides/gs/spring-boot-docker/)
- [PostgreSQL Docker Image](https://hub.docker.com/_/postgres)

## 🆘 Getting Help

If you encounter issues:
1. Check the logs: `docker-compose logs`
2. Verify environment variables
3. Ensure Docker is running
4. Check port availability
5. Review this guide for common solutions

---

**Happy Containerizing! 🐳✨**
