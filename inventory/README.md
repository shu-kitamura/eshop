# Inventory Management Service

Product catalog and inventory management for Ski Shop E-commerce Platform

## Main Features Provided

### Feature List

- **Product Catalog Management**: Comprehensive product information with attributes and categorization
- **Inventory Tracking**: Real-time inventory levels, reservations, and stock movements
- **Category Management**: Hierarchical product categorization system
- **Price Management**: Regular and promotional pricing with history tracking
- **Stock Level Monitoring**: Automatic low stock alerts and inventory analytics
- **Product Search**: Advanced product search with filtering and sorting
- **Image Management**: Product image storage and thumbnail generation
- **Supplier Management**: Supplier information and product sourcing
- **Real-time Event Publishing**: Inventory and product update events for other services

## Service Endpoints

| HTTP Method | Endpoint | Description | Authorization |
|-------------|----------|-------------|---------------|
| GET | `/api/products` | Get product list with pagination | Public |
| GET | `/api/products/{id}` | Get product details by ID | Public |
| GET | `/api/products/sku/{sku}` | Get product by SKU | Public |
| GET | `/api/products/search` | Search products by keyword | Public |
| GET | `/api/products/category/{categoryId}` | Get products by category | Public |
| POST | `/api/products` | Create new product | Admin |
| PUT | `/api/products/{id}` | Update product | Admin |
| POST | `/api/products/batch` | Get multiple products by IDs | Public |
| GET | `/api/categories` | Get category list | Public |
| GET | `/api/categories/{id}` | Get category details | Public |
| GET | `/api/categories/{id}/products` | Get products in category | Public |
| POST | `/api/categories` | Create new category | Admin |
| PUT | `/api/categories/{id}` | Update category | Admin |
| DELETE | `/api/categories/{id}` | Delete category | Admin |
| GET | `/api/inventory/{productId}` | Get product inventory | Public |
| GET | `/api/inventory/status/{productId}` | Get inventory status | Public |
| POST | `/api/inventory/batch` | Get multiple inventories | Public |
| POST | `/api/inventory/reserve` | Reserve inventory | Authenticated |
| POST | `/api/inventory/release` | Release reservation | Authenticated |
| POST | `/api/inventory/stock-in` | Process stock in | Admin |
| POST | `/api/inventory/stock-out` | Process stock out | Admin |
| GET | `/api/inventory/low-stock` | Get low stock products | Admin |
| GET | `/api/prices/{productId}` | Get product pricing | Public |
| POST | `/api/prices` | Create product price | Admin |
| PUT | `/api/prices/{productId}` | Update product price | Admin |
| GET | `/api/prices/history/{productId}` | Get price history | Admin |

## Technology Stack

- **Java**: 21 LTS
- **Spring Boot**: 3.5.3
- **Spring Data MongoDB**: Product and category data storage
- **Spring Data JPA**: Inventory and pricing data storage
- **Spring Security**: OAuth2 resource server
- **Spring Cloud Stream**: Kafka event integration
- **MongoDB**: Product catalog and category data
- **PostgreSQL**: Inventory, pricing, and supplier data
- **Redis**: Caching for product and inventory data
- **Kafka**: Event streaming platform
- **Azure Blob Storage**: Product image storage
- **Thumbnailator**: Processing images and thumbnail generation
- **MapStruct**: Object mapping
- **Flyway**: Database migrations
- **Containerization**: Docker
- **Cloud Platform**: Azure Container Apps

## Environment Variables

| Variable Name | Description | Default Value |
|---------------|-------------|---------------|
| `SPRING_PROFILES_ACTIVE` | Active Spring profile | `local` |
| `MONGODB_URI` | MongoDB connection string | `mongodb://localhost:27017/skishop_inventory` |
| `POSTGRES_URL` | PostgreSQL connection URL | `jdbc:postgresql://localhost:5432/skishop_inventory` |
| `POSTGRES_USERNAME` | PostgreSQL username | `inventory_user` |
| `POSTGRES_PASSWORD` | PostgreSQL password | `inventory_password` |
| `REDIS_HOST` | Redis host | `localhost` |
| `REDIS_PORT` | Redis port | `6379` |
| `KAFKA_BROKERS` | Kafka broker addresses | `localhost:9092` |
| `AZURE_STORAGE_ACCOUNT` | Azure Storage account name | - |
| `AZURE_STORAGE_KEY` | Azure Storage access key | - |
| `AZURE_STORAGE_CONTAINER` | Azure Storage container name | `product-images` |
| `JWT_ISSUER_URI` | JWT issuer URI for authentication | `http://localhost:8080/realms/skishop` |
| `CACHE_TTL_PRODUCTS` | Cache TTL for products (seconds) | `300` |
| `CACHE_TTL_CATEGORIES` | Cache TTL for categories (seconds) | `1800` |
| `CACHE_TTL_INVENTORY` | Cache TTL for inventory (seconds) | `60` |
| `LOG_LEVEL` | Application log level | `INFO` |
| `LOW_STOCK_THRESHOLD` | Default low stock alert threshold | `5` |

