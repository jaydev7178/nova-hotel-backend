# Nova Hotel Backend - Logging Implementation

## Overview
This document outlines the comprehensive logging implementation added to the Nova Hotel Backend application using SLF4J and Logback.

## Dependencies Added

### Spring Boot Logging
Spring Boot already includes SLF4J and Logback dependencies, so no additional dependencies were needed. The logging implementation uses:
- **SLF4J API** - Logging facade (included with Spring Boot)
- **Logback Classic** - Logging implementation (included with Spring Boot)
- **Logback Core** - Core logging functionality (included with Spring Boot)

## Configuration Files

### logback-spring.xml
- **Location**: `src/main/resources/logback-spring.xml`
- **Purpose**: Main logging configuration with profile-specific settings
- **Features**:
  - Development profile: Console + File logging with DEBUG level
  - Production profile: File logging only with WARN level + separate ERROR file
  - Rolling file appenders with size and time-based rotation
  - Configurable log retention (30 days dev, 60 days prod)

### application.yml
- **Simplified logging configuration**
- **Removed verbose Spring Security logging**
- **Kept essential application and SQL logging**

## Logging Implementation

### Controllers
All controllers now include comprehensive logging:

#### AuthController
- **Login attempts**: INFO level with username
- **Registration attempts**: INFO level with username/email
- **Success/failure logging**: INFO for success, WARN for failures
- **Security events**: Authentication failures logged appropriately

#### OrderController
- **Cart operations**: INFO level for add/remove operations
- **Checkout process**: DEBUG level for detailed cart processing
- **Order retrieval**: INFO level with pagination details
- **Error handling**: ERROR level for unexpected errors

### Services

#### OrderService
- **Order creation**: Comprehensive logging throughout the process
- **Product validation**: WARN level for stock issues
- **Business logic errors**: ERROR level for critical failures
- **Success tracking**: INFO level for completed operations

#### UserService
- **User registration**: INFO level for new registrations
- **Validation failures**: WARN level for duplicate username/email
- **User operations**: DEBUG level for user updates

#### EmailService
- **Email sending**: INFO level for email operations
- **Email failures**: ERROR level with full exception details
- **Template processing**: DEBUG level for template operations

### Security Components

#### JwtAuthenticationFilter
- **Token validation**: DEBUG level for successful authentications
- **Token failures**: WARN level for invalid/expired tokens
- **Authentication flow**: Detailed logging of JWT processing

### Exception Handling

#### GlobalExceptionHandler
- **Runtime exceptions**: ERROR level with full stack traces
- **Authentication failures**: WARN level for security events
- **Validation errors**: INFO level for client errors
- **Unexpected errors**: ERROR level with complete context

## Log File Structure

### Development Environment
- **Console Output**: All logs with colored formatting
- **File**: `logs/nova-hotel-dev.log`
- **Rotation**: Daily rotation, 10MB max size, 30 days retention
- **Level**: DEBUG for application, INFO for root

### Production Environment
- **Main Log**: `logs/nova-hotel-prod.log`
- **Error Log**: `logs/nova-hotel-error.log` (ERROR level only)
- **Rotation**: Daily rotation, 50MB max size, 60 days retention
- **Level**: INFO for application, WARN for root

## Log Levels Used

### ERROR
- Database connection failures
- Email sending failures
- Unexpected application errors
- Critical business logic failures

### WARN
- Authentication failures
- Validation errors
- Business rule violations (insufficient stock, etc.)
- Invalid JWT tokens

### INFO
- User registration/login events
- Order creation and status changes
- Email sending operations
- Important business operations

### DEBUG
- Detailed request processing
- JWT token validation details
- Cart operations details
- User operations

## Security Considerations

### Sensitive Data Protection
- **Passwords**: Never logged in plain text
- **JWT Tokens**: Only validation status logged, not token content
- **Personal Information**: Minimal logging of PII
- **Email Content**: Only metadata logged, not email body

### Log File Security
- **File Permissions**: Restricted access to log files
- **Rotation**: Automatic cleanup of old logs
- **Size Limits**: Prevents disk space exhaustion

## Monitoring and Alerting

### Key Metrics to Monitor
- **ERROR level logs**: Immediate attention required
- **Authentication failures**: Security monitoring
- **Order processing errors**: Business impact
- **Email delivery failures**: Customer communication issues

### Log Analysis
- **Structured logging**: Consistent format for parsing
- **Correlation IDs**: Track requests across components
- **Performance metrics**: Response times and throughput

## Best Practices Implemented

### Logging Standards
- **Consistent format**: Timestamp, thread, level, logger, message
- **Meaningful messages**: Clear, actionable log messages
- **Appropriate levels**: Correct log level for each event type
- **Exception handling**: Full stack traces for errors

### Performance Considerations
- **Async logging**: Non-blocking log operations
- **Log level checks**: Conditional debug logging
- **Efficient formatting**: Parameterized log messages
- **Resource management**: Proper file handle management

## Usage Examples

### Adding Logging to New Classes
```java
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class NewService {
    private static final Logger log = LoggerFactory.getLogger(NewService.class);
    
    public void someMethod() {
        log.info("Starting operation");
        try {
            // business logic
            log.debug("Operation details: {}", details);
            log.info("Operation completed successfully");
        } catch (Exception e) {
            log.error("Operation failed", e);
            throw e;
        }
    }
}
```

### Log Message Patterns
```java
// INFO: Business operations
log.info("User {} logged in successfully", username);
log.info("Order {} created with total amount {}", orderId, amount);

// WARN: Business rule violations
log.warn("Login failed for user: {}", username);
log.warn("Insufficient stock for product: {} (requested: {}, available: {})", 
    productName, requested, available);

// ERROR: System errors
log.error("Failed to send email to {}", email, exception);
log.error("Database connection failed", exception);

// DEBUG: Detailed information
log.debug("Processing cart item - Product ID: {}, Quantity: {}", productId, quantity);
log.debug("JWT token validation successful for user: {}", username);
```

## Maintenance

### Log File Management
- **Automatic rotation**: Configured in logback-spring.xml
- **Size monitoring**: Regular checks for disk usage
- **Archive strategy**: Compressed old logs for storage efficiency

### Configuration Updates
- **Environment-specific**: Use Spring profiles for different environments
- **Runtime changes**: Logback supports runtime configuration updates
- **Performance tuning**: Adjust levels and appenders as needed

## Troubleshooting

### Common Issues
- **Log files not created**: Check directory permissions
- **Performance impact**: Reduce log levels in production
- **Disk space**: Monitor log file sizes and retention

### Debug Mode
- **Enable DEBUG**: Set logging level to DEBUG for detailed information
- **Specific packages**: Target specific packages for debugging
- **Temporary logging**: Add temporary debug statements for investigation

This logging implementation provides comprehensive visibility into the Nova Hotel Backend application's operations while maintaining security and performance standards.