package com.splitbuddy.splitbuddy.controllers;

import com.splitbuddy.splitbuddy.dto.response.FriendBalanceResponse;
import com.splitbuddy.splitbuddy.dto.response.FriendExpensesResponse;
import com.splitbuddy.splitbuddy.dto.response.GroupBalanceResponse;
import com.splitbuddy.splitbuddy.dto.response.UserBalanceSummaryResponse;
import com.splitbuddy.splitbuddy.services.BalanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Balance Controller
 * 
 * Handles balance calculations and summaries for users, friends, and groups.
 * 
 * Frontend Types: See expo/splitbuddy/src/types/api-contracts.ts
 * - UserBalanceSummaryResponse, FriendBalanceResponse, GroupBalanceResponse, FriendExpensesResponse
 * 
 * API Documentation: See backend/API_DOCUMENTATION.md#balances
 * OpenAPI Spec: See backend/openapi.yaml#/paths/balances
 * 
 * @see expo/splitbuddy/src/utils/api.ts for frontend API functions
 */
@RestController
@RequestMapping("/api/balances")
@RequiredArgsConstructor
@Slf4j
public class BalanceController {

    private final BalanceService balanceService;

    @GetMapping("/summary")
    public ResponseEntity<UserBalanceSummaryResponse> getUserBalanceSummary() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userIdString = authentication.getName();
        Long userId = Long.valueOf(userIdString);

        UserBalanceSummaryResponse response = balanceService.getUserBalanceSummary(userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}/summary")
    public ResponseEntity<UserBalanceSummaryResponse> getUserBalanceSummaryById(@PathVariable String userId) {
        Long userIdLong = Long.valueOf(userId);
        UserBalanceSummaryResponse response = balanceService.getUserBalanceSummary(userIdLong);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/friends")
    public ResponseEntity<List<FriendBalanceResponse>> getFriendBalances() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userIdString = authentication.getName();
        Long userId = Long.valueOf(userIdString);

        List<FriendBalanceResponse> response = balanceService.getFriendBalances(userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}/friends")
    public ResponseEntity<List<FriendBalanceResponse>> getFriendBalancesById(@PathVariable String userId) {
        Long userIdLong = Long.valueOf(userId);
        List<FriendBalanceResponse> response = balanceService.getFriendBalances(userIdLong);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/friend/{friendId}/expenses")
    public ResponseEntity<FriendExpensesResponse> getFriendExpenses(@PathVariable String friendId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userIdString = authentication.getName();
        Long userId = Long.valueOf(userIdString);
        Long friendIdLong = Long.valueOf(friendId);

        FriendExpensesResponse response = balanceService.getFriendExpenses(userId, friendIdLong);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/groups")
    public ResponseEntity<List<GroupBalanceResponse>> getUserGroupBalances() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userIdString = authentication.getName();
        Long userId = Long.valueOf(userIdString);

        List<GroupBalanceResponse> response = balanceService.getGroupBalances(userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}/groups")
    public ResponseEntity<List<GroupBalanceResponse>> getUserGroupBalancesById(@PathVariable String userId) {
        Long userIdLong = Long.valueOf(userId);
        List<GroupBalanceResponse> response = balanceService.getGroupBalances(userIdLong);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/group/{groupId}/balances")
    public ResponseEntity<List<GroupBalanceResponse>> getGroupBalances(@PathVariable Long groupId) {
        List<GroupBalanceResponse> response = balanceService.getGroupBalancesForGroup(groupId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/recalculate")
    public ResponseEntity<String> recalculateAllBalances() {
        log.info("Starting balance recalculation...");
        balanceService.recalculateAllBalances();
        return ResponseEntity.ok("Balance recalculation completed successfully");
    }
}
