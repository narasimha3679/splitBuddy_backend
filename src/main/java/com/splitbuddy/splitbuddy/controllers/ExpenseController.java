package com.splitbuddy.splitbuddy.controllers;

import com.splitbuddy.splitbuddy.dto.request.CreateExpenseRequest;
import com.splitbuddy.splitbuddy.dto.response.ExpenseResponse;
import com.splitbuddy.splitbuddy.dto.response.FriendBalanceResponse;
import com.splitbuddy.splitbuddy.dto.response.FriendExpensesResponse;
import com.splitbuddy.splitbuddy.dto.response.SettlementResponse;
import com.splitbuddy.splitbuddy.dto.response.UserBalanceSummaryResponse;
import com.splitbuddy.splitbuddy.services.ExpenseService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/expenses")
@CrossOrigin(origins = "*")
public class ExpenseController {

    @Autowired
    private ExpenseService expenseService;

    @PostMapping
    public ResponseEntity<ExpenseResponse> createExpense(@RequestBody CreateExpenseRequest request) {
        ExpenseResponse response = expenseService.createExpense(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{expenseId}")
    public ResponseEntity<ExpenseResponse> getExpenseById(@PathVariable Long expenseId) {
        ExpenseResponse response = expenseService.getExpenseById(expenseId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/group/{groupId}")
    public ResponseEntity<List<ExpenseResponse>> getExpensesByGroup(@PathVariable Long groupId) {
        List<ExpenseResponse> response = expenseService.getExpensesByGroup(groupId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<ExpenseResponse>> getExpensesByUser() {
        List<ExpenseResponse> response = expenseService.getExpensesByUser();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}/balances")
    public ResponseEntity<List<FriendBalanceResponse>> getFriendBalances(@PathVariable String userId) {
        List<FriendBalanceResponse> response = expenseService.getFriendBalances(userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}/settlements")
    public ResponseEntity<List<SettlementResponse>> getSettlements(@PathVariable String userId) {
        List<SettlementResponse> response = expenseService.getSettlements(userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/balance/summary")
    public ResponseEntity<UserBalanceSummaryResponse> getUserBalanceSummary() {
        UserBalanceSummaryResponse response = expenseService.getUserBalanceSummary();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}/balance/summary")
    public ResponseEntity<UserBalanceSummaryResponse> getUserBalanceSummaryById(@PathVariable String userId) {
        UserBalanceSummaryResponse response = expenseService.getUserBalanceSummaryById(userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/friend/{friendId}/expenses")
    public ResponseEntity<FriendExpensesResponse> getExpensesBetweenFriends(@PathVariable String friendId) {
        FriendExpensesResponse response = expenseService.getExpensesBetweenFriends(friendId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/group/{groupId}/all")
    public ResponseEntity<List<ExpenseResponse>> getAllExpensesForGroup(@PathVariable Long groupId) {
        List<ExpenseResponse> response = expenseService.getAllExpensesForGroup(groupId);
        return ResponseEntity.ok(response);
    }
}
