# 🐳 SplitBuddy Docker Setup

Quick start guide for running SplitBuddy with Docker.

## 🚀 Quick Start (5 minutes)

### 1. Prerequisites
- [Docker Desktop](https://www.docker.com/products/docker-desktop/) installed and running
- Git (to clone the repository)

### 2. Run Everything
```bash
# Clone and navigate to the project
git clone <your-repo>
cd splitbuddy-backend

# Start all services (database + backend)
docker-compose up -d

# Check if everything is running
docker-compose ps
```

### 3. Test Your Setup
```bash
# Health check
curl http://localhost:4321/actuator/health

# Or open in browser: http://localhost:4321/actuator/health
```

## 📋 What Gets Created

- **PostgreSQL Database** on port 5432
- **SplitBuddy Backend** on port 4321
- **Persistent data storage** for your database
- **Isolated network** for services

## 🛠️ Development Commands

```bash
# Start services
docker-compose up -d

# View logs
docker-compose logs -f

# Stop services
docker-compose down

# Rebuild after code changes
docker-compose up -d --build

# Access database
docker exec -it splitbuddy-postgres psql -U postgres -d splitbuddy_db
```

## 🔧 Production Deployment

### 1. Set up environment
```bash
cp env.example .env
# Edit .env with your production values
```

### 2. Deploy
```bash
# Make scripts executable (Linux/Mac)
chmod +x docker-scripts/deploy-prod.sh
./docker-scripts/deploy-prod.sh

# Or on Windows PowerShell
.\docker-scripts\deploy-prod.ps1
```

## 📁 Key Files

- `Dockerfile` - Builds your Spring Boot app
- `docker-compose.yml` - Development environment
- `docker-compose.prod.yml` - Production environment
- `docker-scripts/` - Helper scripts
- `DOCKER_GUIDE.md` - Comprehensive guide

## 🆘 Troubleshooting

### Port already in use?
```bash
# Check what's using port 4321
netstat -tulpn | grep :4321
# Stop conflicting service or change port in docker-compose.yml
```

### Database connection failed?
```bash
# Check database logs
docker-compose logs postgres
# Ensure environment variables are correct
```

### Build fails?
```bash
# Clean Docker cache
docker system prune -a
# Rebuild
docker-compose up -d --build
```

## 🌟 Benefits of This Setup

✅ **No local Java/Maven installation needed**  
✅ **Consistent environment across team**  
✅ **Easy to deploy anywhere**  
✅ **Built-in health monitoring**  
✅ **Production-ready configuration**  
✅ **Automatic database setup**  

## 📚 More Information

- See `DOCKER_GUIDE.md` for detailed explanations
- Check [Docker documentation](https://docs.docker.com/)
- Review [Spring Boot Docker guide](https://spring.io/guides/gs/spring-boot-docker/)

---

**Ready to go? Run `docker-compose up -d` and start coding! 🚀**