### Profiles

- **local**: Local development environment (local MongoDB, PostgreSQL, Redis)
- **dev**: Development environment (shared development resources)
- **test**: Testing environment (test databases and Testcontainers)
- **prod**: Production environment (Azure services and full security)

Profile settings are configured in `application.yml` and `application-{profile}.yml` files.

## Local Development Environment Setup and Verification

### Local Compilation and Execution

1. **Install Dependencies**

```bash
cd inventory-management-service
mvn clean install
```

1. **Configure Environment Variables**

Create a `.env` file or set environment variables:

```bash
export MONGODB_URI="mongodb://localhost:27017/skishop_inventory"
export POSTGRES_URL="jdbc:postgresql://localhost:5432/skishop_inventory"
export POSTGRES_USERNAME="inventory_user"
export POSTGRES_PASSWORD="inventory_password"
export KAFKA_BROKERS="localhost:9092"
```

1. **Start Required Services**

Start MongoDB, PostgreSQL, Redis, and Kafka:

```bash
# MongoDB
docker run -d --name inventory-mongodb -p 27017:27017 mongo:latest

# PostgreSQL
docker run -d --name inventory-postgres -p 5432:5432 \
  -e POSTGRES_DB=skishop_inventory \
  -e POSTGRES_USER=inventory_user \
  -e POSTGRES_PASSWORD=inventory_password \
  postgres:15-alpine

# Redis
docker run -d --name inventory-redis -p 6379:6379 redis:latest

# Kafka
docker run -d --name inventory-kafka -p 9092:9092 \
  -e KAFKA_ADVERTISED_LISTENERS=PLAINTEXT://localhost:9092 \
  -e KAFKA_LISTENERS=PLAINTEXT://0.0.0.0:9092 \
  confluentinc/cp-kafka:latest
```

1. **Start Application**

```bash
mvn spring-boot:run
```

1. **Access via Browser**

