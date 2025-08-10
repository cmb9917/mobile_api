# Mobile API

A Kotlin-based Spring Boot API service with comprehensive observability using Micrometer metrics and OpenTelemetry tracing, designed for cloud-native deployment.

## Features

- **📊 Micrometer Metrics**: Application and business metrics collection using Micrometer registry
- **🔍 OpenTelemetry Tracing**: Distributed tracing with automatic trace correlation
- **📤 Unified Export**: Both metrics and traces exported via OpenTelemetry collector
- **🛡️ Resilience4J**: Circuit breaker, retry, rate limiting, and timeout patterns for fault tolerance
- **📝 API Documentation**: Interactive Swagger UI with OpenAPI 3.x specification
- **🧪 Testing**: Unit tests with 60% coverage requirement
- **🐳 Cloud Ready**: Docker containerization with sidecar architecture
- **📋 Structured Logging**: JSON ◊logging with trace ID integration
- **🚀 Production Ready**: Health checks, actuator endpoints, and observability

## API Overview

The API provides basic hello world endpoints for demonstration purposes. For complete API documentation with interactive testing, visit:

**📖 Swagger UI**: http://localhost:8080/api/swagger-ui/index.html

Key endpoints:
- `/api/hello` - Simple hello message
- `/api/hello_person?name=John` - Personalized greeting
- `/api/actuator/health` - Health check endpoint

## Quick Start

### Prerequisites

- Java 17+
- Docker & Docker Compose
- Gradle 8.5+

### Local Development

1. **Clone and build the project:**
   ```bash
   git clone <repository-url>
   cd mobile-api
   ./gradlew build
   ```

2. **Start the complete stack:**
   ```bash
   # Local development (default)
   docker-compose up -d
   
   # Or use environment-specific configurations
   docker-compose -f docker-compose.local.yml up -d    # Local
   docker-compose -f docker-compose.dev.yml up -d      # Development
   docker-compose -f docker-compose.staging.yml up -d  # Staging
   docker-compose -f docker-compose.prod.yml up -d     # Production
   ```
   
   This starts the OpenTelemetry collector sidecar and builds/runs the application automatically.

3. **Access the running application:**
   - **API**: http://localhost:8080/api
   - **Swagger UI**: http://localhost:8080/api/swagger-ui/index.html
   - **Metrics Endpoint**: http://localhost:8889/metrics (for external scraping)

4. **Test the API:**
   ```bash
   # Test hello endpoint
   curl http://localhost:8080/api/hello
   
   # Test personalized endpoint  
   curl "http://localhost:8080/api/hello_person?name=John"
   ```

## Production Deployment

### Docker Image

Build optimized production image:

```bash
docker build -t mobile-api:latest .
```

The image includes OpenTelemetry Java agent, non-root user execution, health checks, and memory-optimized JVM settings.

### Central Telemetry Collection Setup

For production deployments, configure a central external OpenTelemetry collector (such as AWS ADOT) to aggregate telemetry data from your application:

#### **Trace Data Flow**
Configure your application to **push** trace data directly to the central collector:
- Application → Central OTEL Collector → Trace Storage (AWS X-Ray, Jaeger, etc.)

#### **Metrics Data Flow** 
Configure the central collector to **pull** metrics from your application's local sidecar collector:
- Application → Local OTEL Collector (sidecar) → Central OTEL Collector → Metrics Storage (CloudWatch, Prometheus, etc.)

#### **Central Collector Configuration Example (AWS ADOT)**

```yaml
# Central ADOT collector configuration
receivers:
  # For traces: Receive directly from applications
  otlp:
    protocols:
      grpc:
        endpoint: 0.0.0.0:4317
      http:
        endpoint: 0.0.0.0:4318
  
  # For metrics: Scrape from application sidecar collectors
  prometheus:
    config:
      scrape_configs:
        - job_name: 'mobile-api'
          static_configs:
            - targets: ['mobile-api-sidecar:8889']

exporters:
  awsxray:
    region: us-west-2
  awsemf:
    region: us-west-2
    namespace: "MobileAPI/Metrics"

service:
  pipelines:
    traces:
      receivers: [otlp]
      exporters: [awsxray]
    metrics:
      receivers: [prometheus, otlp]
      exporters: [awsemf]
```

