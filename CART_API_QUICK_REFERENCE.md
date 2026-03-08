# Cart API - Quick Reference Guide

## API Endpoints Summary

### 1. Add to Cart
```http
POST /orders/AddToCart
Content-Type: application/json
Authorization: Bearer {jwt_token}

{
  "productId": 1,
  "quantity": 5,
  "unitPrice": 99.99
}
```

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

---

### 2. Get Cart Items
```http
GET /orders/GetCartItem
Authorization: Bearer {jwt_token}
```

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
    }
  ]
}
```

---

### 3. Remove from Cart
```http
POST /orders/RemoveFromCart?productId=1
Authorization: Bearer {jwt_token}
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Product removed from cart successfully",
  "data": null
}
```

---

## Error Responses

### 400 Bad Request
```json
{
  "success": false,
  "message": "Product not found",
  "data": null
}
```

### 401 Unauthorized
```json
{
  "success": false,
  "message": "User not authenticated",
  "data": null
}
```

### 500 Internal Server Error
```json
{
  "success": false,
  "message": "An error occurred while adding product to cart",
  "data": null
}
```

---

## Implementation Details

### OrderItemService Methods

#### saveToCart(OrderItem orderItem)
```java
// Usage
OrderItem cartItem = new OrderItem();
cartItem.setUser(user);
cartItem.setProduct(product);
cartItem.setQuantity(5);
cartItem.setUnitPrice(new BigDecimal("99.99"));
cartItem.setOrder(null);

OrderItem saved = orderItemService.saveToCart(cartItem);
// Returns: OrderItem with id, totalPrice calculated, timestamps set
```

#### getCartItems(Long userId)
```java
// Usage
List<OrderItemDTO> cartItems = orderItemService.getCartItems(userId);
// Returns: List of all cart items (where order_id IS NULL)
```

#### removeFromCart(Long userId, Long productId)
```java
// Usage
orderItemService.removeFromCart(userId, productId);
// Removes cart item and returns void
// Throws: IllegalArgumentException if not found
```

#### getCartItemCount(Long userId)
```java
// Usage
long count = orderItemService.getCartItemCount(userId);
// Returns: Number of items in cart
```

#### clearCart(Long userId)
```java
// Usage
orderItemService.clearCart(userId);
// Removes all cart items for user
```

---

## OrderItemRepository Query Methods

### Get Cart Items
```sql
-- SQL Query Equivalent
SELECT oi FROM OrderItem oi 
WHERE oi.user.id = :userId AND oi.order IS NULL
```

### Find Specific Cart Item
```sql
-- SQL Query Equivalent
SELECT oi FROM OrderItem oi 
WHERE oi.user.id = :userId 
AND oi.product.id = :productId 
AND oi.order IS NULL
```

### Delete Cart Item
```sql
-- SQL Query Equivalent
DELETE FROM OrderItem oi 
WHERE oi.user.id = :userId 
AND oi.product.id = :productId 
AND oi.order IS NULL
```

---

## Integration Points

### With Authentication
- User is extracted from `Authentication` object
- All endpoints require active JWT token
- User isolation is enforced at service layer

### With Product Service
- ProductRepository used to fetch product details
- Validates product exists before creating cart item
- Preserves product price at time of cart addition

### With Order Service
- Existing checkout flow remains unchanged
- Cart items (order_id IS NULL) are converted during checkout
- totalPrice is pre-calculated in cart for efficiency

---

## Database Queries

### View User's Cart
```sql
SELECT * FROM order_items 
WHERE user_id = ? AND order_id IS NULL
ORDER BY created_at DESC;
```

### Count Cart Items
```sql
SELECT COUNT(*) FROM order_items 
WHERE user_id = ? AND order_id IS NULL;
```

### Calculate Cart Total
```sql
SELECT SUM(total_price) FROM order_items 
WHERE user_id = ? AND order_id IS NULL;
```

### Remove Cart Item
```sql
DELETE FROM order_items 
WHERE user_id = ? AND product_id = ? AND order_id IS NULL;
```

### Clear Entire Cart
```sql
DELETE FROM order_items 
WHERE user_id = ? AND order_id IS NULL;
```

---

## Common Scenarios

### Scenario 1: User adds first item to cart
```
1. User calls POST /orders/AddToCart with productId=1
2. OrderItemService validates and saves to database
3. Cart item created with order_id = NULL
4. Response includes cart item details with totalPrice
```

### Scenario 2: User updates quantity in cart
```
1. User removes old cart item (POST /orders/RemoveFromCart?productId=1)
2. User adds new cart item with updated quantity (POST /orders/AddToCart)
3. Simulates update through delete + insert
```

### Scenario 3: User views entire cart
```
1. User calls GET /orders/GetCartItem
2. Service retrieves all items where order_id IS NULL
3. Returns list with totals already calculated
4. Frontend can sum for grand total
```

### Scenario 4: User proceeds to checkout
```
1. User clicks Checkout
2. POST /orders/checkout is called with shipping address and notes (cart items fetched automatically)
3. System fetches user's cart items using GetCartItem logic
4. New Order is created with fetched cart items
5. Cart items get updated with order_id = {newOrderId}
6. Items are no longer in cart (order_id is NOT NULL)
```

### Scenario 5: User abandons cart
```
1. User leaves site without checkout
2. Cart items remain in database (order_id = NULL)
3. Next login, previous cart items still available
4. User can resume or clear cart
```

---

## Performance Considerations

### Indexes Recommended
```sql
-- Add index for cart queries
CREATE INDEX idx_order_items_user_order 
ON order_items(user_id, order_id);

