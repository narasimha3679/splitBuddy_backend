# SplitBuddy API Documentation

Complete API reference for the SplitBuddy backend service.

**Base URL**: `http://localhost:420/api` (local development)

**Authentication**: Most endpoints require a Bearer token in the Authorization header:
```
Authorization: Bearer <jwt_token>
```

## Table of Contents

1. [Authentication](#authentication)
2. [Users](#users)
3. [Friends](#friends)
4. [Groups](#groups)
5. [Expenses](#expenses)
6. [Balances](#balances)
7. [Error Responses](#error-responses)

---

## Authentication

### POST /api/auth/login

Authenticate a user and receive a JWT token.

**Request Body:**
```json
{
  "email": "user@example.com",
  "password": "password123"
}
```

**Response:** `200 OK`
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "message": "Login successful"
}
```

**Validation Rules:**
- `email`: Required, must be valid email format
- `password`: Required, minimum 6 characters

---

### POST /api/auth/register

Register a new user account.

**Request Body:**
```json
{
  "email": "user@example.com",
  "password": "password123",
  "name": "John Doe"
}
```

**Response:** `200 OK`
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "message": "Registration successful"
}
```

**Validation Rules:**
- `email`: Required, must be valid email format
- `password`: Required, minimum 6 characters
- `name`: Required, 2-50 characters

---

### GET /api/auth/me

Get current authenticated user information.

**Headers:**
```
Authorization: Bearer <token>
```

**Response:** `200 OK`
```json
{
  "id": "1",
  "name": "John Doe",
  "email": "user@example.com"
}
```

---

### GET /api/auth/test

Test endpoint to verify CORS and connectivity.

**Response:** `200 OK`
```
"Auth controller is working! CORS should be working now."
```

---

## Users

### GET /api/users/search

Search for a user by email address.

**Headers:**
```
Authorization: Bearer <token>
```

**Query Parameters:**
- `email` (required): User's email address

**Response:** `200 OK`
```json
{
  "id": "1",
  "name": "Jane Doe",
  "email": "jane@example.com"
}
```

**Error:** `404 Not Found` if user doesn't exist

---

### GET /api/users

Get all users (admin endpoint).

**Headers:**
```
Authorization: Bearer <token>
```

**Response:** `200 OK`
```json
[
  {
    "id": "1",
    "name": "John Doe",
    "email": "john@example.com"
  }
]
```

---

### POST /api/users

Create a new user (admin endpoint).

**Headers:**
```
Authorization: Bearer <token>
```

**Request Body:**
```json
{
  "name": "John Doe",
  "email": "john@example.com",
  "password": "password123"
}
```

**Response:** `200 OK`
```json
{
  "id": "1",
  "name": "John Doe",
  "email": "john@example.com"
}
```

---

## Friends

### POST /api/friends/requests

Send a friend request to another user.

**Headers:**
```
Authorization: Bearer <token>
```

**Request Body:**
```json
{
  "senderId": 1,
  "receiverId": 2,
  "message": "Optional message"
}
```

**Response:** `201 Created`
```json
{
  "id": 1,
  "senderName": "John Doe",
  "senderEmail": "john@example.com",
  "createdAt": "2024-01-15T10:00:00",
  "status": "PENDING",
  "sender": {
    "id": 1,
    "name": "John Doe",
    "email": "john@example.com"
  },
  "receiver": {
    "id": 2,
    "name": "Jane Doe",
    "email": "jane@example.com"
  }
}
```

**Validation Rules:**
- `senderId`: Required, must be valid user ID
- `receiverId`: Required, must be valid user ID
- `message`: Optional

---

### PUT /api/friends/requests/{requestId}

Respond to a friend request (accept or reject).

**Headers:**
```
Authorization: Bearer <token>
```

**Query Parameters:**
- `response` (required): "ACCEPTED" or "REJECTED"

**Response:** `200 OK`
```json
{
  "id": 1,
  "senderName": "John Doe",
  "senderEmail": "john@example.com",
  "createdAt": "2024-01-15T10:00:00",
  "status": "ACCEPTED"
}
```

---

### PUT /api/friends/requests/{requestId}/accept

Accept a friend request (convenience endpoint).

**Headers:**
```
Authorization: Bearer <token>
```

**Response:** `200 OK`
```json
{
  "id": 1,
  "senderName": "John Doe",
  "senderEmail": "john@example.com",
  "createdAt": "2024-01-15T10:00:00",
  "status": "ACCEPTED"
}
```

---

### PUT /api/friends/requests/{requestId}/reject

Reject a friend request (convenience endpoint).

**Headers:**
```
Authorization: Bearer <token>
```

**Response:** `200 OK`
```json
{
  "id": 1,
  "senderName": "John Doe",
  "senderEmail": "john@example.com",
  "createdAt": "2024-01-15T10:00:00",
  "status": "REJECTED"
}
```

---

### GET /api/friends/{userId}

Get all friends for a user.

**Headers:**
```
Authorization: Bearer <token>
```

**Response:** `200 OK`
```json
[
  {
    "id": 2,
    "name": "Jane Doe",
    "email": "jane@example.com"
  }
]
```

---

### GET /api/friends/{userId}/pending-requests

Get pending friend requests for a user.

**Headers:**
```
Authorization: Bearer <token>
```

**Response:** `200 OK`
```json
[
  {
    "id": 1,
    "senderName": "John Doe",
    "senderEmail": "john@example.com",
    "createdAt": "2024-01-15T10:00:00",
    "status": "PENDING"
  }
]
```

---

### GET /api/friends/requests/{requestId}

Get a specific friend request by ID.

**Headers:**
```
Authorization: Bearer <token>
```

**Response:** `200 OK`
```json
{
  "id": 1,
  "senderName": "John Doe",
  "senderEmail": "john@example.com",
  "createdAt": "2024-01-15T10:00:00",
  "status": "PENDING"
}
```

---

## Groups

### POST /api/groups

Create a new group.

**Headers:**
```
Authorization: Bearer <token>
```

**Request Body:**
```json
{
  "name": "Weekend Trip",
  "memberIds": ["1", "2", "3"]
}
```

**Response:** `200 OK`
```json
{
  "id": 1,
  "name": "Weekend Trip",
  "createdBy": 1,
  "createdByName": "John Doe",
  "members": [
    {
      "id": 1,
      "name": "John Doe",
      "email": "john@example.com"
    },
    {
      "id": 2,
      "name": "Jane Doe",
      "email": "jane@example.com"
    }
  ]
}
```

**Validation Rules:**
- `name`: Required, 1-100 characters
- `memberIds`: Optional array of user IDs (as strings)

---

### GET /api/groups/me

Get all groups for the current authenticated user.

**Headers:**
```
Authorization: Bearer <token>
```

**Response:** `200 OK`
```json
[
  {
    "id": 1,
    "name": "Weekend Trip",
    "createdBy": 1,
    "createdByName": "John Doe",
    "members": [...]
  }
]
```

---

### GET /api/groups

Get all groups (admin endpoint).

**Headers:**
```
Authorization: Bearer <token>
```

**Response:** `200 OK`
```json
[
  {
    "id": 1,
    "name": "Weekend Trip",
    "createdBy": 1,
    "createdByName": "John Doe",
    "members": [...]
  }
]
```

---

### DELETE /api/groups/{id}

Delete a group.

**Headers:**
```
Authorization: Bearer <token>
```

**Response:** `204 No Content`

---

## Expenses

### POST /api/expenses

Create a new expense.

**Headers:**
```
Authorization: Bearer <token>
```

**Request Body:**
```json
{
  "title": "Dinner at Restaurant",
  "description": "Group dinner with friends",
  "amount": 150.00,
  "currency": "USD",
  "category": "Food & Dining",
  "paidAt": "2024-01-15T19:30:00",
  "paidBy": 1,
  "participants": [
    {
      "userId": 2,
      "amount": 50.00,
      "source": "FRIEND",
      "sourceId": 1
    },
    {
      "userId": 3,
      "amount": 50.00,
      "source": "GROUP",
      "sourceId": 1
    }
  ]
}
```

**Response:** `201 Created`
```json
{
  "id": 1,
  "title": "Dinner at Restaurant",
  "description": "Group dinner with friends",
  "amount": 150.00,
  "currency": "USD",
  "category": "Food & Dining",
  "paidAt": "2024-01-15T19:30:00",
  "createdAt": "2024-01-15T20:00:00",
  "updatedAt": "2024-01-15T20:00:00",
  "paidBy": 1,
  "participants": [
    {
      "userId": 2,
      "userName": "Jane Doe",
      "amount": 50.00,
      "source": "FRIEND",
      "sourceId": 1,
      "isActive": true
    }
  ]
}
```

---

### GET /api/expenses/{expenseId}

Get expense details by ID.

**Headers:**
```
Authorization: Bearer <token>
```

**Response:** `200 OK`
```json
{
  "id": 1,
  "title": "Dinner at Restaurant",
  "description": "Group dinner with friends",
  "amount": 150.00,
  "currency": "USD",
  "category": "Food & Dining",
  "paidAt": "2024-01-15T19:30:00",
  "paidBy": 1,
  "participants": [...]
}
```

---

### GET /api/expenses

Get all expenses for the current authenticated user.

**Headers:**
```
Authorization: Bearer <token>
```

**Response:** `200 OK`
```json
[
  {
    "id": 1,
    "title": "Dinner at Restaurant",
    "amount": 150.00,
    "category": "Food & Dining",
    "paidBy": 1,
    "participants": [...]
  }
]
```

---

### GET /api/expenses/group/{groupId}

Get all expenses for a specific group.

**Headers:**
```
Authorization: Bearer <token>
```

**Response:** `200 OK`
```json
[
  {
    "id": 1,
    "title": "Dinner at Restaurant",
    "amount": 150.00,
    "category": "Food & Dining",
    "paidBy": 1,
    "participants": [...]
  }
]
```

---

### GET /api/expenses/friend/{friendId}/expenses

Get expenses between the current user and a specific friend.

**Headers:**
```
Authorization: Bearer <token>
```

**Response:** `200 OK`
```json
{
  "friendId": 2,
  "friendName": "Jane Doe",
  "totalOwedToFriend": 50.00,
  "totalOwedByFriend": 30.00,
  "netBalance": -20.00,
  "sharedExpenses": [...]
}
```

---

### GET /api/expenses/balance/summary

Get balance summary for the current authenticated user.

**Headers:**
```
Authorization: Bearer <token>
```

**Response:** `200 OK`
```json
{
  "userId": 1,
  "userName": "John Doe",
  "totalOwed": 250.00,
  "totalOwes": 180.00,
  "netBalance": 70.00
}
```

---

### GET /api/expenses/user/{userId}/balance/summary

Get balance summary for a specific user.

**Headers:**
```
Authorization: Bearer <token>
```

**Response:** `200 OK`
```json
{
  "userId": 1,
  "userName": "John Doe",
  "totalOwed": 250.00,
  "totalOwes": 180.00,
  "netBalance": 70.00
}
```

---

## Balances

### GET /api/balances/summary

Get balance summary for the current authenticated user.

**Headers:**
```
Authorization: Bearer <token>
```

**Response:** `200 OK`
```json
{
  "userId": 1,
  "userName": "John Doe",
  "totalOwed": 250.00,
  "totalOwes": 180.00,
  "netBalance": 70.00
}
```

---

### GET /api/balances/friends

Get friend balances for the current authenticated user.

**Headers:**
```
Authorization: Bearer <token>
```

**Response:** `200 OK`
```json
[
  {
    "friendId": 2,
    "friendName": "Jane Doe",
    "balance": -50.00
  }
]
```

**Note:** Positive balance means friend owes you, negative means you owe friend.

---

### GET /api/balances/friend/{friendId}/expenses

Get detailed expenses between current user and a friend.

**Headers:**
```
Authorization: Bearer <token>
```

**Response:** `200 OK`
```json
{
  "friendId": 2,
  "friendName": "Jane Doe",
  "totalOwedToFriend": 50.00,
  "totalOwedByFriend": 30.00,
  "netBalance": -20.00,
  "sharedExpenses": [...]
}
```

---

### GET /api/balances/groups

Get group balances for the current authenticated user.

**Headers:**
```
Authorization: Bearer <token>
```

**Response:** `200 OK`
```json
[
  {
    "groupId": 1,
    "groupName": "Weekend Trip",
    "userId": 1,
    "userName": "John Doe",
    "balance": -100.00
  }
]
```

**Note:** Positive balance means group owes you, negative means you owe the group.

---

### GET /api/balances/group/{groupId}/balances

Get all member balances for a specific group.

**Headers:**
```
Authorization: Bearer <token>
```

**Response:** `200 OK`
```json
[
  {
    "groupId": 1,
    "groupName": "Weekend Trip",
    "userId": 1,
    "userName": "John Doe",
    "balance": -100.00
  },
  {
    "groupId": 1,
    "groupName": "Weekend Trip",
    "userId": 2,
    "userName": "Jane Doe",
    "balance": 50.00
  }
]
```

---

### POST /api/balances/recalculate

Recalculate all balances in the system (admin/maintenance endpoint).

**Headers:**
```
Authorization: Bearer <token>
```

**Response:** `200 OK`
```
"Balance recalculation completed successfully"
```

---

## Error Responses

All error responses follow a consistent format:

### 400 Bad Request
```json
{
  "message": "Validation failed",
  "errors": {
    "email": "Email is required",
    "password": "Password must be at least 6 characters"
  }
}
```

### 401 Unauthorized
```json
{
  "message": "Authentication required"
}
```

### 403 Forbidden
```json
{
  "message": "You don't have permission to access this resource"
}
```

### 404 Not Found
```json
{
  "message": "Resource not found"
}
```

### 500 Internal Server Error
```json
{
  "message": "An internal server error occurred"
}
```

---

## Data Types

### Date/Time Format
All date/time fields use ISO 8601 format: `YYYY-MM-DDTHH:mm:ss`

Example: `2024-01-15T19:30:00`

### Currency
All monetary amounts are represented as `BigDecimal` (decimal numbers) with 2 decimal places.

### Participant Source
- `FRIEND`: Expense participant added via friend relationship
- `GROUP`: Expense participant added via group membership

### Friend Request Status
- `PENDING`: Request has been sent but not yet responded to
- `ACCEPTED`: Request has been accepted
- `REJECTED`: Request has been rejected
- `CANCELLED`: Request has been cancelled

---

## Rate Limiting

Currently, there are no rate limits implemented. This may change in future versions.

---

## Versioning

Current API version: **v1** (implicit)

All endpoints are under `/api/` prefix. Future versions may use `/api/v2/` etc.

---

## Support

For API support or questions, please refer to:
- Backend repository: `backend/`
- API Contract: `backend/openapi.yaml`
- TypeScript Types: `expo/splitbuddy/src/types/api-contracts.ts`
