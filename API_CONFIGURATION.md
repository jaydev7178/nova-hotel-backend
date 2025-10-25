# Nova Hotel Supplies Backend API Configuration

This document provides the complete API configuration for the Nova Hotel Supplies backend to ensure seamless integration with the frontend.

## 🚀 Base Configuration

- **Base URL**: `http://localhost:8080/api`
- **Authentication**: JWT Bearer Token
- **Content-Type**: `application/json`
- **CORS**: Enabled for frontend domains

## 🔐 Authentication Endpoints

### User Registration
```http
POST /api/auth/register
Content-Type: application/json

{
  "username": "string",
  "email": "string",
  "password": "string",
  "fullName": "string",
  "phoneNumber": "string",
  "address": "string"
}
```

**Response:**
```json
{
  "success": true,
  "message": "User registered successfully",
  "data": {
    "id": 1,
    "username": "user123",
    "email": "user@example.com",
    "fullName": "John Doe",
    "role": "USER",
    "isActive": true,
    "createdAt": "2024-01-01T00:00:00"
  }
}
```

### User Login
```http
POST /api/auth/login
Content-Type: application/json

{
  "usernameOrEmail": "string",
  "password": "string"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "token": "jwt-token-here",
    "user": {
      "id": 1,
      "username": "user123",
      "email": "user@example.com",
      "fullName": "John Doe",
      "role": "USER"
    },
    "message": "Login successful"
  }
}
```

### Get Current User
```http
GET /api/auth/me
Authorization: Bearer <jwt-token>
```

## 🛍️ Product Endpoints

### Get All Products (Paginated)
```http
GET /api/products?page=0&size=10&sortBy=name&sortDir=asc
```

**Response:**
```json
{
  "content": [
    {
      "id": 1,
      "name": "Professional Chef Knife Set",
      "description": "High-quality stainless steel knife set",
      "price": 89.99,
      "stockQuantity": 50,
      "sku": "KIT-001",
      "imageUrl": "/uploads/products/knife-set.jpg",
      "category": {
        "id": 1,
        "name": "Kitchen Supplies"
      },
      "isActive": true,
      "createdAt": "2024-01-01T00:00:00"
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
  "totalElements": 100,
  "totalPages": 10,
  "first": true,
  "last": false
}
```

### Get Product by ID
```http
GET /api/products/{id}
```

### Search Products
```http
GET /api/products/search?name=knife&page=0&size=10
```

### Get Products by Category
```http
GET /api/products/category/{categoryId}?page=0&size=10
```

### Get In-Stock Products
```http
GET /api/products/in-stock?page=0&size=10
```

### Get Products by Price Range
```http
GET /api/products/price-range?minPrice=10&maxPrice=100&page=0&size=10
```

## 📦 Category Endpoints

### Get All Categories
```http
GET /api/categories
```

**Response:**
```json
[
  {
    "id": 1,
    "name": "Kitchen Supplies",
    "description": "Kitchen and cooking equipment for hotels",
    "isActive": true,
    "createdAt": "2024-01-01T00:00:00"
  }
]
```

### Get Category by ID
```http
GET /api/categories/{id}
```

### Search Categories
```http
GET /api/categories/search?name=kitchen
```

## 🛒 Order Endpoints

### Create Order (Checkout)
```http
POST /api/orders/checkout
Authorization: Bearer <jwt-token>
Content-Type: application/json

{
  "cartItems": {
    "1": 2,
    "3": 1,
    "5": 3
  },
  "shippingAddress": "123 Main St, City, State 12345",
  "notes": "Please deliver during business hours"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Order created successfully",
  "data": {
    "id": 1,
    "orderNumber": "ORD-1704067200000",
    "user": {
      "id": 1,
      "username": "user123",
      "email": "user@example.com",
      "fullName": "John Doe"
    },
    "status": "PENDING",
    "totalAmount": 299.97,
    "shippingAddress": "123 Main St, City, State 12345",
    "notes": "Please deliver during business hours",
    "termsAccepted": false,
    "createdAt": "2024-01-01T00:00:00",
    "orderItems": [
      {
        "id": 1,
        "product": {
          "id": 1,
          "name": "Professional Chef Knife Set",
          "price": 89.99
        },
        "quantity": 2,
        "unitPrice": 89.99,
        "totalPrice": 179.98
      }
    ]
  }
}
```

### Get User Orders
```http
GET /api/orders?page=0&size=10&sortBy=createdAt&sortDir=desc
Authorization: Bearer <jwt-token>
```

### Get Order by ID
```http
GET /api/orders/{id}
Authorization: Bearer <jwt-token>
```

### Get Order by Order Number
```http
GET /api/orders/order-number/{orderNumber}
```

### Accept Terms and Update Order
```http
PUT /api/orders/{id}/accept-terms
Authorization: Bearer <jwt-token>
```

### Get Orders by Status
```http
GET /api/orders/status/{status}?page=0&size=10
```

**Order Statuses:**
- `PENDING`
- `APPROVED`
- `PAYMENT_INFO_SENT`
- `PAYMENT_CONFIRMED`
- `DELIVERY_INITIATED`
- `DELIVERED`
- `CANCELLED`

