# API Error Handling Improvements

## Overview
This document outlines the improvements made to the SplitBuddy API error handling to ensure consistent HTTP status codes and meaningful error messages.

## Changes Made

### 1. Custom Exceptions Created
- `UserNotFoundException` - 404 Not Found
- `GroupNotFoundException` - 404 Not Found  
- `FriendRequestNotFoundException` - 404 Not Found
- `InvalidCredentialsException` - 401 Unauthorized
- `DuplicateResourceException` - 409 Conflict
- `InvalidOperationException` - 400 Bad Request

### 2. Global Exception Handler
Created `GlobalExceptionHandler` class with `@RestControllerAdvice` to:
- Standardize error response format
- Map exceptions to appropriate HTTP status codes
- Provide consistent error message structure
- Handle validation errors
- Handle generic exceptions

### 3. Error Response Format
All error responses now follow this consistent format:
```json
{
  "status": 404,
  "error": "User not found",
  "message": "User not found with ID: 123",
  "timestamp": "2024-01-15T10:30:00",
  "details": {
    "field": "error message"
  }
}
```

### 4. Controller Updates
All controllers now:
- Return `ResponseEntity<T>` instead of raw objects
- Use proper HTTP status codes:
  - 200 OK for successful GET/POST operations
  - 204 No Content for successful DELETE operations
  - Let exceptions handle error cases automatically

### 5. Service Layer Updates
Services now:
- Throw appropriate custom exceptions instead of returning error messages
- Use meaningful exception messages
- Handle edge cases properly

### 6. Input Validation
Added validation annotations to DTOs:
- `@NotBlank`, `@Email`, `@Size` for string validation
- `@NotNull` for required fields
- `@Valid` annotation on controller methods

## HTTP Status Codes Used

- **200 OK** - Successful operations
- **201 Created** - Resource created successfully
- **204 No Content** - Successful deletion
- **400 Bad Request** - Invalid input data or operation
- **401 Unauthorized** - Invalid credentials
- **404 Not Found** - Resource not found
- **409 Conflict** - Resource already exists
- **422 Unprocessable Entity** - Validation errors
- **500 Internal Server Error** - Unexpected server errors

## Example Error Responses

### User Not Found (404)
```json
{
  "status": 404,
  "error": "User not found",
  "message": "User not found with ID: 123e4567-e89b-12d3-a456-426614174000",
  "timestamp": "2024-01-15T10:30:00"
}
```

### Validation Error (400)
```json
{
  "status": 400,
  "error": "Validation failed",
  "message": "Invalid input data",
  "timestamp": "2024-01-15T10:30:00",
  "details": {
    "email": "Email should be valid",
    "password": "Password must be at least 6 characters long"
  }
}
```

### Duplicate Resource (409)
```json
{
  "status": 409,
  "error": "Resource already exists",
  "message": "Email already in use",
  "timestamp": "2024-01-15T10:30:00"
}
```

## Benefits

1. **Consistent Error Handling** - All endpoints now return appropriate status codes
2. **Better Client Experience** - Clear error messages help clients handle errors properly
3. **Easier Debugging** - Structured error responses with timestamps
4. **Input Validation** - Automatic validation with clear error messages
5. **Maintainable Code** - Centralized exception handling reduces code duplication

## Testing

The improvements include test cases to verify:
- Successful operations return 200/204 status codes
- Error cases return appropriate error status codes
- Validation errors are properly handled
- Exception handling works as expected
