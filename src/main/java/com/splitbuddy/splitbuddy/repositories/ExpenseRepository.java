package com.splitbuddy.splitbuddy.repositories;

import com.splitbuddy.splitbuddy.models.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {

        // Find expenses where user is the payer
        List<Expense> findByPaidByIdOrderByCreatedAtDesc(Long paidById);

        // Find expenses where user is a participant
        @Query(value = "SELECT e.* FROM public.expenses e " +
                        "JOIN public.expense_participants ep ON e.id = ep.expense_id " +
                        "WHERE ep.user_id = :userId", nativeQuery = true)
        List<Expense> findExpensesByParticipantId(@Param("userId") Long userId);

        // Find all expenses for a user (either as payer or participant)
        @Query("SELECT DISTINCT e FROM Expense e " +
                        "WHERE e.paidBy.id = :userId " +
                        "OR EXISTS (SELECT ep FROM ExpenseParticipant ep WHERE ep.expense = e AND ep.user.id = :userId) "
                        +
                        "ORDER BY e.createdAt DESC")
        List<Expense> findAllExpensesForUser(@Param("userId") Long userId);

        // Find expenses by group (where any participant is from the group)
        @Query("SELECT DISTINCT e FROM Expense e " +
                        "JOIN e.participants ep " +
                        "WHERE ep.source = 'GROUP' AND ep.sourceId = :groupId " +
                        "ORDER BY e.createdAt DESC")
        List<Expense> findExpensesByGroupId(@Param("groupId") Long groupId);

        // Find expenses shared between two specific users (regardless of source)
        @Query("SELECT DISTINCT e FROM Expense e " +
                        "JOIN e.participants ep1 " +
                        "JOIN e.participants ep2 " +
                        "WHERE ep1.user.id = :userId1 AND ep2.user.id = :userId2 " +
                        "ORDER BY e.createdAt DESC")
        List<Expense> findExpensesBetweenFriends(@Param("userId1") Long userId1, @Param("userId2") Long userId2);

        // Find all expenses for a specific group with detailed information
        @Query("SELECT DISTINCT e FROM Expense e " +
                        "JOIN e.participants ep " +
                        "WHERE ep.source = 'GROUP' AND ep.sourceId = :groupId " +
                        "ORDER BY e.createdAt DESC")
        List<Expense> findAllExpensesForGroup(@Param("groupId") Long groupId);
}
