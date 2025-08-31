# Friend Expenses Endpoint Fix

## Issue Description

The endpoint `GET /api/expenses/friend/{friendId}/expenses` was only returning expenses where both users participated with `source: "FRIEND"`, missing expenses where users participated through groups or mixed sources.

## Problem

The original query in `ExpenseRepository.findExpensesBetweenFriends()` was:
```java
@Query("SELECT DISTINCT e FROM Expense e " +
        "JOIN e.participants ep1 " +
        "JOIN e.participants ep2 " +
        "WHERE ep1.user.id = :userId1 AND ep2.user.id = :userId2 " +
        "AND ep1.source = 'FRIEND' AND ep2.source = 'FRIEND' " +  // This was the problem
        "ORDER BY e.createdAt DESC")
```

This filter excluded:
- Expenses where both users participated through groups (`source: "GROUP"`)
- Mixed expenses where users participated through different sources

## Solution

Removed the source filter to include ALL expenses where both users are participants:

```java
@Query("SELECT DISTINCT e FROM Expense e " +
        "JOIN e.participants ep1 " +
        "JOIN e.participants ep2 " +
        "WHERE ep1.user.id = :userId1 AND ep2.user.id = :userId2 " +
        "ORDER BY e.createdAt DESC")
```

## Expected Behavior

Now the endpoint will return all 4 expenses between users 1 and 2:

1. **Expense ID 1**: "Dinner at Restaurant" - FRIEND source
2. **Expense ID 2**: "Group Trip Expenses" - GROUP source  
3. **Expense ID 3**: "Mixed Expense" - Mixed sources (GROUP for both users)
4. **Expense ID 4**: "movie" - FRIEND source

## Files Modified

1. `src/main/java/com/splitbuddy/splitbuddy/repositories/ExpenseRepository.java`
   - Updated `findExpensesBetweenFriends()` query to remove source filter

2. `BALANCE_FEATURES.md`
   - Updated documentation to reflect that all expense sources are now included

## Testing

Use the existing test file: `src/main/java/com/splitbuddy/splitbuddy/http/expense-balance-test.http`

```http
### Get individual Expenses Between Friends
GET http://localhost:4321/api/expenses/friend/2/expenses
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwiaWF0IjoxNzU2NTMxMDMzLCJleHAiOjE3NjE3MTUwMzN9.6AYDaRVLGeI3edJUMAEn6Gtkr1lLD8bs6V8Y5XpCKYo
```

Expected result: Should now return 4 expenses instead of 2.