- **Swagger UI**: [http://localhost:8082/swagger-ui.html](http://localhost:8082/swagger-ui.html)
- **API Documentation**: [http://localhost:8082/v3/api-docs](http://localhost:8082/v3/api-docs)
- **Health Check**: [http://localhost:8082/actuator/health](http://localhost:8082/actuator/health)

### Run Microservices with Docker Compose

1. **Build and Start Containers**

```bash
docker-compose up --build
```

1. **Access via Browser**

- **Inventory Management Service**: [http://localhost:8082](http://localhost:8082)
- **Swagger UI**: [http://localhost:8082/swagger-ui.html](http://localhost:8082/swagger-ui.html)

## Production Environment Setup and Verification

Primary deployment target: Azure Container Apps
Related external resources: Azure Cosmos DB (MongoDB API), Azure Database for PostgreSQL, Azure Redis Cache, Azure Event Hubs (Kafka), Azure Blob Storage

### 1. Setup Related External Resources

**Azure Cosmos DB (MongoDB API)**:

```bash
# Create Azure Cosmos DB account with MongoDB API
az cosmosdb create \
  --name "ski-shop-inventory-cosmos" \
  --resource-group "rg-ski-shop" \
  --kind "MongoDB" \
  --capabilities "EnableMongo" \
  --default-consistency-level "Session" \
  --locations "East US=0"

# Create database and collections
az cosmosdb mongodb database create \
  --account-name "ski-shop-inventory-cosmos" \
  --resource-group "rg-ski-shop" \
  --name "skishop_inventory"

az cosmosdb mongodb collection create \
  --account-name "ski-shop-inventory-cosmos" \
  --resource-group "rg-ski-shop" \
  --database-name "skishop_inventory" \
  --name "products" \
  --shard "sku"
```

**Azure Database for PostgreSQL**:

```bash
# Create Azure PostgreSQL
az postgres flexible-server create \
  --name "ski-shop-inventory-db" \
  --resource-group "rg-ski-shop" \
  --location "East US" \
  --admin-user "inventory_admin" \
  --admin-password "YourSecurePassword123!" \
  --sku-name "Standard_D2s_v3" \
  --tier "GeneralPurpose" \
  --storage-size 128 \
  --version "15"

# Configure firewall
az postgres flexible-server firewall-rule create \
  --name "AllowAllAzureServices" \
  --resource-group "rg-ski-shop" \
  --server-name "ski-shop-inventory-db" \
  --start-ip-address "0.0.0.0" \
  --end-ip-address "0.0.0.0"
```

**Azure Blob Storage**:

```bash
# Create storage account
az storage account create \
  --name "skishopinventorystore" \
  --resource-group "rg-ski-shop" \
  --location "East US" \
  --sku "Standard_LRS" \
  --kind "StorageV2"

# Create container
az storage container create \
  --name "product-images" \
  --account-name "skishopinventorystore" \
  --public-access "blob"
```

### 2. Azure Container Apps Environment Setup and Deployment

```bash
# Set environment variables
export AZURE_SUBSCRIPTION_ID="your-subscription-id"
export AZURE_LOCATION="eastus"
export AZURE_ENV_NAME="inventory-prod"

# Initialize Azure Developer CLI
azd init --template inventory-management-service

# Set secrets
azd env set POSTGRES_PASSWORD "YourSecurePassword123!" --secret
azd env set MONGODB_URI "mongodb://ski-shop-inventory-cosmos:${COSMOSDB_KEY}@ski-shop-inventory-cosmos.mongo.cosmos.azure.com:10255/skishop_inventory?ssl=true&replicaSet=globaldb" --secret
azd env set AZURE_STORAGE_KEY "your-azure-storage-key" --secret

# Deploy infrastructure and application
azd up
```

### 3. Access Azure Container Apps Instance via Browser

After deployment, access the service at the provided Azure Container Apps URL:

- **Service URL**: [https://inventory-prod.internal.azurecontainerapps.io](https://inventory-prod.internal.azurecontainerapps.io)
- **Health Check**: [https://inventory-prod.internal.azurecontainerapps.io/actuator/health](https://inventory-prod.internal.azurecontainerapps.io/actuator/health)

### 4. Azure CLI Environment Setup Script

```bash
#!/bin/bash
# setup-azure-environment.sh

# Variables
RESOURCE_GROUP="rg-ski-shop-inventory"
LOCATION="eastus"
APP_NAME="inventory-management-service"
CONTAINER_REGISTRY="acrskishop"
COSMOS_ACCOUNT_NAME="ski-shop-inventory-cosmos"
POSTGRES_SERVER_NAME="ski-shop-inventory-db"
STORAGE_ACCOUNT_NAME="skishopinventorystore"

# Create resource group
az group create --name $RESOURCE_GROUP --location $LOCATION

# Create Container Registry
az acr create \
  --name $CONTAINER_REGISTRY \
  --resource-group $RESOURCE_GROUP \
  --sku "Standard" \
  --location $LOCATION

# Create Container Apps Environment
az containerapp env create \
  --name "env-ski-shop" \
  --resource-group $RESOURCE_GROUP \
  --location $LOCATION

# Deploy application
az containerapp create \
  --name $APP_NAME \
  --resource-group $RESOURCE_GROUP \
  --environment "env-ski-shop" \
  --image "$CONTAINER_REGISTRY.azurecr.io/inventory-management-service:latest" \
  --target-port 8082 \
  --ingress 'external' \
  --env-vars \
    SPRING_PROFILES_ACTIVE="prod" \
    POSTGRES_URL="jdbc:postgresql://$POSTGRES_SERVER_NAME.postgres.database.azure.com:5432/skishop_inventory" \
    POSTGRES_USERNAME="inventory_admin" \
    AZURE_STORAGE_ACCOUNT="$STORAGE_ACCOUNT_NAME" \
    AZURE_STORAGE_CONTAINER="product-images"

echo "Deployment completed. Check the Azure portal for the application URL."
```

## Service Feature Verification Methods

### curl Commands for Testing and Expected Results

### 1. Health Check

```bash
curl -X GET http://localhost:8082/actuator/health

# Expected Response:
{
  "status": "UP",
  "components": {
    "diskSpace": {"status": "UP"},
    "mongo": {"status": "UP"},
    "db": {"status": "UP"},
    "redis": {"status": "UP"},
    "kafka": {"status": "UP"}
  }
}
```

### 2. Get Products

```bash
curl -X GET "http://localhost:8082/api/products?page=0&size=10"

# Expected Response:
{
  "content": [
    {
      "id": "61f7c8a53e5c74a9a2f22b8b",
      "sku": "SKI-ATOMIC-001",
      "name": "Atomic Bent 100 Ski",
      "description": "The Atomic Bent 100 is a versatile all-mountain ski...",
      "brand": "Atomic",
      "price": {
        "regularPrice": 65000,
        "salePrice": 58500,
        "currencyCode": "JPY",
        "onSale": true
      },
      "inventory": {
        "status": "IN_STOCK",
        "quantity": 15
      },
      "category": {
        "id": "61f7c8a53e5c74a9a2f22b01",
        "name": "Skis"
      },
      "imageUrl": "https://storage.skieshop.com/products/atomic-bent-100-main.jpg"
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 10,
    "sort": {
      "sorted": true,
      "unsorted": false
    }
  },
  "totalElements": 42,
  "totalPages": 5,
  "last": false,
  "first": true,
  "size": 10,
  "number": 0
}
```

### 3. Get Inventory Status

```bash
curl -X GET "http://localhost:8082/api/inventory/status/61f7c8a53e5c74a9a2f22b8b"

# Expected Response:
{
  "productId": "61f7c8a53e5c74a9a2f22b8b",
  "sku": "SKI-ATOMIC-001",
  "status": "IN_STOCK",
  "quantity": 15,
  "reservedQuantity": 2,
  "availableQuantity": 13,
  "lowStockThreshold": 5,
  "isLowStock": false,
  "lastUpdated": "2025-07-04T10:15:30Z"
}
```

### 4. Search Products

```bash
curl -X GET "http://localhost:8082/api/products/search?keyword=atomic&category=ski&page=0&size=10"

# Expected Response (similar to Get Products with filtered results)
{
  "content": [
    {
      "id": "61f7c8a53e5c74a9a2f22b8b",
      "sku": "SKI-ATOMIC-001",
      "name": "Atomic Bent 100 Ski",
      // ... other fields ...
    }
  ],
  // ... pagination info ...
}
```

## Integration with Other Microservices

This service integrates with the following microservices:

- **API Gateway**: Routing and authentication for inventory requests
- **Sales Management Service**: Inventory reservations for orders
- **Payment Cart Service**: Product information for cart display
- **AI Support Service**: Product data for recommendations
- **Point Service**: Product data for point calculations
- **Coupon Service**: Product eligibility for promotions
- **Frontend Service**: Product catalog browsing interface

## Testing

### Unit Test Execution

```bash
# Run unit tests
mvn test

# Run tests with coverage
mvn test jacoco:report

# View coverage report
open target/site/jacoco/index.html
```

### Integration Test Execution

```bash
# Run integration tests
mvn test -Dtest="*IT"

# Run all tests including integration tests
mvn verify

# Run tests with Testcontainers
mvn test -Dspring.profiles.active=test
```

## Monitoring & Logging

### Health Check

```url
GET /actuator/health
```

Response includes MongoDB, PostgreSQL, Redis, and Kafka connectivity status.

### Metrics

```url
GET /actuator/metrics
GET /actuator/prometheus
```

Available metrics:

- Product operation counts (views, searches, updates)
- Inventory transaction volume
- Cache hit/miss ratio
- Database operation latency
- Event publishing statistics

### Log Levels

- **Development environment**: DEBUG
- **Production environment**: INFO

Key loggers:

- `com.skishop.inventory`: Main application logger
- `org.springframework.data.mongodb`: MongoDB operations
- `org.springframework.data.jpa`: JPA operations
- `org.springframework.cache`: Cache operations
- `org.springframework.kafka`: Kafka operations

## Troubleshooting

### Common Issues

#### 1. MongoDB Connection Failure

```text
ERROR: Unable to connect to MongoDB
Solution: Check MONGODB_URI environment variable and ensure MongoDB is running
```

#### 2. PostgreSQL Connection Issues

```text
ERROR: Unable to connect to PostgreSQL
Solution: Verify POSTGRES_URL, POSTGRES_USERNAME, and POSTGRES_PASSWORD
```

#### 3. Azure Blob Storage Issues

```text
ERROR: Failed to upload image to Azure Blob Storage
Solution: Check AZURE_STORAGE_ACCOUNT, AZURE_STORAGE_KEY, and AZURE_STORAGE_CONTAINER
```

#### 4. Kafka Connection Problems

```text
ERROR: Could not connect to Kafka broker
Solution: Verify KAFKA_BROKERS environment variable and Kafka service availability
```

#### 5. Cache Inconsistency

```text
WARN: Cache inconsistency detected
Solution: Manually invalidate cache with Redis CLI or through API endpoint
```

## Developer Information

### Directory Structure

```text
inventory-management-service/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/skishop/inventory/
│   │   │       ├── controller/          # REST API controllers
│   │   │       │   ├── ProductController.java
│   │   │       │   ├── CategoryController.java
│   │   │       │   ├── InventoryController.java
│   │   │       │   └── PriceController.java
│   │   │       ├── service/             # Business logic services
│   │   │       │   ├── ProductService.java
│   │   │       │   ├── CategoryService.java
│   │   │       │   ├── InventoryService.java
│   │   │       │   ├── PriceService.java
│   │   │       │   ├── ImageService.java
│   │   │       │   └── EventPublisherService.java
│   │   │       ├── repository/          # Data repositories
│   │   │       │   ├── mongo/           # MongoDB repositories
│   │   │       │   │   ├── ProductRepository.java
│   │   │       │   │   └── CategoryRepository.java
│   │   │       │   └── jpa/             # JPA repositories
│   │   │       │       ├── InventoryRepository.java
│   │   │       │       ├── PriceRepository.java
│   │   │       │       └── ProductImageRepository.java
│   │   │       ├── entity/              # Entity classes
│   │   │       │   ├── mongo/           # MongoDB entities
│   │   │       │   │   ├── Product.java
│   │   │       │   │   └── Category.java
│   │   │       │   └── jpa/             # JPA entities
│   │   │       │       ├── Inventory.java
│   │   │       │       ├── Price.java
│   │   │       │       └── ProductImage.java
│   │   │       ├── dto/                 # Data transfer objects
│   │   │       │   ├── ProductDTO.java
│   │   │       │   ├── CategoryDTO.java
│   │   │       │   ├── InventoryDTO.java
│   │   │       │   ├── PriceDTO.java
│   │   │       │   └── request/         # Request DTOs
│   │   │       ├── mapper/              # MapStruct mappers
│   │   │       │   ├── ProductMapper.java
│   │   │       │   ├── CategoryMapper.java
│   │   │       │   └── PriceMapper.java
│   │   │       ├── config/              # Configuration classes
│   │   │       │   ├── MongoConfig.java
│   │   │       │   ├── JpaConfig.java
│   │   │       │   ├── CacheConfig.java
│   │   │       │   ├── KafkaConfig.java
│   │   │       │   ├── SecurityConfig.java
│   │   │       │   └── StorageConfig.java
│   │   │       ├── exception/           # Exception handling
│   │   │       │   ├── InventoryException.java
│   │   │       │   ├── ResourceNotFoundException.java
│   │   │       │   └── GlobalExceptionHandler.java
│   │   │       ├── event/               # Event classes
│   │   │       │   ├── ProductEvent.java
│   │   │       │   ├── InventoryEvent.java
│   │   │       │   └── PriceEvent.java
│   │   │       └── InventoryManagementServiceApplication.java
│   │   └── resources/
│   │       ├── db/migration/            # Flyway migration scripts
│   │       ├── application.yml          # Main configuration
│   │       ├── application-local.yml    # Local environment config
│   │       └── application-prod.yml     # Production environment config
│   └── test/
│       ├── java/                        # Unit and integration tests
│       └── resources/                   # Test resources
├── docker-compose.yml                   # Local development setup
├── Dockerfile                           # Container image definition
├── pom.xml                              # Maven configuration
└── README.md                            # This file
```

## Change History

- **v1.0.0** (2025-07-04): Initial release
  - Product catalog management with MongoDB
  - Inventory tracking with PostgreSQL
  - Category hierarchy management
  - Price management with history tracking
  - Azure Blob Storage integration for product images
  - Event-driven architecture with Kafka
  - Docker containerization and Azure deployment support
