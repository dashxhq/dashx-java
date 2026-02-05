# DashX Spring Boot Demo Application

A demo Spring Boot application showcasing how to use the DashX Java SDK with various endpoints for testing SDK functionality.

## Prerequisites

- Java 17 or higher
- Gradle 8.14 or higher
- DashX account with API credentials

## Setup

### 1. Configure Environment Variables

Copy the example environment file:

```bash
cp .env.example .env
```

Edit `.env` and add your DashX credentials:

```bash
DASHX_PUBLIC_KEY=your-public-key-here
DASHX_PRIVATE_KEY=your-private-key-here
DASHX_TARGET_ENVIRONMENT=your-environment-name
DASHX_BASE_URL=https://api.dashx.com/graphql
```

### 2. Load Environment Variables

```bash
export $(cat .env | xargs)
```

### 3. Run the Application

From the project root:

```bash
gradle dashx-demo-spring-boot:bootRun
```

Or from the demo directory:

```bash
cd dashx-demo-spring-boot
../gradlew bootRun
```

The application will start on `http://localhost:8081`

## Available Endpoints

### Welcome Endpoint

Test that the application is running:

```
GET http://localhost:8081/welcome
```

### 1. User Identification

Identify or create a user account.

**Endpoint:** `GET /identify`

**Parameters:**
- `uid` (optional) - User identifier
- `email` (optional) - User email
- `phone` (optional) - User phone number
- `name` (optional) - Full name
- `firstName` (optional) - First name
- `lastName` (optional) - Last name
- `anonymousUid` (optional) - Anonymous user identifier

**Example URLs:**

```bash
# Identify by email
http://localhost:8081/identify?email=test@example.com&firstName=John&lastName=Doe

# Identify by uid
http://localhost:8081/identify?uid=user123&name=John+Doe

# Identify with multiple fields
http://localhost:8081/identify?uid=user456&email=jane@example.com&phone=1234567890&firstName=Jane&lastName=Smith
```

### 2. Event Tracking

Track user events for analytics and workflows.

**Endpoint:** `GET /track`

**Parameters:**
- `event` (required) - Event name
- `uid` (optional) - User identifier

**Example URLs:**

```bash
# Track simple event
http://localhost:8081/track?event=page_viewed

# Track event for specific user
http://localhost:8081/track?event=button_clicked&uid=user123
```

**Endpoint:** `GET /track-with-data`

**Parameters:**
- `event` (required) - Event name
- `uid` (optional) - User identifier
- `dataKey` (optional) - Custom data key
- `dataValue` (optional) - Custom data value

**Example URLs:**

```bash
# Track event with custom data
http://localhost:8081/track-with-data?event=product_purchased&uid=user123&dataKey=productId&dataValue=prod_456

# Track event with multiple data points
http://localhost:8081/track-with-data?event=signup_completed&dataKey=source&dataValue=google
```

### 3. Asset Management

#### Get Single Asset

Retrieve a specific asset by ID.

**Endpoint:** `GET /get-asset`

**Parameters:**
- `id` (required) - Asset ID

**Example URL:**

```bash
http://localhost:8081/get-asset?id=asset_123
```

#### List All Assets

List all assets (no filters).

**Endpoint:** `GET /list-assets`

**Example URL:**

```bash
# List all assets
http://localhost:8081/list-assets

# List assets for a specific resource
http://localhost:8081/list-assets?resourceId=resource_123
```

#### List Assets with Filtering and Ordering

List assets with advanced filtering and sorting options.

**Endpoint:** `GET /list-assets-filtered`

**Parameters:**
- `resourceId` (optional) - Filter by resource ID
- `orderField` (optional) - Field to sort by (e.g., createdAt, name, size)
- `orderDirection` (optional) - Sort direction: `asc` or `desc` (default: desc)

**Example URLs:**

```bash
# List assets ordered by creation date
http://localhost:8081/list-assets-filtered?orderField=createdAt&orderDirection=desc

# List assets for a resource, ordered by name
http://localhost:8081/list-assets-filtered?resourceId=resource_123&orderField=name&orderDirection=asc

# List all assets ordered by size
http://localhost:8081/list-assets-filtered?orderField=size&orderDirection=desc
```

### 4. Content Records

Search and query content records from your DashX content management system.

**Endpoint:** `GET /search-records`

**Parameters:**
- `resource` (required) - Resource name (e.g., "posts", "products", "users")
- `limit` (optional) - Maximum number of results (default: no limit)
- `page` (optional) - Page number for pagination (default: 0)

**Example URLs:**

```bash
# Search all records for a resource
http://localhost:8081/search-records?resource=posts

# Search with pagination
http://localhost:8081/search-records?resource=products&limit=10&page=0

# Search second page
http://localhost:8081/search-records?resource=users&limit=25&page=1
```

### 5. Issue Management

Create and manage issues (tickets, tasks, bugs, etc.).

#### Create Issue

Create a new issue.

**Endpoint:** `GET /create-issue`

