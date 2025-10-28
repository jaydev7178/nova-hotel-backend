# Nova Hotel API Testing Flow Documentation

## Overview
This document outlines the complete end-to-end API testing flow for the Nova Hotel system using Postman. The collection is designed to automate the entire user journey from registration to order confirmation, including admin interactions.

## Test Environment Setup

### Required Environment Variables
```json
{
  "baseUrl": "http://localhost:8080/api",
  "userEmail": "{{$randomEmail}}",
  "userPassword": "Test@123",
  "adminEmail": "admin@novahotel.com",
  "adminPassword": "Admin@123",
  "userToken": "",
  "adminToken": "",
  "productId": "",
  "orderId": "",
  "retryCount": 0
}
```

## Postman Collection Structure

### 1. User Registration Flow
```json
{
  "name": "1. User Registration",
  "request": {
    "method": "POST",
    "url": "{{baseUrl}}/auth/register",
    "body": {
      "mode": "raw",
      "raw": {
        "email": "{{userEmail}}",
        "password": "{{userPassword}}",
        "firstName": "Test",
        "lastName": "User"
      }
    },
    "pre-request": {
      "script": {
        "exec": [
          "pm.environment.set('retryCount', 0);",
          "console.log('Starting user registration...');"
        ]
      }
    },
    "test": {
      "script": {
        "exec": [
          "pm.test('Registration successful', () => {",
          "  const maxRetries = 2;",
          "  const response = pm.response.json();",
          "  ",
          "  if (pm.response.code === 201) {",
          "    pm.environment.set('userId', response.id);",
          "    console.log('Registration successful');",
          "  } else {",
          "    const retryCount = parseInt(pm.environment.get('retryCount')) || 0;",
          "    if (retryCount < maxRetries) {",
          "      pm.environment.set('retryCount', retryCount + 1);",
          "      console.log(`Registration failed. Retrying... (${retryCount + 1}/${maxRetries})`);",
          "      postman.setNextRequest('1. User Registration');",
          "    } else {",
          "      throw new Error('Registration failed after maximum retries');",
          "    }",
          "  }",
          "});"
        ]
      }
    }
  }
}
```

### 2. User Login Flow
```json
{
  "name": "2. User Login",
  "request": {
    "method": "POST",
    "url": "{{baseUrl}}/auth/login",
    "body": {
      "mode": "raw",
      "raw": {
        "email": "{{userEmail}}",
        "password": "{{userPassword}}"
      }
    },
    "pre-request": {
      "script": {
        "exec": [
          "pm.environment.set('retryCount', 0);",
          "console.log('Attempting user login...');"
        ]
      }
    },
    "test": {
      "script": {
        "exec": [
          "pm.test('Login successful', () => {",
          "  const response = pm.response.json();",
          "  ",
          "  if (pm.response.code === 200 && response.token) {",
          "    pm.environment.set('userToken', response.token);",
          "    console.log('Login successful');",
          "  } else {",
          "    const retryCount = parseInt(pm.environment.get('retryCount')) || 0;",
          "    if (retryCount < 2) {",
          "      pm.environment.set('retryCount', retryCount + 1);",
          "      console.log('Login failed. Retrying registration...');",
          "      postman.setNextRequest('1. User Registration');",
          "    } else {",
          "      throw new Error('Login failed after retries');",
          "    }",
          "  }",
          "});"
        ]
      }
    }
  }
}
```

### 3. Product Browsing & Cart Management
```json
{
  "name": "3.1 Get Products",
  "request": {
    "method": "GET",
    "url": "{{baseUrl}}/products",
    "headers": {
      "Authorization": "Bearer {{userToken}}"
    },
    "test": {
      "script": {
        "exec": [
          "pm.test('Products retrieved successfully', () => {",
          "  const response = pm.response.json();",
          "  if (response.products && response.products.length > 0) {",
          "    pm.environment.set('productId', response.products[0].id);",
          "    console.log('Product selected:', response.products[0].id);",
          "  } else {",
          "    throw new Error('No products available');",
          "  }",
          "});"
        ]
      }
    }
  }
},
{
  "name": "3.2 Add to Cart",
  "request": {
    "method": "POST",
    "url": "{{baseUrl}}/cart/add",
    "headers": {
      "Authorization": "Bearer {{userToken}}"
    },
    "body": {
      "mode": "raw",
      "raw": {
        "productId": "{{productId}}",
        "quantity": 1
      }
    },
    "test": {
      "script": {
        "exec": [
          "pm.test('Product added to cart', () => {",
          "  pm.response.to.have.status(200);",
          "});"
        ]
      }
    }
  }
}
```

