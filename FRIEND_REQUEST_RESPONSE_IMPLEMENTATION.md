# Friend Request Response Implementation

## Overview
This document describes the implementation of the new friend request response format that matches the expected UI interface.

## UI Interface Expected
```typescript
export interface FriendRequest {
  id: number;
  senderName: string;
  senderEmail: string;
  createdAt: string;
  status?: FriendRequestStatus;
  sender?: User;
  receiver?: User;
}
```

## Implementation Details

### 1. New DTOs Created

#### FriendRequestResponse.java
- **Location**: `src/main/java/com/splitbuddy/splitbuddy/dto/response/FriendRequestResponse.java`
- **Purpose**: Main response DTO for friend requests
- **Fields**:
  - `id` (Long)
  - `senderName` (String)
  - `senderEmail` (String)
  - `createdAt` (String - formatted as ISO string)
  - `status` (FriendRequestStatus enum)
  - `sender` (UserResponse)
  - `receiver` (UserResponse)

#### UserResponse.java
- **Location**: `src/main/java/com/splitbuddy/splitbuddy/dto/response/UserResponse.java`
- **Purpose**: Simplified user DTO to avoid circular references
- **Fields**:
  - `id` (Long)
  - `name` (String)
  - `email` (String)

### 2. Updated Endpoints

#### POST /api/friends/requests
- **Previous**: Returned simple string message
- **Now**: Returns `FriendRequestResponse` object
- **Status**: 201 CREATED on success

#### PUT /api/friends/requests/{requestId}/accept
- **Previous**: Returned simple string message
- **Now**: Returns `FriendRequestResponse` object
- **Status**: 200 OK on success

#### PUT /api/friends/requests/{requestId}/reject
- **Previous**: Returned simple string message
- **Now**: Returns `FriendRequestResponse` object
- **Status**: 200 OK on success

#### PUT /api/friends/requests/{requestId}?response={ACCEPTED|REJECTED}
- **Previous**: Returned simple string message
- **Now**: Returns `FriendRequestResponse` object
- **Status**: 200 OK on success

#### GET /api/friends/{userId}/pending-requests
- **Previous**: Returned `List<PendingFriendRequestsResponseDto>`
- **Now**: Returns `List<FriendRequestResponse>`
- **Status**: 200 OK on success

#### GET /api/friends/requests/{requestId} (NEW)
- **Purpose**: Get a single friend request by ID
- **Returns**: `FriendRequestResponse` object
- **Status**: 200 OK on success

### 3. Service Layer Changes

#### FriendService.java
- **Updated Methods**:
  - `sendFriendRequest()`: Now returns `FriendRequestResponse`
  - `acceptFriendRequest()`: Now returns `FriendRequestResponse`
  - `rejectFriendRequest()`: Now returns `FriendRequestResponse`
  - `respondToFriendRequest()`: Now returns `FriendRequestResponse`
  - `getPendingRequests(Long userId)`: Now returns `List<FriendRequestResponse>`
  - `getPendingRequests(User receiver)`: Now returns `List<FriendRequestResponse>`
  - `getFriendRequest(Long requestId)`: NEW method

- **Helper Methods**:
  - `convertToUserResponse(User user)`: Converts User entity to UserResponse DTO
  - `convertToFriendRequestResponse(FriendRequest request)`: Converts FriendRequest entity to FriendRequestResponse DTO

### 4. Response Format Example

```json
{
  "id": 1,
  "senderName": "John Doe",
  "senderEmail": "john@example.com",
  "createdAt": "2024-01-15T10:30:00",
  "status": "PENDING",
  "sender": {
    "id": 1,
    "name": "John Doe",
    "email": "john@example.com"
  },
  "receiver": {
    "id": 2,
    "name": "Jane Smith",
    "email": "jane@example.com"
  }
}
```

### 5. Testing

A test HTTP file has been created at:
`src/main/java/com/splitbuddy/splitbuddy/http/friend-request-test.http`

This file contains test requests for all the friend request endpoints.

## Benefits

1. **Consistent API**: All friend request endpoints now return the same response format
2. **UI Compatibility**: Response format matches the expected TypeScript interface
3. **No Circular References**: Using UserResponse prevents JSON serialization issues
4. **Complete Information**: Includes all necessary fields for the UI
5. **Type Safety**: Proper DTOs ensure type safety and clear contracts

## Migration Notes

- The old `PendingFriendRequestsResponseDto` is still available but no longer used
- All existing endpoints maintain backward compatibility in terms of functionality
- New endpoints provide enhanced response data
