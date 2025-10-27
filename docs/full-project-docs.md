# Nova Hotel Project Documentation

## Table of Contents
- [Overview](#overview)
- [Backend Documentation](#backend-documentation)
- [Frontend Documentation](#frontend-documentation)
- [Integration Guide](#integration-guide)
- [Testing Guide](#testing-guide)

## Overview

Nova Hotel is a full-stack web application built with Spring Boot backend and Angular frontend. This documentation provides comprehensive information about both projects and their integration.

## Backend Documentation

### Tech Stack
- Java 21
- Spring Boot 3.x
- Spring Security with JWT
- JPA/Hibernate
- PostgreSQL Database
- Gradle Build Tool
- Flyway Migration

### Setup and Running

1. Prerequisites:
   - Java 21 JDK
   - PostgreSQL
   - Gradle

2. Clone the repository:
   ```powershell
   git clone https://github.com/jaydev7178/nova-hotel-backend.git
   cd nova-hotel-backend
   ```

3. Configure environment:
   - Copy `application-dev.yml` to create local configuration
   - Update database credentials

4. Build and run:
   ```powershell
   ./gradlew bootRun
   ```

### Environment Variables
- `SPRING_PROFILES_ACTIVE`: dev/prod
- `DB_URL`: Database URL
- `DB_USERNAME`: Database username
- `DB_PASSWORD`: Database password
- `JWT_SECRET`: Secret key for JWT tokens
- `JWT_EXPIRATION`: Token expiration time
- `FILE_UPLOAD_PATH`: Path for file uploads

### Database Migrations
- Located in `src/main/resources/db/migration`
- Managed by Flyway
- Run automatically on application startup
- Migration naming: `V{version}__{description}.sql`

### API Endpoints

#### Authentication
- POST `/api/auth/login`
  ```json
  {
    "email": "user@example.com",
    "password": "password123"
  }
  ```
  Response:
  ```json
  {
    "token": "jwt.token.here",
    "user": {
      "id": 1,
      "email": "user@example.com",
      "roles": ["USER"]
    }
  }
  ```

#### Products
- GET `/api/products` - List all products
- GET `/api/products/{id}` - Get single product
- POST `/api/products` - Create product (Admin)
- PUT `/api/products/{id}` - Update product (Admin)
- DELETE `/api/products/{id}` - Delete product (Admin)

#### Orders
- GET `/api/orders` - List user orders
- POST `/api/orders` - Create order
- GET `/api/orders/{id}` - Get order details
- PUT `/api/orders/{id}/status` - Update order status (Admin)

### Security Implementation
- JWT-based authentication
- Role-based authorization (USER, ADMIN)
- Token refresh mechanism
- Password encryption using BCrypt
- CORS configuration for frontend integration

### Known Issues and TODOs
1. Implement order notification system
2. Add rate limiting for API endpoints
3. Implement caching for product catalog
4. Add order analytics endpoints

## Frontend Documentation

### Tech Stack
- Angular 17+
- Bootstrap 5
- RxJS
- NgRx (planned)

### Project Structure
```
src/
├── app/
│   ├── core/
│   │   ├── auth/
│   │   ├── interceptors/
│   │   └── services/
│   ├── features/
│   │   ├── products/
│   │   ├── orders/
│   │   └── admin/
│   └── shared/
│       ├── components/
│       └── models/
```

### Key Components
1. AuthService - Handles authentication
2. ProductService - Product management
3. OrderService - Order processing
4. AdminDashboard - Admin features
5. CartService - Shopping cart management

### Environment Configuration
```typescript
// environment.ts
export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080/api',
  imageBaseUrl: 'http://localhost:8080/uploads'
};
```

## Integration Guide

### Project Setup Requirements
1. Backend Setup:
   - Ensure backend is running on port 8080
   - CORS is configured for frontend origin
   - All required environment variables are set
   - Database migrations are executed

2. Frontend Setup:
   - Node.js 18+ installed
   - Angular CLI installed globally
   - Environment files configured correctly
   - Required dependencies installed

### Authentication Flow Details
1. User Login Process:
   - Frontend collects credentials from login form
   - POST request to `/api/auth/login` with credentials
   - Backend validates and generates JWT
   - Frontend stores token in localStorage and updates auth state
   - Redirect to protected route (e.g., dashboard)

2. Token Management:
   - Token storage: localStorage with key 'token'
   - Token refresh: 15 minutes before expiration
   - Auto logout on token expiration
   - Clear token on logout/session timeout

3. Protected Routes:
   - Angular Route Guards implementation
   - Role-based access control
   - Unauthorized route redirects
   - Session persistence handling

### API Integration Steps
1. Environment Configuration:
   ```typescript
   // frontend/src/environments/environment.ts
   export const environment = {
     production: false,
     apiUrl: 'http://localhost:8080/api',
     imageBaseUrl: 'http://localhost:8080/uploads',
     tokenKey: 'token',
     refreshTokenKey: 'refreshToken'
   };
   ```

2. Core Models Setup:
   ```typescript
   // frontend/src/app/core/models/user.model.ts
   export interface User {
     id: number;
     email: string;
     roles: string[];
     firstName?: string;
     lastName?: string;
   }

   // frontend/src/app/core/models/auth.model.ts
   export interface AuthResponse {
     token: string;
     refreshToken: string;
     user: User;
   }
   ```

3. Error Handling Setup:
   ```typescript
   // frontend/src/app/core/interceptors/error.interceptor.ts
   @Injectable()
   export class ErrorInterceptor implements HttpInterceptor {
     constructor(private router: Router) {}

     intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
       return next.handle(req).pipe(
         catchError(error => {
           if (error.status === 401) {
             localStorage.clear();
             this.router.navigate(['/login']);
           }
           return throwError(() => error);
         })
       );
     }
   }
   ```

### Sample Integration Code

1. Authentication Service:
```typescript
// frontend/src/app/core/services/auth.service.ts
@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private currentUserSubject = new BehaviorSubject<User | null>(null);
  public currentUser$ = this.currentUserSubject.asObservable();

  constructor(
    private http: HttpClient,
    private router: Router
  ) {
    this.loadStoredUser();
  }

  login(email: string, password: string): Observable<void> {
    return this.http.post<AuthResponse>(`${environment.apiUrl}/auth/login`, {
      email,
      password
    }).pipe(
      tap(response => {
        localStorage.setItem(environment.tokenKey, response.token);
        localStorage.setItem(environment.refreshTokenKey, response.refreshToken);
        this.currentUserSubject.next(response.user);
      })
    );
  }

  logout(): void {
    localStorage.clear();
    this.currentUserSubject.next(null);
    this.router.navigate(['/login']);
  }

  private loadStoredUser(): void {
    const token = localStorage.getItem(environment.tokenKey);
    if (token) {
      const decodedToken = jwt_decode(token);
      this.currentUserSubject.next(decodedToken.user);
    }
  }
}
```

2. HTTP Interceptor:
```typescript
// frontend/src/app/core/interceptors/auth.interceptor.ts
@Injectable()
export class AuthInterceptor implements HttpInterceptor {
  constructor(private authService: AuthService) {}

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    const token = localStorage.getItem(environment.tokenKey);
    
    if (token && !this.isTokenExpired(token)) {
      req = req.clone({
        setHeaders: {
          Authorization: `Bearer ${token}`
        }
      });
    } else if (token) {
      // Token expired
      this.authService.logout();
    }
    
    return next.handle(req);
  }

  private isTokenExpired(token: string): boolean {
    const decodedToken = jwt_decode(token);
    return decodedToken.exp * 1000 < Date.now();
  }
}
```

3. Product Service with Error Handling:
```typescript
// frontend/src/app/features/products/services/product.service.ts
@Injectable({
  providedIn: 'root'
})
export class ProductService {
  private productsSubject = new BehaviorSubject<Product[]>([]);
  public products$ = this.productsSubject.asObservable();

  constructor(
    private http: HttpClient,
    private snackBar: MatSnackBar
  ) {}

  getProducts(): Observable<Product[]> {
    return this.http.get<Product[]>(`${environment.apiUrl}/products`).pipe(
      tap(products => this.productsSubject.next(products)),
      catchError(error => {
        this.snackBar.open('Error loading products', 'Close', {
          duration: 3000
        });
        return throwError(() => error);
      })
    );
  }

  createProduct(product: ProductCreate): Observable<Product> {
    const formData = new FormData();
    Object.entries(product).forEach(([key, value]) => {
      formData.append(key, value);
    });

    return this.http.post<Product>(`${environment.apiUrl}/products`, formData).pipe(
      tap(newProduct => {
        const current = this.productsSubject.value;
        this.productsSubject.next([...current, newProduct]);
      })
    );
  }

  updateProduct(id: number, product: ProductUpdate): Observable<Product> {
    return this.http.put<Product>(`${environment.apiUrl}/products/${id}`, product).pipe(
      tap(updatedProduct => {
        const current = this.productsSubject.value;
        const index = current.findIndex(p => p.id === id);
        if (index !== -1) {
          current[index] = updatedProduct;
          this.productsSubject.next([...current]);
        }
      })
    );
  }
}
```

## Testing Guide

### Backend Testing
1. Unit Tests:
   ```powershell
   ./gradlew test
   ```
2. Integration Tests:
   ```powershell
   ./gradlew integrationTest
   ```

### Frontend Testing
1. Unit Tests:
   ```powershell
   ng test
   ```
2. E2E Tests (Cypress):
   ```powershell
   ng e2e
   ```

### API Testing
- Postman collection available in `/docs/postman`
- Basic curl examples:
  ```bash
  # Login
  curl -X POST http://localhost:8080/api/auth/login \
    -H "Content-Type: application/json" \
    -d '{"email":"user@example.com","password":"password123"}'

  # Get Products
  curl http://localhost:8080/api/products \
    -H "Authorization: Bearer {token}"
  ```

### Smoke Tests
1. Backend health check
2. User registration and login
3. Product listing and details
4. Order creation and status update
5. Admin dashboard access

## Development Workflow
1. Feature branch creation
2. Local development and testing
3. PR creation and review
4. Integration testing
5. Deployment to staging
6. Production deployment

## Deployment
- Backend: Docker container on AWS ECS
- Frontend: Static hosting on AWS S3/CloudFront
- Database: AWS RDS PostgreSQL
- CI/CD: GitHub Actions