#### **Application Configuration**
Update your application's OTEL endpoint to point to the central collector for traces:
```bash
export OTEL_EXPORTER_OTLP_ENDPOINT=http://central-adot-collector:4318
```

### Environment Setup

Environment-specific configuration:

```bash
# Local Development
export SPRING_PROFILES_ACTIVE=local
export OTEL_EXPORTER_OTLP_ENDPOINT=http://localhost:4318

# Development Server
export SPRING_PROFILES_ACTIVE=dev
export OTEL_EXPORTER_OTLP_ENDPOINT=http://dev-otel-collector:4318

# Staging Environment
export SPRING_PROFILES_ACTIVE=staging
export OTEL_EXPORTER_OTLP_ENDPOINT=http://staging-otel-collector:4318

# Production Environment
export SPRING_PROFILES_ACTIVE=prod
export OTEL_EXPORTER_OTLP_ENDPOINT=http://prod-otel-collector:4318
export LOG_LEVEL=INFO
```

### Health Checks

- Application: `/api/actuator/health`
- Container health check included
- Startup/liveness/readiness probes configured

## Configuration

### Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `SPRING_PROFILES_ACTIVE` | `local` | Active Spring profile |
| `SERVER_PORT` | `8080` | API server port |
| `OTEL_EXPORTER_OTLP_ENDPOINT` | `http://localhost:4318` | OpenTelemetry collector endpoint |
| `LOG_LEVEL` | `INFO` | Application log level |

### Environment Profiles

| Profile | Purpose | Logging | Trace Sampling | Metrics Reporting |
|---------|---------|---------|----------------|-------------------|
| **local** | Local development | DEBUG | 100% | 5s intervals |
| **dev** | Development server | DEBUG | 50% | 10s intervals |
| **staging** | Pre-production testing | INFO | 10% | 15s intervals |
| **prod** | Production deployment | INFO | 1% | 30s intervals |
| **test** | Unit testing | N/A | Disabled | Simple registry |

## Testing

Run all tests with coverage:

```bash
./gradlew test jacocoTestReport
```

The build enforces 60% code coverage. View reports at `build/reports/jacoco/test/html/index.html`

## Running Different Environments

### Local Development
```bash
# Using Gradle
./gradlew bootRun --args='--spring.profiles.active=local'

# Using Docker
docker-compose -f docker-compose.local.yml up -d
```

### Development Environment
```bash
# Using Docker
docker-compose -f docker-compose.dev.yml up -d

# Using environment variables
SPRING_PROFILES_ACTIVE=dev ./gradlew bootRun
```

### Staging Environment
```bash
# Using Docker
docker-compose -f docker-compose.staging.yml up -d

# Using environment variables
SPRING_PROFILES_ACTIVE=staging ./gradlew bootRun
```

### Production Environment
```bash
# Using Docker
docker-compose -f docker-compose.prod.yml up -d

# Using environment variables
SPRING_PROFILES_ACTIVE=prod ./gradlew bootRun
```

## Security

### Dependency Scanning

Security vulnerabilities are scanned using OWASP Dependency Check:

```bash
./gradlew dependencyCheckAnalyze
```

## Resilience

### Resilience Patterns with Resilience4J

**Resilience4J** is a lightweight fault tolerance library designed for functional programming, inspired by Netflix Hystrix but built for Java 8 and functional programming. We use Resilience4J to implement several key resilience patterns that make our application more robust in distributed environments:

#### **Why Resilience4J?**
- **Fault Tolerance**: Prevents cascading failures when downstream services are unavailable
- **Performance Protection**: Rate limiting and timeouts prevent resource exhaustion
- **Observability**: Built-in metrics integration provides visibility into failure patterns
- **Configuration**: Fine-tuned control over failure thresholds and recovery behavior

