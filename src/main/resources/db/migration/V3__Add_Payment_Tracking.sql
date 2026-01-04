-- V3: Add payment tracking fields to expense_participants table
-- This migration adds is_paid and paid_at columns to track settlement status

ALTER TABLE expense_participants ADD COLUMN IF NOT EXISTS is_paid BOOLEAN DEFAULT FALSE;
ALTER TABLE expense_participants ADD COLUMN IF NOT EXISTS paid_at TIMESTAMP;

-- Create index for faster queries on unpaid expenses
CREATE INDEX IF NOT EXISTS idx_expense_participants_is_paid ON expense_participants(is_paid);