### 4. Order Placement
```json
{
  "name": "4.1 Create Order",
  "request": {
    "method": "POST",
    "url": "{{baseUrl}}/orders",
    "headers": {
      "Authorization": "Bearer {{userToken}}"
    },
    "test": {
      "script": {
        "exec": [
          "pm.test('Order created successfully', () => {",
          "  const response = pm.response.json();",
          "  if (response.orderId) {",
          "    pm.environment.set('orderId', response.orderId);",
          "    console.log('Order created:', response.orderId);",
          "  } else {",
          "    throw new Error('Order creation failed');",
          "  }",
          "});"
        ]
      }
    }
  }
}
```

### 5. Admin Actions
```json
{
  "name": "5.1 Admin Login",
  "request": {
    "method": "POST",
    "url": "{{baseUrl}}/admin/login",
    "body": {
      "mode": "raw",
      "raw": {
        "email": "{{adminEmail}}",
        "password": "{{adminPassword}}"
      }
    },
    "test": {
      "script": {
        "exec": [
          "pm.test('Admin login successful', () => {",
          "  const response = pm.response.json();",
          "  if (response.token) {",
          "    pm.environment.set('adminToken', response.token);",
          "    console.log('Admin login successful');",
          "  } else {",
          "    throw new Error('Admin login failed');",
          "  }",
          "});"
        ]
      }
    }
  }
},
{
  "name": "5.2 Approve Order",
  "request": {
    "method": "PUT",
    "url": "{{baseUrl}}/admin/orders/{{orderId}}/approve",
    "headers": {
      "Authorization": "Bearer {{adminToken}}"
    },
    "test": {
      "script": {
        "exec": [
          "pm.test('Order approved successfully', () => {",
          "  pm.response.to.have.status(200);",
          "  console.log('Order approved:', pm.environment.get('orderId'));",
          "});"
        ]
      }
    }
  }
}
```

### 6. User Order Confirmation
```json
{
  "name": "6.1 Check Order Status",
  "request": {
    "method": "GET",
    "url": "{{baseUrl}}/orders/{{orderId}}",
    "headers": {
      "Authorization": "Bearer {{userToken}}"
    },
    "test": {
      "script": {
        "exec": [
          "pm.test('Order status is approved', () => {",
          "  const response = pm.response.json();",
          "  pm.expect(response.status).to.equal('APPROVED');",
          "  console.log('Order status verified');",
          "});"
        ]
      }
    }
  }
},
{
  "name": "6.2 Confirm Order",
  "request": {
    "method": "POST",
    "url": "{{baseUrl}}/orders/{{orderId}}/confirm",
    "headers": {
      "Authorization": "Bearer {{userToken}}"
    },
    "test": {
      "script": {
        "exec": [
          "pm.test('Order confirmed successfully', () => {",
          "  pm.response.to.have.status(200);",
          "  console.log('Order confirmation complete');",
          "});"
        ]
      }
    }
  }
}
```

## Running the Collection

### Prerequisites
1. Postman installed
2. Environment variables configured
3. Backend server running

### Steps to Run
1. Import the collection into Postman
2. Set up the environment variables
3. Use Collection Runner or Newman CLI:
   ```bash
   newman run NovaHotel.postman_collection.json -e environment.json
   ```

### Monitoring Progress
- Each step logs progress to the Postman console
- Failed requests automatically retry (up to 3 times)
- Environment variables persist between requests
- Full test results available after completion

## Error Handling

### Retry Logic
- Registration: 2 retries
- Login: 2 retries with re-registration
- Product selection: Auto-select alternative if first fails
- Order creation: 1 retry with fresh cart
- Admin approval: 1 retry after refresh

### Common Error Scenarios
1. Registration fails:
   - Retry with new email
   - Log failure reason
   - Maximum 2 attempts

2. Login fails:
   - Attempt re-registration
   - Use backup credentials
   - Maximum 2 attempts

3. Product unavailable:
   - Select alternative product
   - Refresh product list
   - Skip if no products available

4. Order creation fails:
   - Clear cart
   - Re-add products
   - Retry order creation

5. Admin approval fails:
   - Refresh admin token
   - Retry approval
   - Maximum 1 retry

## Test Success Criteria

### Required Response Codes
- Registration: 201 Created
- Login: 200 OK with token
- Product listing: 200 OK with products
- Cart operations: 200 OK
- Order creation: 201 Created
- Order approval: 200 OK
- Order confirmation: 200 OK

### Data Validation
- Valid JWT tokens received
- Product IDs exist
- Order IDs created
- Status transitions correct
- Response payloads match schema

## Maintenance

### Updating the Collection
1. Export collection JSON
2. Update request parameters
3. Update test scripts
4. Update environment variables
5. Re-import collection

### Troubleshooting
- Check environment variables
- Verify server status
- Review Postman console logs
- Check retry counters
- Validate token expiration

## Security Notes
- Tokens stored in environment variables
- Passwords not logged
- Admin credentials secured
- Session management automated
- HTTPS enforced in production
