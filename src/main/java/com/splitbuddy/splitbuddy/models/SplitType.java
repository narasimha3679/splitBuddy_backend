package com.splitbuddy.splitbuddy.models;

public enum SplitType {
    EQUAL, // Split equally among all participants
    PERCENTAGE, // Split based on percentages
    AMOUNT, // Split based on specific amounts
    SHARES // Split based on shares (e.g., 2 shares for one person, 1 for another)
}
