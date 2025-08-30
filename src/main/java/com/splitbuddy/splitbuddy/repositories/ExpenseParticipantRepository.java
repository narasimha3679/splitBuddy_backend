package com.splitbuddy.splitbuddy.repositories;

import com.splitbuddy.splitbuddy.models.ExpenseParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExpenseParticipantRepository extends JpaRepository<ExpenseParticipant, Long> {

    List<ExpenseParticipant> findByExpenseIdOrderByUser_Name(Long expenseId);

    List<ExpenseParticipant> findByUserId(Long userId);

    List<ExpenseParticipant> findBySourceAndSourceId(ExpenseParticipant.ParticipantSource source, Long sourceId);

    boolean existsByExpenseIdAndUserId(Long expenseId, Long userId);

    @Query("SELECT ep FROM ExpenseParticipant ep WHERE ep.expense.id IN :expenseIds")
    List<ExpenseParticipant> findByExpenseIds(@Param("expenseIds") List<Long> expenseIds);

}
