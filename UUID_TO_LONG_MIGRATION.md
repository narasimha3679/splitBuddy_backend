# UUID to Long ID Migration Guide

This guide explains how to migrate your SplitBuddy application from UUID-based IDs to Long-based IDs.

## Overview

The migration changes all primary keys and foreign keys from UUID to Long (BIGINT) type. This provides:
- Better performance for indexing and joins
- Smaller storage size
- Easier debugging and readability
- Better compatibility with auto-increment sequences

## Migration Steps

### 1. Backup Your Database
Before starting the migration, create a complete backup of your database:

```bash
pg_dump -h localhost -U postgres -d splitbuddy_db > splitbuddy_backup_$(date +%Y%m%d_%H%M%S).sql
```

### 2. Run the Database Migration Script
Execute the migration script on your PostgreSQL database:

```bash
psql -h localhost -U postgres -d splitbuddy_db -f database_migration_uuid_to_long.sql
```

**Important**: This script will:
- Create new BIGINT columns
- Migrate all existing data
- Preserve all relationships
- Update foreign key constraints
- Create performance indexes

### 3. Update Application Code
The application code has been updated to use Long instead of UUID. Key changes include:

#### Model Classes
- All `@Id` fields changed from `UUID` to `Long`
- `@GeneratedValue(strategy = GenerationType.UUID)` changed to `@GeneratedValue(strategy = GenerationType.IDENTITY)`

#### Repository Interfaces
- All repository interfaces updated to use `Long` instead of `UUID`
- Method signatures updated accordingly

#### Service Classes
- All service methods updated to use `Long` parameters
- UUID parsing logic replaced with Long parsing

#### DTOs
- Request/Response DTOs updated to use `Long` for ID fields
- String-based ID handling maintained for API compatibility

#### Controllers
- Controller methods updated to use `Long` path variables
- Response mapping updated

### 4. Update Application Configuration
Ensure your `application.yml` has the correct database configuration:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/splitbuddy_db
    username: postgres
    password: postgres
  jpa:
    hibernate:
      ddl-auto: validate  # Change from 'update' to 'validate' after migration
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
```

### 5. Test the Migration
After completing the migration:

1. Start your application
2. Test all API endpoints
3. Verify data integrity
4. Check that all relationships are preserved

## Rollback Plan

If you need to rollback the migration:

1. Restore from your database backup
2. Revert the code changes to use UUID
3. Update application configuration

## Performance Benefits

After migration, you should see:
- Faster query execution
- Reduced storage usage
- Better index performance
- Improved join operations

## API Compatibility

The API endpoints remain the same, but IDs are now returned as Long values instead of UUID strings. Frontend applications may need to be updated to handle the new ID format.

## Troubleshooting

### Common Issues

1. **Sequence Errors**: If you encounter sequence-related errors, run:
   ```sql
   SELECT setval('users_id_seq', (SELECT MAX(id) FROM users));
   ```

2. **Foreign Key Violations**: Ensure all foreign key relationships are properly migrated by checking the migration script output.

3. **Application Startup Errors**: Verify that all repository methods are properly updated to use Long parameters.

### Verification Queries

Run these queries to verify the migration:

```sql
-- Check table structures
\d users
\d expenses
\d groups
\d friend_requests
\d friendships
\d expense_participants

-- Check data counts
SELECT COUNT(*) FROM users;
SELECT COUNT(*) FROM expenses;
SELECT COUNT(*) FROM groups;
SELECT COUNT(*) FROM friend_requests;
SELECT COUNT(*) FROM friendships;
SELECT COUNT(*) FROM expense_participants;

-- Check foreign key relationships
SELECT COUNT(*) FROM expenses e JOIN users u ON e.paid_by = u.id;
SELECT COUNT(*) FROM expense_participants ep JOIN expenses e ON ep.expense_id = e.id;
```

## Support

If you encounter issues during migration:
1. Check the application logs for detailed error messages
2. Verify database connectivity and permissions
3. Ensure all code changes are properly applied
4. Test with a small dataset first

## Future Considerations

- Consider implementing ID obfuscation if you need to hide sequential IDs
- Monitor performance improvements after migration
- Update any external integrations that depend on UUID format
- Consider implementing audit logging for ID changes
