# Balance and Expense Retrieval Features

This document describes the new features added to retrieve user balance summaries and expenses between friends.

## New Features

### 1. User Balance Summary

Get a comprehensive overview of a user's financial position across all expenses.

#### Endpoints:
- `GET /api/expenses/balance/summary` - Get balance summary for authenticated user
- `GET /api/expenses/user/{userId}/balance/summary` - Get balance summary for specific user

#### Response Format:
```json
{
  "userId": 1,
  "userName": "John Doe",
  "totalOwed": 150.00,      // Amount others owe to this user
  "totalOwes": 75.50,       // Amount this user owes to others
  "netBalance": 74.50       // Net balance (positive = net owed to user)
}
```

### 2. Expenses Between Friends

Get detailed information about expenses shared between two specific friends, including balance calculations.

#### Endpoint:
- `GET /api/expenses/friend/{friendId}/expenses` - Get expenses and balance with specific friend

#### Response Format:
```json
{
  "friendId": 2,
  "friendName": "Jane Smith",
  "totalOwedToFriend": 25.00,    // Amount user owes to friend
  "totalOwedByFriend": 50.00,    // Amount friend owes to user
  "netBalance": 25.00,           // Net balance (positive = friend owes user)
  "sharedExpenses": [
    {
      "id": 1,
      "title": "Dinner",
      "amount": 100.00,
      "paidBy": 1,
      "participants": [...]
    }
  ]
}
```

### 3. Enhanced Group Expenses

Get all expenses for a specific group with detailed information.

#### Endpoint:
- `GET /api/expenses/group/{groupId}/all` - Get all expenses for a group

#### Response Format:
Returns a list of `ExpenseResponse` objects for all expenses in the group.

## Usage Examples

### Get Your Balance Summary
```bash
curl -X GET "http://localhost:8080/api/expenses/balance/summary" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Example Response:**
```json
{
  "userId": 1,
  "userName": "John Doe",
  "totalOwed": 150.00,      // Others owe John $150
  "totalOwes": 75.50,       // John owes others $75.50
  "netBalance": 74.50       // Net: John is owed $74.50
}
```

### Get Friend Balances
```bash
curl -X GET "http://localhost:8080/api/expenses/user/1/balances" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Example Response:**
```json
[
  {
    "friendId": 2,
    "friendName": "Samantha",
    "balance": -50.00       // John owes Samantha $50
  },
  {
    "friendId": 3,
    "friendName": "Sai",
    "balance": 60.00        // Sai owes John $60
  }
]
```

### Get Balance Summary for User ID 5
```bash
curl -X GET "http://localhost:8080/api/expenses/user/5/balance/summary" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### Get Expenses with Friend ID 3
```bash
curl -X GET "http://localhost:8080/api/expenses/friend/3/expenses" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### Get All Expenses for Group ID 2
```bash
curl -X GET "http://localhost:8080/api/expenses/group/2/all" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

## Business Logic

### Balance Calculation
- **totalOwed**: Sum of all amounts that others owe to the user (when user paid for expenses and others participated)
- **totalOwes**: Sum of all amounts that the user owes to others (when others paid for expenses and user participated)
- **netBalance**: totalOwed - totalOwes (positive means net amount owed to the user, negative means user owes others)

### Individual Friend Balance Calculation
- **Positive balance**: Friend owes money to the user
- **Negative balance**: User owes money to the friend
- **Zero balance**: No outstanding balance between user and friend

### Friend Expense Calculation
- Only considers expenses where both users are participants and the source is 'FRIEND'
- Calculates individual balances based on who paid for each expense
- Provides a complete list of shared expenses for transparency

### Group Expenses
- Retrieves all expenses where any participant has the source 'GROUP' and matches the group ID
- Orders expenses by creation date (most recent first)

## Security
- All endpoints require JWT authentication
- Users can only access their own balance summary or friend expenses
- Group expenses are accessible to any authenticated user (consider adding group membership validation if needed)

## Error Handling
- Returns 404 if user or friend not found
- Returns 401 if not authenticated
- Returns 403 if unauthorized access attempted
