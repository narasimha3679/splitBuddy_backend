-- Database Migration Script: Convert UUID to BIGINT (Long)
-- This script converts all UUID primary keys and foreign keys to BIGINT
-- Run this script on your PostgreSQL database before starting the application

-- Step 1: Create new BIGINT columns
ALTER TABLE users ADD COLUMN id_new BIGSERIAL;
ALTER TABLE expenses ADD COLUMN id_new BIGSERIAL;
ALTER TABLE groups ADD COLUMN id_new BIGSERIAL;
ALTER TABLE friend_requests ADD COLUMN id_new BIGSERIAL;
ALTER TABLE friendships ADD COLUMN id_new BIGSERIAL;
ALTER TABLE expense_participants ADD COLUMN id_new BIGSERIAL;

-- Step 2: Create new foreign key columns
ALTER TABLE expenses ADD COLUMN paid_by_new BIGINT;
ALTER TABLE expense_participants ADD COLUMN expense_id_new BIGINT;
ALTER TABLE expense_participants ADD COLUMN user_id_new BIGINT;
ALTER TABLE expense_participants ADD COLUMN source_id_new BIGINT;
ALTER TABLE friend_requests ADD COLUMN sender_id_new BIGINT;
ALTER TABLE friend_requests ADD COLUMN receiver_id_new BIGINT;
ALTER TABLE friendships ADD COLUMN user_id_new BIGINT;
ALTER TABLE friendships ADD COLUMN friend_id_new BIGINT;
ALTER TABLE groups ADD COLUMN created_by_new BIGINT;
ALTER TABLE group_members ADD COLUMN group_id_new BIGINT;
ALTER TABLE group_members ADD COLUMN user_id_new BIGINT;

-- Step 3: Create mapping tables to preserve relationships
CREATE TEMP TABLE user_id_mapping AS 
SELECT id, id_new FROM users ORDER BY id_new;

CREATE TEMP TABLE expense_id_mapping AS 
SELECT id, id_new FROM expenses ORDER BY id_new;

CREATE TEMP TABLE group_id_mapping AS 
SELECT id, id_new FROM groups ORDER BY id_new;

CREATE TEMP TABLE friend_request_id_mapping AS 
SELECT id, id_new FROM friend_requests ORDER BY id_new;

CREATE TEMP TABLE friendship_id_mapping AS 
SELECT id, id_new FROM friendships ORDER BY id_new;

CREATE TEMP TABLE expense_participant_id_mapping AS 
SELECT id, id_new FROM expense_participants ORDER BY id_new;

-- Step 4: Update foreign key references using the mapping tables
UPDATE expenses SET paid_by_new = um.id_new 
FROM user_id_mapping um WHERE expenses.paid_by = um.id;

UPDATE expense_participants SET expense_id_new = em.id_new 
FROM expense_id_mapping em WHERE expense_participants.expense_id = em.id;

UPDATE expense_participants SET user_id_new = um.id_new 
FROM user_id_mapping um WHERE expense_participants.user_id = um.id;

UPDATE expense_participants SET source_id_new = gm.id_new 
FROM group_id_mapping gm WHERE expense_participants.source_id = gm.id;

UPDATE friend_requests SET sender_id_new = um.id_new 
FROM user_id_mapping um WHERE friend_requests.sender_id = um.id;

UPDATE friend_requests SET receiver_id_new = um.id_new 
FROM user_id_mapping um WHERE friend_requests.receiver_id = um.id;

UPDATE friendships SET user_id_new = um.id_new 
FROM user_id_mapping um WHERE friendships.user_id = um.id;

UPDATE friendships SET friend_id_new = um.id_new 
FROM user_id_mapping um WHERE friendships.friend_id = um.id;

UPDATE groups SET created_by_new = um.id_new 
FROM user_id_mapping um WHERE groups.created_by = um.id;

UPDATE group_members SET group_id_new = gm.id_new 
FROM group_id_mapping gm WHERE group_members.group_id = gm.id;

UPDATE group_members SET user_id_new = um.id_new 
FROM user_id_mapping um WHERE group_members.user_id = um.id;

-- Step 5: Drop old columns and rename new ones
-- Users table
ALTER TABLE users DROP COLUMN id;
ALTER TABLE users RENAME COLUMN id_new TO id;
ALTER TABLE users ADD PRIMARY KEY (id);

-- Expenses table
ALTER TABLE expenses DROP COLUMN id;
ALTER TABLE expenses DROP COLUMN paid_by;
ALTER TABLE expenses RENAME COLUMN id_new TO id;
ALTER TABLE expenses RENAME COLUMN paid_by_new TO paid_by;
ALTER TABLE expenses ADD PRIMARY KEY (id);
ALTER TABLE expenses ADD CONSTRAINT fk_expenses_paid_by FOREIGN KEY (paid_by) REFERENCES users(id);

-- Groups table
ALTER TABLE groups DROP COLUMN id;
ALTER TABLE groups DROP COLUMN created_by;
ALTER TABLE groups RENAME COLUMN id_new TO id;
ALTER TABLE groups RENAME COLUMN created_by_new TO created_by;
ALTER TABLE groups ADD PRIMARY KEY (id);
ALTER TABLE groups ADD CONSTRAINT fk_groups_created_by FOREIGN KEY (created_by) REFERENCES users(id);