## 👤 User Endpoints

### Get Current User Profile
```http
GET /api/users/profile
Authorization: Bearer <jwt-token>
```

### Update User Profile
```http
PUT /api/users/update-profile
Authorization: Bearer <jwt-token>
Content-Type: application/json

{
  "fullName": "John Doe Updated",
  "phoneNumber": "+1234567890",
  "address": "123 Main St, City, State 12345"
}
```

## 🔧 Admin Endpoints

### Get All Users
```http
GET /api/admin/users
Authorization: Bearer <jwt-token>
```

### Create Category
```http
POST /api/admin/categories
Authorization: Bearer <jwt-token>
Content-Type: application/json

{
  "name": "New Category",
  "description": "Category description"
}
```

### Create Product
```http
POST /api/admin/products
Authorization: Bearer <jwt-token>
Content-Type: application/json

{
  "name": "New Product",
  "description": "Product description",
  "price": 99.99,
  "stockQuantity": 50,
  "sku": "NEW-001",
  "category": {
    "id": 1
  }
}
```

### Update Order Status
```http
PUT /api/admin/orders/{id}/status
Authorization: Bearer <jwt-token>
Content-Type: application/json

{
  "status": "APPROVED"
}
```

### Send Payment Info
```http
PUT /api/orders/{id}/send-payment-info
Authorization: Bearer <jwt-token>
Content-Type: application/json

{
  "paymentInfo": "Bank: ABC Bank, Account: 123456789, Routing: 987654321"
}
```

### Confirm Payment
```http
PUT /api/orders/{id}/confirm-payment
Authorization: Bearer <jwt-token>
```

### Initiate Delivery
```http
PUT /api/orders/{id}/initiate-delivery
Authorization: Bearer <jwt-token>
```

### Mark as Delivered
```http
PUT /api/orders/{id}/mark-delivered
Authorization: Bearer <jwt-token>
```

## 📧 Email Integration

The backend automatically sends emails for:
- Order confirmation to users
- Order notifications to owners
- Payment information
- Delivery updates

## 🔒 Security Configuration

### JWT Token Structure
```json
{
  "sub": "username",
  "role": "USER",
  "iat": 1704067200,
  "exp": 1704153600
}
```

### Role-Based Access
- **USER**: Can browse products, create orders, manage profile
- **OWNER**: Can manage products, approve orders, send payment info
- **ADMIN**: Full access to all endpoints

## 📊 Error Responses

### Validation Error
```json
{
  "status": 400,
  "message": "Validation failed",
  "timestamp": "2024-01-01T00:00:00",
  "details": "{fieldName=error message}"
}
```

### Authentication Error
```json
{
  "status": 401,
  "message": "Authentication failed: Invalid credentials",
  "timestamp": "2024-01-01T00:00:00",
  "details": "uri=/api/auth/login"
}
```

### Access Denied
```json
{
  "status": 403,
  "message": "Access denied: Insufficient privileges",
  "timestamp": "2024-01-01T00:00:00",
  "details": "uri=/api/admin/users"
}
```

## 🚀 Frontend Integration Guide

### 1. Authentication Flow
1. User registers/logs in
2. Store JWT token in localStorage/sessionStorage
3. Include token in all authenticated requests
4. Handle token expiration (refresh or re-login)

### 2. Product Browsing
1. Fetch categories for navigation
2. Implement pagination for product lists
3. Add search and filter functionality
4. Handle product images from `/uploads/products/`

### 3. Shopping Cart
1. Maintain cart state in frontend
2. Validate stock availability before checkout
3. Send cart items as `{productId: quantity}` map

### 4. Order Management
1. Display order status with appropriate UI
2. Allow users to accept terms for pending orders
3. Show payment information when available
4. Track delivery status

### 5. Admin Panel
1. Implement role-based UI components
2. Provide order management interface
3. Add product/category management
4. Show order statistics and analytics

## 🔧 Environment Variables

Set these environment variables for production:

```env
DB_USERNAME=your_db_username
DB_PASSWORD=your_db_password
JWT_SECRET=your_jwt_secret_key
MAIL_USERNAME=your_email@gmail.com
MAIL_PASSWORD=your_app_password
OWNER_EMAIL=owner@novahotelsupplies.com
CORS_ORIGINS=https://yourdomain.com,https://www.yourdomain.com
```

## 📱 Mobile App Integration

The API is designed to work with mobile applications:
- All endpoints return JSON
- JWT authentication works with mobile apps
- File uploads supported for product images
- CORS configured for mobile app domains

## 🔄 Real-time Updates

For real-time order updates, consider implementing:
- WebSocket connections for order status changes
- Server-sent events for notifications
- Polling mechanism for order status updates

## 📈 Performance Optimization

- Implement pagination for all list endpoints
- Use appropriate HTTP caching headers
- Optimize database queries with proper indexing
- Implement rate limiting for API endpoints

This configuration ensures seamless integration between the backend and frontend, providing a robust foundation for the Nova Hotel Supplies e-commerce platform.

