# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Development Commands

This is a Java 21 Spring Boot 3 project using Gradle as the build system.

### Core Commands

- `./gradlew build` - Build the project
- `./gradlew test` - Run all tests using JUnit Platform
- `./gradlew bootRun` - Run the Spring Boot application locally

### Docker Development

- `docker compose --env-file .env up -d` - Start all services (app, MySQL, Redis)
- Set up `.env` file from `.env.example` before running Docker

### Environment Setup

- Copy `.env.example` to `.env` and configure environment variables
- Requires MySQL, Redis, AWS S3, Firebase, and App Store configurations
- Application runs on configurable port (default 8080)
- Swagger UI available at `/swagger-ui.html` when enabled

### Deployment (CI/CD)

This project uses GitHub Actions for automated deployment with zero-downtime Blue-Green strategy.

#### Development Server (dev-api.deepple.co.kr)

- **Trigger**: Push to `develop` branch (CI 성공 후 자동)
- **Workflow**: `.github/workflows/cd-dev.yml`
- **Strategy**: Blue-Green deployment with nginx (zero downtime)
- **Process**:
    1. Build Docker image and push to AWS ECR
    2. Deploy to inactive slot (Blue:8081 or Green:8082)
    3. Health check new container (100 retries × 3초)
    4. Switch nginx proxy_pass to new port
    5. Graceful nginx reload (no dropped connections)
    6. Remove old container
- **Rollback**: Automatic on health check failure - old container keeps running
- **Monitoring**: GitHub Actions logs at `https://github.com/deepple-dev/deepple-api/actions`

#### Production Server

- **Trigger**: Manual (workflow_dispatch)
- **Workflow**: `.github/workflows/cd-prod.yml`
- **Strategy**: ECS Rolling Update
- **Process**:
    1. Build Docker image and push to AWS ECR
    2. Update ECS Task Definition with new image
    3. ECS performs rolling update
    4. Wait for service stability
- **Rollback**: ECS automatic rollback on unhealthy tasks, or manually deploy previous commit SHA

#### Manual Deployment

```bash
# Development: Push to develop branch triggers CI → CD automatically
git push origin develop

# Production: Manual trigger from GitHub Actions UI
# 1. Go to Actions → CD - Production
# 2. Click "Run workflow"
# 3. Select branch and optionally enter commit SHA or tag
# 4. Click "Run workflow"

# Check deployment status
gh run list --workflow=cd-dev.yml
gh run list --workflow=cd-prod.yml
gh run view <run-id> --log
```

## Architecture Overview

This is a dating/matching service backend built with Domain-Driven Design (DDD) principles and code-level CQRS.

### Domain Structure

The codebase is organized by bounded contexts (subdomains):

- `member` - User profiles, authentication, introductions
- `match` - Matching logic and communication
- `heart` - In-app currency system and transactions
- `payment` - In-app purchases via App Store
- `notification` - Push notifications via Firebase
- `admin` - Administrative functions and screening
- `community` - Self-introductions and profile exchanges
- `interview` - User questionnaires and answers
- `like` - Like/dislike functionality
- `mission` - User missions and rewards
- `block` - User blocking functionality
- `report` - User reporting system
- `datingexam` - Dating compatibility tests

### Package Structure (DDD + CQRS)

Each domain follows this structure:

```
domain_name/
├── presentation/     # API controllers and DTOs
├── command/          # Write operations
│   ├── application/  # Application services
│   ├── domain/       # Domain entities and business logic
│   └── infra/        # Infrastructure (JPA repositories, external APIs)
└── query/            # Read operations (separate from commands)
```

### Key Technologies

- **Persistence**: JPA with QueryDSL, MySQL with Flyway migrations
- **Security**: JWT tokens, Spring Security
- **Caching**: Redis with Redisson
- **External Integrations**: AWS S3, Firebase, Apple App Store Server API
- **Architecture Patterns**: Domain Events, Local Events, CQRS separation

### Event System

The application uses a local event system (`Events.java`) for domain event handling. Events are published within the
same process and handled by event handlers in the infrastructure layer.

### Testing Structure

- Unit tests for domain objects and value objects
- Integration tests for repositories using `@DataJpaTest`
- Service tests using mocks and test slices
- Tests follow the same package structure as main code

## Important Notes

- This is a Korean dating service ("연애의 모든 것") with Korean language in comments and some naming
- Uses Java 21 language features
- Planned migration to MSA (microservices) architecture
- Domain entities use Lombok for reducing boilerplate
- Heavy use of value objects for domain modeling
- All external API calls are abstracted through domain interfaces