-- Friend requests table
ALTER TABLE friend_requests DROP COLUMN id;
ALTER TABLE friend_requests DROP COLUMN sender_id;
ALTER TABLE friend_requests DROP COLUMN receiver_id;
ALTER TABLE friend_requests RENAME COLUMN id_new TO id;
ALTER TABLE friend_requests RENAME COLUMN sender_id_new TO sender_id;
ALTER TABLE friend_requests RENAME COLUMN receiver_id_new TO receiver_id;
ALTER TABLE friend_requests ADD PRIMARY KEY (id);
ALTER TABLE friend_requests ADD CONSTRAINT fk_friend_requests_sender FOREIGN KEY (sender_id) REFERENCES users(id);
ALTER TABLE friend_requests ADD CONSTRAINT fk_friend_requests_receiver FOREIGN KEY (receiver_id) REFERENCES users(id);

-- Friendships table
ALTER TABLE friendships DROP COLUMN id;
ALTER TABLE friendships DROP COLUMN user_id;
ALTER TABLE friendships DROP COLUMN friend_id;
ALTER TABLE friendships RENAME COLUMN id_new TO id;
ALTER TABLE friendships RENAME COLUMN user_id_new TO user_id;
ALTER TABLE friendships RENAME COLUMN friend_id_new TO friend_id;
ALTER TABLE friendships ADD PRIMARY KEY (id);
ALTER TABLE friendships ADD CONSTRAINT fk_friendships_user FOREIGN KEY (user_id) REFERENCES users(id);
ALTER TABLE friendships ADD CONSTRAINT fk_friendships_friend FOREIGN KEY (friend_id) REFERENCES users(id);

-- Expense participants table
ALTER TABLE expense_participants DROP COLUMN id;
ALTER TABLE expense_participants DROP COLUMN expense_id;
ALTER TABLE expense_participants DROP COLUMN user_id;
ALTER TABLE expense_participants DROP COLUMN source_id;
ALTER TABLE expense_participants RENAME COLUMN id_new TO id;
ALTER TABLE expense_participants RENAME COLUMN expense_id_new TO expense_id;
ALTER TABLE expense_participants RENAME COLUMN user_id_new TO user_id;
ALTER TABLE expense_participants RENAME COLUMN source_id_new TO source_id;
ALTER TABLE expense_participants ADD PRIMARY KEY (id);
ALTER TABLE expense_participants ADD CONSTRAINT fk_expense_participants_expense FOREIGN KEY (expense_id) REFERENCES expenses(id);
ALTER TABLE expense_participants ADD CONSTRAINT fk_expense_participants_user FOREIGN KEY (user_id) REFERENCES users(id);

-- Group members table
ALTER TABLE group_members DROP COLUMN group_id;
ALTER TABLE group_members DROP COLUMN user_id;
ALTER TABLE group_members RENAME COLUMN group_id_new TO group_id;
ALTER TABLE group_members RENAME COLUMN user_id_new TO user_id;
ALTER TABLE group_members ADD CONSTRAINT fk_group_members_group FOREIGN KEY (group_id) REFERENCES groups(id);
ALTER TABLE group_members ADD CONSTRAINT fk_group_members_user FOREIGN KEY (user_id) REFERENCES users(id);

-- Step 6: Create indexes for better performance
CREATE INDEX idx_expenses_paid_by ON expenses(paid_by);
CREATE INDEX idx_expense_participants_expense_id ON expense_participants(expense_id);
CREATE INDEX idx_expense_participants_user_id ON expense_participants(user_id);
CREATE INDEX idx_friend_requests_sender_id ON friend_requests(sender_id);
CREATE INDEX idx_friend_requests_receiver_id ON friend_requests(receiver_id);
CREATE INDEX idx_friendships_user_id ON friendships(user_id);
CREATE INDEX idx_friendships_friend_id ON friendships(friend_id);
CREATE INDEX idx_group_members_group_id ON group_members(group_id);
CREATE INDEX idx_group_members_user_id ON group_members(user_id);

-- Step 7: Update sequences to start from the next available ID
SELECT setval('users_id_seq', (SELECT MAX(id) FROM users));
SELECT setval('expenses_id_seq', (SELECT MAX(id) FROM expenses));
SELECT setval('groups_id_seq', (SELECT MAX(id) FROM groups));
SELECT setval('friend_requests_id_seq', (SELECT MAX(id) FROM friend_requests));
SELECT setval('friendships_id_seq', (SELECT MAX(id) FROM friendships));
SELECT setval('expense_participants_id_seq', (SELECT MAX(id) FROM expense_participants));

-- Step 8: Clean up temporary tables
DROP TABLE user_id_mapping;
DROP TABLE expense_id_mapping;
DROP TABLE group_id_mapping;
DROP TABLE friend_request_id_mapping;
DROP TABLE friendship_id_mapping;
DROP TABLE expense_participant_id_mapping;

-- Verification queries
SELECT 'Users count: ' || COUNT(*) FROM users;
SELECT 'Expenses count: ' || COUNT(*) FROM expenses;
SELECT 'Groups count: ' || COUNT(*) FROM groups;
SELECT 'Friend requests count: ' || COUNT(*) FROM friend_requests;
SELECT 'Friendships count: ' || COUNT(*) FROM friendships;
SELECT 'Expense participants count: ' || COUNT(*) FROM expense_participants;
