# Nova Hotel API Test Cases Documentation

## Environment Setup

```json
{
  "baseUrl": "http://localhost:8080/api",
  "userEmail": "",
  "username": "",
  "userPassword": "Test@123",
  "adminEmail": "admin@novahotel.com",
  "adminPassword": "Admin@123",
  "userToken": "",
  "adminToken": "",
  "productId": "",
  "orderId": ""
}
```

## 1. Authentication Tests

### TC-REG-01: User Registration
- **Endpoint**: POST /auth/register
- **Test Cases**:
  1. Successful registration with valid data
  2. Duplicate email registration
  3. Invalid email format
  4. Missing required fields
  5. Password complexity validation

### TC-LOG-01: User Login
- **Endpoint**: POST /auth/login
- **Test Cases**:
  1. Successful login with username
  2. Successful login with email
  3. Invalid credentials
  4. Account locked after multiple failures
  5. Session token validation

### TC-USER-01: Current User Info
- **Endpoint**: GET /auth/me
- **Test Cases**:
  1. Get authenticated user info
  2. Invalid/expired token
  3. Token permissions validation

## 2. Product Management Tests

### TC-PROD-01: Product Listing
- **Endpoint**: GET /products
- **Test Cases**:
  1. Get all products with pagination
  2. Sort by different fields
  3. Filter by category
  4. Search by name
  5. Price range filtering

### TC-PROD-02: Product Details
- **Endpoint**: GET /products/{id}
- **Test Cases**:
  1. Get existing product
  2. Non-existent product
  3. Invalid product ID format
  4. Access control validation

### TC-PROD-03: Product Search
- **Endpoint**: GET /products/search
- **Test Cases**:
  1. Search by exact name
  2. Partial name match
  3. Case-insensitive search
  4. Multiple search terms
  5. Empty search results

### TC-PROD-04: Category Products
- **Endpoint**: GET /products/category/{categoryId}
- **Test Cases**:
  1. Get products by valid category
  2. Invalid category ID
  3. Empty category
  4. Pagination validation
  5. Sorting validation

### TC-PROD-05: Stock Management
- **Endpoint**: GET /products/in-stock
- **Test Cases**:
  1. List in-stock products
  2. Low stock products
  3. Out of stock products
  4. Stock threshold validation

## 3. Order Management Tests

### TC-ORD-01: Order Creation
- **Endpoint**: POST /orders/checkout
- **Test Cases**:
  1. Create order with valid cart
  2. Empty cart validation
  3. Invalid product in cart
  4. Out of stock products
  5. Shipping address validation

### TC-ORD-02: Order Listing
- **Endpoint**: GET /orders
- **Test Cases**:
  1. Get user orders with pagination
  2. Filter by status
  3. Date range filtering
  4. Sort by different fields
  5. Access control validation

### TC-ORD-03: Order Details
- **Endpoint**: GET /orders/{id}
- **Test Cases**:
  1. Get existing order
  2. Non-existent order
  3. Order access permissions
  4. Order status validation

### TC-ORD-04: Order Status
- **Endpoint**: GET /orders/status/{status}
- **Test Cases**:
  1. Filter by valid status
  2. Invalid status value
  3. Status transition validation
  4. Status update permissions

## 4. Admin Management Tests

### TC-ADM-01: Admin Login
- **Endpoint**: POST /admin/login
- **Test Cases**:
  1. Valid admin credentials
  2. Invalid credentials
  3. Role validation
  4. Permission levels

### TC-ADM-02: Order Management
- **Endpoint**: PUT /admin/orders/{orderId}/approve
- **Test Cases**:
  1. Approve valid order
  2. Invalid order status transition
  3. Admin permission validation
  4. Order notification system

## Test Execution Flow

1. Environment Setup
   - Set base URL
   - Initialize test data
   - Clean up previous test data

2. Authentication Flow
   - Register new user
   - Verify email uniqueness
   - Login with credentials
   - Validate token

3. Product Testing
   - List all products
   - Verify pagination
   - Search functionality
   - Category filtering

4. Order Processing
   - Add items to cart
   - Create order
   - Verify order status
   - Test notifications

5. Admin Operations
   - Admin authentication
   - Order management
   - User management
   - System configuration

## Error Handling

### Common Error Scenarios
1. Network errors
   - Retry mechanism
   - Timeout handling
   - Connection recovery

2. Authentication errors
   - Token refresh
   - Session recovery
   - Permission validation

3. Data validation errors
   - Input sanitization
   - Format validation
   - Business rule validation

4. System errors
   - Error logging
   - Graceful degradation
   - Recovery procedures

## Test Data Management

### Required Test Data
1. User accounts
   - Regular users
   - Admin users
   - Different roles

2. Products
   - Various categories
   - Different price ranges
   - Stock levels

3. Orders
   - Different statuses
   - Various payment methods
   - Multiple items

### Data Cleanup
1. Pre-test cleanup
   - Remove test users
   - Reset product stock
   - Clear test orders

2. Post-test cleanup
   - Remove test data
   - Reset system state
   - Clear cached data

## Reporting

### Test Reports
1. Execution summary
   - Total tests
   - Pass/fail ratio
   - Error categories

2. Coverage report
   - API coverage
   - Scenario coverage
   - Edge cases tested

3. Performance metrics
   - Response times
   - Success rates
   - Error distribution

## Environment Requirements

### Backend Setup
1. Database
   - Clean test database
   - Required migrations
   - Initial seed data

2. Server Configuration
   - Test environment
   - Required services
   - External dependencies

### Test Tools
1. Postman
   - Collection runner
   - Environment variables
   - Pre-request scripts
   - Test scripts

2. Monitoring
   - Log collection
   - Error tracking
   - Performance monitoring