-- Add index for product lookup
CREATE INDEX idx_order_items_product 
ON order_items(product_id, order_id);
```

### Query Performance
- `findCartItemsByUserId`: O(n) where n = items in user's cart
- `findCartItemByUserIdAndProductId`: O(1) with proper indexing
- `deleteCartItemByUserIdAndProductId`: O(1) with proper indexing

### Optimization Tips
1. Use pagination if users have many cart items (>100)
2. Cache cart totals if calculation is expensive
3. Consider lazy loading of Product details
4. Archive old carts periodically (optional)

---

## Validation Rules

### Add to Cart
- [ ] productId must exist in products table
- [ ] quantity >= 1
- [ ] unitPrice > 0
- [ ] User must be authenticated
- [ ] user_id must be valid

### Get Cart Items
- [ ] User must be authenticated
- [ ] Only returns items where order_id IS NULL
- [ ] Returns empty list if no cart items

### Remove from Cart
- [ ] User must be authenticated
- [ ] productId must exist in user's cart
- [ ] order_id must be NULL for item being removed

---

## Related APIs

### Updated Checkout API
```http
POST /orders/checkout
Content-Type: application/json
Authorization: Bearer {jwt_token}

{
  "shippingAddress": {
    "street": "123 Main St",
    "city": "City",
    "zipCode": "12345"
  },
  "notes": "Please deliver in morning"
}
```

**Note:** Cart items are automatically fetched from the user's cart using the same authentication context as the GetCartItem endpoint. No need to include `cartItems` in the request body.

### Get User Orders
```http
GET /orders?page=0&size=10&sortBy=createdAt&sortDir=desc
Authorization: Bearer {jwt_token}
```

### Get Order Details
```http
GET /orders/{orderId}
Authorization: Bearer {jwt_token}
```

---

## Troubleshooting

### Issue: "Cart item not found" when removing
- **Cause:** Product was not in user's cart (order_id wasn't NULL)
- **Solution:** Verify cart items first with GET /orders/GetCartItem

### Issue: Can't add product to cart
- **Cause:** Invalid productId or product doesn't exist
- **Solution:** Check product exists and is available

### Issue: Cart contains old data after logout/login
- **Cause:** This is expected behavior - cart persists across sessions
- **Solution:** Implement cart clearing on logout if desired

### Issue: Duplicate items in cart
- **Cause:** Adding same product twice creates separate cart items
- **Solution:** Implement update logic on frontend (remove + re-add)

---

## Future Enhancements

1. **Update Cart Item Quantity**
   - PATCH /orders/UpdateCart/{cartItemId}
   - Allow direct quantity updates

2. **Bulk Add to Cart**
   - POST /orders/AddToCartBulk
   - Add multiple items in one request

3. **Merge Carts**
   - POST /orders/MergeCart
   - Combine carts from different sessions/devices

4. **Save for Later**
   - Move items to wishlist instead of deleting

5. **Cart Expiration**
   - Auto-clear carts after X days of inactivity

6. **Cart Analytics**
   - GET /orders/CartAnalytics
   - Track abandoned carts, popular items

---
