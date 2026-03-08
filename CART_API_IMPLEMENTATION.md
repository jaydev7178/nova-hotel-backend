# Cart API Implementation Summary

## Overview
Implemented three new cart management APIs and enhanced the OrderItem-related classes to support cart functionality. All cart items are stored in the `order_items` table with `order_id = NULL`.

---

## Files Modified

### 1. **OrderItemRepository.java**
Added three new query methods for cart operations:

```java
// Get all cart items for a user (where order is null)
@Query("SELECT oi FROM OrderItem oi WHERE oi.user.id = :userId AND oi.order IS NULL")
List<OrderItem> findCartItemsByUserId(@Param("userId") Long userId);

// Find specific cart item by user and product
@Query("SELECT oi FROM OrderItem oi WHERE oi.user.id = :userId AND oi.product.id = :productId AND oi.order IS NULL")
List<OrderItem> findCartItemByUserIdAndProductId(@Param("userId") Long userId, @Param("productId") Long productId);

// Delete cart item by user and product
@Query("DELETE FROM OrderItem oi WHERE oi.user.id = :userId AND oi.product.id = :productId AND oi.order IS NULL")
void deleteCartItemByUserIdAndProductId(@Param("userId") Long userId, @Param("productId") Long productId);
```

---

### 2. **OrderItemDTO.java**
Added `orderId` field to include order information:

```java
@Data
public class OrderItemDTO {
    private Long id;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalPrice;
    private Long productId;
    private Long userId;
    private Long orderId;  // NEW FIELD
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

---

### 3. **OrderItemService.java**
Added comprehensive cart management methods:

#### a. `saveToCart(OrderItem orderItem)`
- Saves an OrderItem as a cart item (order is null)
- Validates all required fields (user, product, quantity, unitPrice)
- Automatically calculates totalPrice = unitPrice × quantity
- Ensures order field is null

**Parameters:**
- `orderItem`: OrderItem entity to save

**Returns:** Saved OrderItem

**Throws:** IllegalArgumentException if validation fails

#### b. `getCartItems(Long userId)`
- Retrieves all cart items for a specific user
- Returns items where order_id is NULL

**Parameters:**
- `userId`: ID of the user

**Returns:** List of OrderItemDTO objects

#### c. `removeFromCart(Long userId, Long productId)`
- Removes a product from user's cart
- Deletes cart items where order_id is NULL

**Parameters:**
- `userId`: ID of the user
- `productId`: ID of the product to remove

**Throws:** IllegalArgumentException if cart item not found

#### d. `getCartItemCount(Long userId)`
- Returns the number of items in user's cart

**Parameters:**
- `userId`: ID of the user

**Returns:** Count of cart items

#### e. `clearCart(Long userId)`
- Clears all cart items for a user

**Parameters:**
- `userId`: ID of the user

---

### 4. **OrderController.java**
Added three new REST endpoints:

#### a. POST `/orders/AddToCart`
**Summary:** Add product to cart

**Request Body:**
```json
{
  "productId": 1,
  "quantity": 5,
  "unitPrice": 99.99
}
```

**Parameters:**
- `OrderItemDTO request`: Cart item details
- `Authentication authentication`: Authenticated user context

**Response (201 Created):**
```json
{
  "success": true,
  "message": "Product added to cart successfully",
  "data": {
    "id": 1,
    "productId": 1,
    "quantity": 5,
    "unitPrice": 99.99,
    "totalPrice": 499.95,
    "userId": 10,
    "createdAt": "2026-01-13T10:30:00",
    "updatedAt": "2026-01-13T10:30:00"
  }
}
```

**Error Handling:**
- 400 Bad Request: If product not found or validation fails
- 500 Internal Server Error: Unexpected errors

**Business Logic:**
1. Extracts user from authentication context
2. Fetches product from database
3. Creates OrderItem with null order (cart item)
4. Calculates total price automatically
5. Saves to database using OrderItemService

---

#### b. GET `/orders/GetCartItem`
**Summary:** Retrieve all cart items for authenticated user

**Parameters:**
- `Authentication authentication`: Authenticated user context

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Cart items retrieved successfully",
  "data": [
    {
      "id": 1,
      "productId": 1,
      "quantity": 5,
      "unitPrice": 99.99,
      "totalPrice": 499.95,
      "userId": 10,
      "orderId": null,
      "createdAt": "2026-01-13T10:30:00",
      "updatedAt": "2026-01-13T10:30:00"
    },
    {
      "id": 2,
      "productId": 2,
      "quantity": 3,
      "unitPrice": 49.99,
      "totalPrice": 149.97,
      "userId": 10,
      "orderId": null,
      "createdAt": "2026-01-13T10:35:00",
      "updatedAt": "2026-01-13T10:35:00"
    }
  ]
}
```

**Error Handling:**
- 500 Internal Server Error: Unexpected errors

**Business Logic:**
1. Extracts user from authentication context
2. Retrieves all items where order_id is NULL and userId matches
3. Returns as OrderItemDTO list

---

#### c. POST `/orders/RemoveFromCart`
**Summary:** Remove product from cart

**Query Parameters:**
- `productId` (required): ID of product to remove

