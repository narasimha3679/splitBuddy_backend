-- Create balance_aggregates table for efficient balance calculations
CREATE TABLE balance_aggregates (
    id BIGSERIAL PRIMARY KEY,
    user1_id BIGINT,
    user2_id BIGINT,
    user_id BIGINT,
    group_id BIGINT,
    balance_type VARCHAR(20) NOT NULL,
    balance DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    last_updated TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_expense_id BIGINT NOT NULL,
    
    -- Constraints for friend-to-friend balances
    CONSTRAINT fk_balance_user1 FOREIGN KEY (user1_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_balance_user2 FOREIGN KEY (user2_id) REFERENCES users(id) ON DELETE CASCADE,
    
    -- Constraints for user-to-group balances
    CONSTRAINT fk_balance_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_balance_group FOREIGN KEY (group_id) REFERENCES groups(id) ON DELETE CASCADE,
    
    -- Unique constraints
    CONSTRAINT uk_friend_balance UNIQUE (user1_id, user2_id, balance_type),
    CONSTRAINT uk_group_balance UNIQUE (user_id, group_id, balance_type),
    
    -- Check constraints
    CONSTRAINT chk_balance_type CHECK (balance_type IN ('FRIEND_TO_FRIEND', 'USER_TO_GROUP')),
    CONSTRAINT chk_friend_balance_users CHECK (
        (balance_type = 'FRIEND_TO_FRIEND' AND user1_id IS NOT NULL AND user2_id IS NOT NULL AND user_id IS NULL AND group_id IS NULL) OR
        (balance_type = 'USER_TO_GROUP' AND user_id IS NOT NULL AND group_id IS NOT NULL AND user1_id IS NULL AND user2_id IS NULL)
    )
);

-- Create indexes for better performance
CREATE INDEX idx_balance_user1 ON balance_aggregates(user1_id);
CREATE INDEX idx_balance_user2 ON balance_aggregates(user2_id);
CREATE INDEX idx_balance_user ON balance_aggregates(user_id);
CREATE INDEX idx_balance_group ON balance_aggregates(group_id);
CREATE INDEX idx_balance_type ON balance_aggregates(balance_type);
CREATE INDEX idx_balance_last_updated ON balance_aggregates(last_updated);