**Parameters:**
- `title` (required) - Issue title
- `issueType` (optional) - Type of issue (e.g., "bug", "feature", "task")
- `issueStatus` (optional) - Status (e.g., "open", "in-progress", "closed")

**Example URLs:**

```bash
# Create issue with type and status
http://localhost:8081/create-issue?title=Add+dark+mode&issueType=task

# Create bug report
http://localhost:8081/create-issue?title=Crash+on+startup&issueType=task&issueStatus=To+Do
```

#### Upsert Issue

Create a new issue or update an existing one (based on idempotency key).

**Endpoint:** `GET /upsert-issue`

**Parameters:**
- `title` (required) - Issue title
- `issueType` (optional) - Type of issue
- `issueStatus` (optional) - Status
- `idempotencyKey` (optional) - Unique key to prevent duplicates

**Example URLs:**

```bash
# Create new issue with idempotency key
http://localhost:8081/upsert-issue?title=Fix+payment+processing&idempotencyKey=payment-fix-2024

# Update existing issue (same idempotency key)
http://localhost:8081/upsert-issue?title=Fix+payment+processing+-+Updated&issueType=bug&idempotencyKey=payment-fix-2024

# Create without idempotency key
http://localhost:8081/upsert-issue?title=New+feature+request&issueType=feature
```

## Configuration

The application is configured via environment variables or `application.properties`:

### Required Configuration

```properties
dashx.public-key=${DASHX_PUBLIC_KEY}
dashx.private-key=${DASHX_PRIVATE_KEY}
dashx.target-environment=${DASHX_TARGET_ENVIRONMENT}
```

### Optional Configuration

```properties
# Base URL (default: https://api.dashx.com/graphql)
dashx.base-url=${DASHX_BASE_URL:https://api.dashx.com/graphql}

# Connection timeout in milliseconds (default: 10000)
dashx.connection-timeout=${DASHX_CONNECTION_TIMEOUT:10000}

# Response timeout in milliseconds (default: 30000)
dashx.response-timeout=${DASHX_RESPONSE_TIMEOUT:30000}

# Maximum connections (default: 500)
dashx.max-connections=${DASHX_MAX_CONNECTIONS:500}

# Max idle time in milliseconds (default: 20000)
dashx.max-idle-time=${DASHX_MAX_IDLE_TIME:20000}

# Server port (default: 8081)
server.port=8081
```

## Testing with cURL

You can also test the endpoints using cURL:

### Identify User

```bash
curl "http://localhost:8081/identify?email=test@example.com&firstName=John&lastName=Doe"
```

### Track Event

```bash
curl "http://localhost:8081/track?event=page_viewed&uid=user123"
```

### Track Event with Data

```bash
curl "http://localhost:8081/track-with-data?event=purchase&uid=user123&dataKey=amount&dataValue=99.99"
```

### Get Asset

```bash
curl "http://localhost:8081/get-asset?id=asset_abc123"
```

### List Assets

```bash
curl "http://localhost:8081/list-assets"
curl "http://localhost:8081/list-assets?resourceId=resource_123"
```

### List Assets with Ordering

```bash
curl "http://localhost:8081/list-assets-filtered?orderField=createdAt&orderDirection=desc"
```

### Search Records

```bash
curl "http://localhost:8081/search-records?resource=posts&limit=10&page=0"
```

### Create Issue

```bash
curl "http://localhost:8081/create-issue?title=Bug+in+login&issueType=bug&issueStatus=open"
```

### Upsert Issue

```bash
curl "http://localhost:8081/upsert-issue?title=Payment+fix&issueType=bug&idempotencyKey=payment-2024"
```

## Error Handling

The demo application uses CompletableFuture for all async operations. Errors are propagated as exceptions and will return HTTP 500 with error details.

Common errors:
- `DashXValidationException` - Invalid input (null, empty, etc.)
- `DashXGraphQLException` - GraphQL API errors
- `DashXConfigurationException` - Configuration errors

## Development

### Enable Debug Logging

Add to `application.properties`:

```properties
logging.level.com.dashx=DEBUG
logging.level.reactor.netty=DEBUG
```

### Hot Reload

The application supports Spring Boot DevTools for hot reloading during development.

## Troubleshooting

### Connection Issues

If you see "Connection prematurely closed" errors:

1. Check your credentials are correct
2. Verify the base URL is accessible
3. Try increasing timeouts:

```properties
dashx.connection-timeout=20000
dashx.response-timeout=60000
```

### Authentication Errors

Verify your API keys are correctly set:

```bash
echo $DASHX_PUBLIC_KEY
echo $DASHX_PRIVATE_KEY
```

### Port Already in Use

Change the port in `application.properties`:

```properties
server.port=8082
```

## Next Steps

1. Explore the [DashX Java SDK documentation](../README.md)
2. Check the [Spring Boot Starter documentation](../dashx-spring-boot-starter/README.md)

## License

This demo application is licensed under the MIT License - see the [LICENSE](../LICENSE) file for details.