**Parameters:**
- `Authentication authentication`: Authenticated user context

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Product removed from cart successfully",
  "data": null
}
```

**Error Handling:**
- 400 Bad Request: If cart item not found
- 500 Internal Server Error: Unexpected errors

**Business Logic:**
1. Extracts user from authentication context
2. Removes cart item where order_id is NULL, userId matches, and productId matches
3. Returns success response

---

## OrderItem Entity Review

**Current State (No changes needed):**
- ✅ Properly designed with User, Product, and Order relationships
- ✅ `order_id` column allows NULL values (for cart items)
- ✅ Automatic timestamp management (createdAt, updatedAt)
- ✅ PrePersist hook calculates totalPrice automatically
- ✅ Proper validation annotations
- ✅ Uses JsonBackReference to prevent circular serialization

**Key Fields:**
- `id`: Primary key
- `user_id`: Foreign key to User (required)
- `order_id`: Foreign key to Order (nullable - used for cart)
- `product_id`: Foreign key to Product (required)
- `quantity`: Item quantity (minimum 1)
- `unitPrice`: Price per unit
- `totalPrice`: Total price (quantity × unitPrice)
- `createdAt`: Timestamp of creation
- `updatedAt`: Timestamp of last update

---

## Database Schema
The cart functionality leverages the existing `order_items` table:

```sql
-- Cart items have order_id = NULL
SELECT * FROM order_items WHERE order_id IS NULL AND user_id = ?;

-- Items transition to order when checkout happens
UPDATE order_items SET order_id = ? WHERE id = ? AND order_id IS NULL;
```

---

## API Usage Flow

### Adding to Cart
```
POST /orders/AddToCart
Body: {productId: 1, quantity: 5, unitPrice: 99.99}
↓
OrderItemService.saveToCart()
↓
Database: INSERT INTO order_items (user_id, product_id, quantity, unit_price, total_price, order_id) 
         VALUES (userId, 1, 5, 99.99, 499.95, NULL)
↓
Response: OrderItemDTO with cart item details
```

### Viewing Cart
```
GET /orders/GetCartItem
↓
OrderItemService.getCartItems(userId)
↓
Database: SELECT * FROM order_items WHERE user_id = ? AND order_id IS NULL
↓
Response: List<OrderItemDTO> with all cart items
```

### Removing from Cart
```
POST /orders/RemoveFromCart?productId=1
↓
OrderItemService.removeFromCart(userId, productId)
↓
Database: DELETE FROM order_items WHERE user_id = ? AND product_id = ? AND order_id IS NULL
↓
Response: Success message
```

---

## Integration with Existing Checkout Flow

The existing `checkout` endpoint already handles converting cart items to order items:
1. Cart items (order_id = NULL) are retrieved for the user
2. A new Order is created
3. Cart items are associated with the order (order_id is updated)
4. Order status is set to PENDING

---

## Validation & Error Handling

### AddToCart Validation
- Product exists in database
- Quantity is at least 1
- User is authenticated
- Unit price is provided

### GetCartItem
- User is authenticated
- Returns empty list if no cart items exist

### RemoveFromCart
- User is authenticated
- Cart item exists (product_id in user's cart)
- Throws IllegalArgumentException if not found

---

## Security Considerations

✅ **Authentication Required:** All three endpoints require authentication
✅ **User Isolation:** Each user can only access their own cart
✅ **Authorization:** Users cannot remove others' cart items

---

## Testing Recommendations

### Unit Tests (OrderItemService)
```java
testSaveToCart_ValidItem_Success()
testSaveToCart_NullUser_ThrowsException()
testSaveToCart_NullProduct_ThrowsException()
testGetCartItems_ReturnsUserCartOnly()
testRemoveFromCart_Success()
testRemoveFromCart_ItemNotFound_ThrowsException()
testClearCart_RemovesAllItems()
```

### Integration Tests (OrderController)
```java
testAddToCart_ValidRequest_Returns201()
testGetCartItem_AuthenticatedUser_ReturnsCart()
testRemoveFromCart_ValidProductId_Returns200()
testAddToCart_InvalidProductId_Returns400()
testRemoveFromCart_NotInCart_Returns400()
```

### API Tests (Postman/cURL)
```bash
# Add to cart
curl -X POST http://localhost:8080/orders/AddToCart \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{"productId": 1, "quantity": 5, "unitPrice": 99.99}'

# Get cart
curl -X GET http://localhost:8080/orders/GetCartItem \
  -H "Authorization: Bearer {token}"

# Remove from cart
curl -X POST http://localhost:8080/orders/RemoveFromCart?productId=1 \
  -H "Authorization: Bearer {token}"
```

---

## Build Status
✅ **Build Successful** - All code compiles without errors

---

## Summary of Changes

| Component | Changes |
|-----------|---------|
| OrderItemRepository | Added 3 query methods for cart operations |
| OrderItemDTO | Added orderId field |
| OrderItemService | Added 5 public methods for cart management |
| OrderController | Added 3 REST endpoints for cart operations |
| OrderItem Entity | No changes needed - already supports null order_id |

**Total Lines Added:** ~200+ lines across 4 files
**Build Status:** ✅ SUCCESS