#### **Implemented Patterns**
| Pattern | Purpose | Configuration | Endpoints |
|---------|---------|---------------|-----------|
| **Circuit Breaker** | Stops calls to failing services, allows recovery | 40-60% failure threshold, 30-60s wait | All services |
| **Retry** | Handles transient failures with backoff | 2-4 attempts, exponential backoff | All services |  
| **Rate Limiter** | Prevents overloading with request throttling | 30-50 requests/minute | `/hello`, `/hello_person` |
| **Time Limiter** | Prevents hanging requests with timeouts | 3s timeout | `/external` |
| **Bulkhead** | Isolates critical resources | Max 10 concurrent calls | `/external` |

#### **Resilience4J Metrics**
All resilience patterns expose metrics through Micrometer:
- Circuit breaker state and failure rates
- Retry attempt counts and success rates  
- Rate limiter permit acquisition and rejection rates
- Time limiter timeout and completion rates

These metrics are automatically exported to the OpenTelemetry collector alongside application metrics.

## Observability

### Metrics Collection with Micrometer

**Micrometer** is a dimensional metrics facade that provides a vendor-neutral interface for collecting application metrics. It acts as an abstraction layer that allows you to instrument your application code once and export metrics to multiple monitoring systems.

#### **Why Micrometer?**
- **Vendor Neutral**: Switch between monitoring systems (Prometheus, CloudWatch, etc.) without changing application code
- **Dimensional Metrics**: Rich tagging support for detailed metric filtering and aggregation
- **Spring Integration**: Native Spring Boot integration with auto-configuration
- **Comprehensive Coverage**: Automatic JVM, HTTP, and framework metrics plus custom business metrics

#### **How We Use Micrometer**
In our application, Micrometer serves as the central metrics collection system:

1. **Custom Business Metrics**: Created via `MetricsService` using Micrometer APIs
   - Counters for API requests and errors
   - Gauges for active users and system state
   - Timers for operation durations

2. **Automatic Framework Metrics**: Spring Boot provides out-of-the-box metrics
   - HTTP server requests with status codes and response times  
   - JVM metrics (memory, GC, threads)
   - System metrics (CPU, disk usage)

3. **Resilience4J Integration**: Fault tolerance metrics automatically registered
   - Circuit breaker states and failure rates
   - Retry attempt statistics
   - Rate limiter permit usage

4. **Export Pipeline**: All metrics flow through a unified pipeline
   - Micrometer → Spring Boot OTLP → OpenTelemetry Collector → External Systems

### Metrics Overview

Comprehensive metrics collection including:

#### **Automatic Metrics:**
- HTTP request counts by endpoint (`api.requests.total`)
- Response times and percentiles (`api.request.duration`) 
- Error counts by status code (`api.errors.total`)
- JVM metrics (memory, garbage collection, threads)
- System metrics (CPU usage, connection pools)

#### **Custom Business Metrics:**
- Active users gauge (`business.active.users`)
- Custom operation timing (`operation.duration`)
- Custom counters and gauges via MetricsService

### Tracing

Distributed tracing instrumentation for:
- HTTP requests and responses
- Database operations (when added)
- Custom business logic spans
- Automatic trace correlation with logs

### Logging

- Clean, readable logging format
- OpenTelemetry trace ID integration
- Configurable log levels
- Error stack traces captured
- Structured logging with trace correlation
- Docker logs: `docker logs mobile-api-app`
- Test logs: `build/reports/tests/`

### Dashboards

Access monitoring:
- **Metrics Endpoint**: http://localhost:8889/metrics - OpenTelemetry metrics for external scraping
- **External Prometheus**: Configure your Prometheus to scrape the metrics endpoint
- **Grafana**: Access through your external monitoring infrastructure
- **Traces**: Available in OTEL collector logs for debugging

