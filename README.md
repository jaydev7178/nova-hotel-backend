# Nova Hotel Supplies Backend

A comprehensive Spring Boot backend for the Nova Hotel Supplies e-commerce platform.

## 🚀 Features

- **User Management**: Registration, authentication, and profile management
- **Product Management**: CRUD operations for products with categories
- **Order Management**: Complete order workflow from cart to delivery
- **Email Notifications**: Automated email notifications for order updates
- **Security**: JWT-based authentication with role-based authorization
- **API Documentation**: Swagger/OpenAPI documentation
- **Database**: MySQL with migration scripts

## 🛠️ Technology Stack

- **Java 21**
- **Spring Boot 3.2.0**
- **Spring Security** with JWT
- **Spring Data JPA**
- **MySQL 8.0**
- **Gradle** build system
- **Thymeleaf** for email templates
- **Swagger/OpenAPI** for API documentation

## 📋 Prerequisites

- Java 21 or higher
- MySQL 8.0 or higher
- Gradle 7.0 or higher

## 🚀 Getting Started

### 1. Clone the Repository

```bash
git clone <repository-url>
cd nova-hotel-backend
```

### 2. Database Setup

Create a MySQL database:

```sql
CREATE DATABASE nova_hotel_db;
```

### 3. Environment Configuration

Create a `.env` file in the root directory:

```env
DB_USERNAME=your_db_username
DB_PASSWORD=your_db_password
JWT_SECRET=your_jwt_secret_key
MAIL_USERNAME=your_email@gmail.com
MAIL_PASSWORD=your_app_password
OWNER_EMAIL=owner@novahotelsupplies.com
```

### 4. Build and Run

```bash
# Build the project
./gradlew build

# Run the application
./gradlew bootRun
```

The application will start on `http://localhost:8080`

## 📚 API Documentation

Once the application is running, you can access the API documentation at:
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

## 🔐 Authentication

The API uses JWT-based authentication. Include the JWT token in the Authorization header:

```
Authorization: Bearer <your-jwt-token>
```

## 📊 Database Schema

The application includes the following main entities:

- **Users**: User accounts with roles (USER, OWNER, ADMIN)
- **Categories**: Product categories
- **Products**: Product catalog with stock management
- **Orders**: Order management with status tracking
- **OrderItems**: Individual items within orders

## 🔄 Order Workflow

1. **PENDING**: Order created, waiting for approval
2. **APPROVED**: Order approved by owner
3. **PAYMENT_INFO_SENT**: Payment information sent to user
4. **PAYMENT_CONFIRMED**: Payment confirmed
5. **DELIVERY_INITIATED**: Delivery started
6. **DELIVERED**: Order completed

## 📧 Email Notifications

The system sends automated emails for:
- Order confirmation to users
- Order notifications to owners
- Payment information
- Delivery updates

## 🛡️ Security Features

- JWT token authentication
- Role-based authorization
- Password encryption
- CORS configuration
- Input validation
- SQL injection prevention

## 🧪 Testing

Run tests with:

```bash
./gradlew test
```

## 📦 Deployment

### Production Environment

1. Set environment variables:
   ```bash
   export DB_USERNAME=production_db_user
   export DB_PASSWORD=production_db_password
   export JWT_SECRET=production_jwt_secret
   export MAIL_USERNAME=production_email
   export MAIL_PASSWORD=production_email_password
   ```

2. Build for production:
   ```bash
   ./gradlew build -Pprofile=prod
   ```

3. Run the JAR:
   ```bash
   java -jar build/libs/nova-hotel-backend-1.0.0.jar
   ```

## 🔧 Configuration

### Application Properties

The application supports multiple profiles:
- `dev`: Development environment
- `prod`: Production environment

### Database Configuration

Configure database connection in `application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/nova_hotel_db
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
```

### Email Configuration

Configure email settings:

```yaml
spring:
  mail:
    host: smtp.gmail.com
    port: 587
    username: ${MAIL_USERNAME}
    password: ${MAIL_PASSWORD}
```

## 📝 API Endpoints

### Authentication
- `POST /api/auth/register` - User registration
- `POST /api/auth/login` - User login
- `GET /api/auth/me` - Get current user

### Products
- `GET /api/products` - Get all products (paginated)
- `GET /api/products/{id}` - Get product by ID
- `GET /api/products/category/{categoryId}` - Get products by category
- `GET /api/products/search` - Search products

### Orders
- `POST /api/orders/checkout` - Create new order
- `GET /api/orders` - Get user orders
- `GET /api/orders/{id}` - Get order by ID
- `PUT /api/orders/{id}/accept-terms` - Accept terms and approve order

### Admin
- `GET /api/admin/users` - Get all users
- `POST /api/admin/categories` - Create category
- `POST /api/admin/products` - Create product
- `GET /api/admin/orders` - Get all orders

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests if applicable
5. Submit a pull request

## 📄 License

This project is licensed under the MIT License.

## 📞 Support

For support, email support@novahotelsupplies.com or create an issue in the repository.

## 🔗 Frontend Integration

This backend is designed to work with the Nova Hotel Supplies frontend. The API responses are structured to match the frontend's expected data format.

For frontend integration details, see the API documentation at `/swagger-ui.html` when the application is running.