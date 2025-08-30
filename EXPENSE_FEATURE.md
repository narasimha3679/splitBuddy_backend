# Expense Creation Feature

## Overview

The expense creation feature handles expense creation where the UI always includes the expense creator (payer) in the participants list. The backend validates all participants and skips friendship validation for the creator since a user cannot be friends with themselves.

## How It Works

### Current Implementation

1. **Participant Processing**: All participants from the request are processed and validated
2. **Creator Validation Skip**: When the participant is the same as the payer, friendship/group validation is skipped
3. **Amount Validation**: Total participant amounts must equal the expense amount
4. **Source Validation**: For other participants, validates friendship or group membership

### API Endpoint

```
POST /api/expenses
Content-Type: application/json
Authorization: Bearer <token>
```

### Request Body

```json
{
  "title": "Dinner at Restaurant",
  "description": "Group dinner with friends",
  "amount": 100.00,
  "currency": "USD",
  "category": "Food & Dining",
  "paidBy": "b366f233-d035-4d0c-a6d5-222b057e78c3",
  "paidAt": "2024-01-15T19:30:00Z",
  "participants": [
    {
      "userId": "b366f233-d035-4d0c-a6d5-222b057e78c3",
      "amount": 20.00,
      "source": "FRIEND",
      "sourceId": null
    },
    {
      "userId": "2e669c2a-301a-4dbf-a716-21e37f3098d5",
      "amount": 50.00,
      "source": "FRIEND",
      "sourceId": null
    },
    {
      "userId": "3f779d3b-412b-5ecf-b827-32f48f4209e6",
      "amount": 30.00,
      "source": "FRIEND",
      "sourceId": null
    }
  ]
}
```

## Scenarios

### Scenario 1: Uneven split with creator included

**Request**: Payer pays $100, includes themselves with $20, others pay $50 + $30
**Result**: Works correctly, creator validation skipped

### Scenario 2: Equal split with creator included

**Request**: Payer pays $80, includes themselves with $40, other pays $40
**Result**: Works correctly, creator validation skipped

### Scenario 3: Group expenses with creator included

**Request**: Group expense where creator is included in participants list
**Result**: Works correctly, creator validation skipped

## Benefits

1. **Accurate Balance Tracking**: Creator's share is always recorded
2. **UI Control**: UI has full control over participant amounts and splits
3. **Consistent Data**: All expenses have complete participant information
4. **Better UX**: Clear and predictable behavior

## Validation Rules

1. **Amount Validation**: Total participant amounts must equal expense amount
2. **User Existence**: All participants must be valid users
3. **Friendship/Group Validation**: Participants (except creator) must be friends or group members
4. **Creator Validation Skip**: Creator's friendship/group validation is skipped

## Error Handling

- `InvalidOperationException`: When total participant amounts don't match expense amount
- `UserNotFoundException`: When payer or participant users don't exist
- `InvalidOperationException`: When participants (except creator) are not friends or group members

## Testing

Use the provided test cases in `expense-test.http` to verify the functionality:

1. Test with creator included in participants (uneven split)
2. Test with creator included in participants (equal split)
3. Test with group participants (creator included)
