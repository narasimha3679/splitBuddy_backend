package com.splitbuddy.splitbuddy.services;

import com.splitbuddy.splitbuddy.dto.request.CreateExpenseRequest;
import com.splitbuddy.splitbuddy.dto.request.UpdateExpenseRequest;
import com.splitbuddy.splitbuddy.dto.response.ExpenseResponse;
import com.splitbuddy.splitbuddy.dto.response.FriendBalanceResponse;
import com.splitbuddy.splitbuddy.dto.response.FriendExpensesResponse;
import com.splitbuddy.splitbuddy.dto.response.GroupBalanceResponse;
import com.splitbuddy.splitbuddy.dto.response.SettlementResponse;
import com.splitbuddy.splitbuddy.dto.response.UserBalanceSummaryResponse;
import com.splitbuddy.splitbuddy.exceptions.ExpenseNotFoundException;
import com.splitbuddy.splitbuddy.exceptions.InvalidOperationException;
import com.splitbuddy.splitbuddy.exceptions.UserNotFoundException;
import com.splitbuddy.splitbuddy.models.*;
import com.splitbuddy.splitbuddy.repositories.ExpenseParticipantRepository;
import com.splitbuddy.splitbuddy.repositories.ExpenseRepository;
import com.splitbuddy.splitbuddy.repositories.FriendshipRepository;
import com.splitbuddy.splitbuddy.repositories.GroupRepository;
import com.splitbuddy.splitbuddy.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Slf4j
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final ExpenseParticipantRepository expenseParticipantRepository;
    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final FriendshipRepository friendshipRepository;
    private final BalanceService balanceService;

    @Transactional
    public ExpenseResponse createExpense(CreateExpenseRequest request) {
        log.info("Creating expense: {}", request.getTitle());

        // Validate and get the payer
        User payer = userRepository.findById(request.getPaidBy())
                .orElseThrow(() -> new UserNotFoundException("Payer not found"));

        // Create the expense
        Expense expense = new Expense();
        expense.setTitle(request.getTitle());
        expense.setDescription(request.getDescription());
        expense.setAmount(request.getAmount());
        expense.setCurrency(request.getCurrency());
        expense.setCategory(request.getCategory());
        expense.setPaidBy(payer);
        expense.setPaidAt(request.getPaidAt());

        // Process participants and handle duplicates
        Set<Long> processedUserIds = new HashSet<>();
        List<ExpenseParticipant> participants = new ArrayList<>();

        for (CreateExpenseRequest.ParticipantRequest participantDto : request.getParticipants()) {
            // Skip if user already processed (duplicate prevention)
            if (processedUserIds.contains(participantDto.getUserId())) {
                log.warn("Skipping duplicate participant: {}", participantDto.getUserId());
                continue;
            }

            // Validate user exists
            User participantUser = userRepository.findById(participantDto.getUserId())
                    .orElseThrow(() -> new UserNotFoundException(
                            "Participant user not found: " + participantDto.getUserId()));

            // Validate friendship or group membership based on source
            validateParticipantSource(participantDto, payer);

            // Create participant
            ExpenseParticipant participant = new ExpenseParticipant();
            participant.setExpense(expense);
            participant.setUser(participantUser);
            participant.setSource(participantDto.getSource());
            participant.setSourceId(participantDto.getSourceId());
            participant.setAmount(participantDto.getAmount());

            participants.add(participant);
            processedUserIds.add(participantDto.getUserId());
        }

        // Validate total amount matches (with small tolerance for rounding)
        BigDecimal totalParticipantAmount = participants.stream()
                .map(ExpenseParticipant::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal difference = request.getAmount().subtract(totalParticipantAmount).abs();
        if (difference.compareTo(BigDecimal.valueOf(0.01)) > 0) {
            throw new InvalidOperationException("Total participant amount (" + totalParticipantAmount +
                    ") does not match expense amount (" + request.getAmount() + ")");
        }

        // Save expense and participants
        expense.setParticipants(participants);
        Expense savedExpense = expenseRepository.save(expense);

        // Update balance aggregates
        balanceService.updateBalancesForExpense(savedExpense);

        log.info("Expense created successfully with ID: {}", savedExpense.getId());
        return convertToResponse(savedExpense);
    }

    /**
     * Update an existing expense
     */
    @Transactional
    public ExpenseResponse updateExpense(Long expenseId, UpdateExpenseRequest request) {
        log.info("Updating expense ID: {}", expenseId);

        // Find the expense
        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new ExpenseNotFoundException("Expense not found: " + expenseId));

        // Verify current user has permission (must be payer or participant)
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long currentUserId = Long.valueOf(authentication.getName());
        
        boolean isPayerOrParticipant = expense.getPaidBy().getId().equals(currentUserId) ||
                expense.getParticipants().stream().anyMatch(p -> p.getUser().getId().equals(currentUserId));
        
        if (!isPayerOrParticipant) {
            throw new InvalidOperationException("You don't have permission to update this expense");
        }

        // Reverse existing balance aggregates before updating
        balanceService.reverseBalancesForExpense(expense);

        // Update expense fields (only non-null values)
        if (request.getTitle() != null) {
            expense.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            expense.setDescription(request.getDescription());
        }
        if (request.getAmount() != null) {
            expense.setAmount(request.getAmount());
        }
        if (request.getCurrency() != null) {
            expense.setCurrency(request.getCurrency());
        }
        if (request.getCategory() != null) {
            expense.setCategory(request.getCategory());
        }
        if (request.getPaidAt() != null) {
            expense.setPaidAt(request.getPaidAt());
        }
        if (request.getPaidBy() != null) {
            User newPayer = userRepository.findById(request.getPaidBy())
                    .orElseThrow(() -> new UserNotFoundException("Payer not found: " + request.getPaidBy()));
            expense.setPaidBy(newPayer);
        }

        // Update participants if provided
        if (request.getParticipants() != null && !request.getParticipants().isEmpty()) {
            // Remove old participants
            expenseParticipantRepository.deleteAll(expense.getParticipants());
            
            // Create new participants
            List<ExpenseParticipant> newParticipants = new ArrayList<>();
            for (UpdateExpenseRequest.ParticipantRequest participantDto : request.getParticipants()) {
                User participantUser = userRepository.findById(participantDto.getUserId())
                        .orElseThrow(() -> new UserNotFoundException("Participant not found: " + participantDto.getUserId()));
                
                ExpenseParticipant participant = new ExpenseParticipant();
                participant.setExpense(expense);
                participant.setUser(participantUser);
                participant.setAmount(participantDto.getAmount());
                participant.setSource(participantDto.getSource());
                participant.setSourceId(participantDto.getSourceId());
                newParticipants.add(participant);
            }
            expense.setParticipants(newParticipants);
        }

        // Save updated expense
        Expense updatedExpense = expenseRepository.save(expense);

        // Recalculate balance aggregates
        balanceService.updateBalancesForExpense(updatedExpense);

        log.info("Expense updated successfully: {}", expenseId);
        return convertToResponse(updatedExpense);
    }

    /**
     * Delete an expense
     */
    @Transactional
    public void deleteExpense(Long expenseId) {
        log.info("Deleting expense ID: {}", expenseId);

        // Find the expense
        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new ExpenseNotFoundException("Expense not found: " + expenseId));

        // Verify current user has permission
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long currentUserId = Long.valueOf(authentication.getName());
        
        if (!expense.getPaidBy().getId().equals(currentUserId)) {
            throw new InvalidOperationException("Only the payer can delete this expense");
        }

        // Reverse balance aggregates
        balanceService.reverseBalancesForExpense(expense);

        // Delete participants and expense
        expenseParticipantRepository.deleteAll(expense.getParticipants());
        expenseRepository.delete(expense);

        log.info("Expense deleted successfully: {}", expenseId);
    }

    /**
     * Update payment status of a participant
     */
    @Transactional
    public void updateParticipantPaymentStatus(Long expenseId, Long participantId, boolean isPaid) {
        log.info("Updating payment status for participant {} in expense {}: isPaid={}", participantId, expenseId, isPaid);

        // Find the participant
        ExpenseParticipant participant = expenseParticipantRepository.findById(participantId)
                .orElseThrow(() -> new ExpenseNotFoundException("Participant not found: " + participantId));

        // Verify the participant belongs to the expense
        if (!participant.getExpense().getId().equals(expenseId)) {
            throw new InvalidOperationException("Participant does not belong to this expense");
        }

        // Verify current user has permission (must be payer)
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long currentUserId = Long.valueOf(authentication.getName());
        
        if (!participant.getExpense().getPaidBy().getId().equals(currentUserId)) {
            throw new InvalidOperationException("Only the payer can mark payments");
        }

        // Update payment status
        participant.setPaid(isPaid);
        participant.setPaidAt(isPaid ? Instant.now() : null);
        expenseParticipantRepository.save(participant);

        // Update balances
        balanceService.updateBalanceForPayment(participant.getExpense(), participant, isPaid);

        log.info("Payment status updated successfully");
    }

    private void validateParticipantSource(CreateExpenseRequest.ParticipantRequest participantDto, User payer) {
        // Skip validation if the participant is the same as the payer (user can't be
        // friends with themselves)
        if (participantDto.getUserId().equals(payer.getId())) {
            log.info("Skipping validation for payer as participant: {}", participantDto.getUserId());
            return;
        }

        if (participantDto.getSource() == ExpenseParticipant.ParticipantSource.FRIEND) {
            // Validate friendship exists - sourceId is optional for friends
            User participantUser = userRepository.findById(participantDto.getUserId())
                    .orElseThrow(() -> new UserNotFoundException(
                            "Participant user not found: " + participantDto.getUserId()));

            boolean friendshipExists = friendshipRepository.existsByUserAndFriend(payer, participantUser) ||
                    friendshipRepository.existsByUserAndFriend(participantUser, payer);
            if (!friendshipExists) {
                throw new InvalidOperationException("User is not a friend: " + participantDto.getUserId());
            }
        } else if (participantDto.getSource() == ExpenseParticipant.ParticipantSource.GROUP) {
            // Validate group membership - sourceId is required for groups
            if (participantDto.getSourceId() == null) {
                throw new InvalidOperationException("Source ID is required for GROUP participants");
            }

            Group group = groupRepository.findById(participantDto.getSourceId())
                    .orElseThrow(
                            () -> new InvalidOperationException("Group not found: " + participantDto.getSourceId()));

            boolean isGroupMember = group.getMembers().stream()
                    .anyMatch(member -> member.getId().equals(participantDto.getUserId()));
            if (!isGroupMember) {
                throw new InvalidOperationException("User is not a member of the group: " + participantDto.getUserId());
            }
        }
    }

    public List<ExpenseResponse> getExpensesForUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userIdString = authentication.getName();
        Long userId = Long.valueOf(userIdString);
        List<Expense> expenses = expenseRepository.findExpensesByParticipantId(userId);
        return expenses.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public ExpenseResponse getExpenseById(Long expenseId) {
        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new ExpenseNotFoundException("Expense not found"));
        return convertToResponse(expense);
    }

    public List<ExpenseResponse> getExpensesByGroup(Long groupId) {
        List<Expense> expenses = expenseRepository.findExpensesByGroupId(groupId);
        return expenses.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public List<ExpenseResponse> getExpensesByUser() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userIdString = authentication.getName();
        Long userId = Long.valueOf(userIdString);
        System.out.println("userId: " + userId);
        List<Expense> expenses = expenseRepository.findExpensesByParticipantId(userId);
        return expenses.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public List<FriendBalanceResponse> getFriendBalances(String userIdString) {
        Long userId = Long.valueOf(userIdString);
        return balanceService.getFriendBalances(userId);
    }

    public List<ExpenseResponse> getRecentActivities() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userIdString = authentication.getName();
        Long userId = Long.valueOf(userIdString);

        List<Expense> expenses = expenseRepository.findAllExpensesForUser(userId);
        return expenses.stream()
                .sorted((e1, e2) -> e2.getCreatedAt().compareTo(e1.getCreatedAt())) // Most recent first
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public List<SettlementResponse> getSettlements(String userIdString) {
        Long userId = Long.valueOf(userIdString);
        // TODO: Implement settlement calculation logic
        return new ArrayList<>();
    }

    public UserBalanceSummaryResponse getUserBalanceSummary() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userIdString = authentication.getName();
        Long userId = Long.valueOf(userIdString);

        return balanceService.getUserBalanceSummary(userId);
    }

    public UserBalanceSummaryResponse getUserBalanceSummaryById(String userIdString) {
        Long userId = Long.valueOf(userIdString);
        return balanceService.getUserBalanceSummary(userId);
    }

    public FriendExpensesResponse getExpensesBetweenFriends(String friendIdString) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userIdString = authentication.getName();
        Long userId = Long.valueOf(userIdString);
        Long friendId = Long.valueOf(friendIdString);

        return balanceService.getFriendExpenses(userId, friendId);
    }

    public List<ExpenseResponse> getAllExpensesForGroup(Long groupId) {
        List<Expense> expenses = expenseRepository.findAllExpensesForGroup(groupId);
        return expenses.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public List<GroupBalanceResponse> getGroupBalances(Long groupId) {
        return balanceService.getGroupBalancesForGroup(groupId);
    }

    public List<GroupBalanceResponse> getUserGroupBalances(String userIdString) {
        Long userId = Long.valueOf(userIdString);
        return balanceService.getGroupBalances(userId);
    }

    private ExpenseResponse convertToResponse(Expense expense) {
        ExpenseResponse response = new ExpenseResponse();
        response.setId(expense.getId());
        response.setTitle(expense.getTitle());
        response.setDescription(expense.getDescription());
        response.setAmount(expense.getAmount());
        response.setCurrency(expense.getCurrency());
        response.setCategory(expense.getCategory());
        response.setPaidAt(expense.getPaidAt());
        response.setCreatedAt(expense.getCreatedAt());
        response.setUpdatedAt(expense.getUpdatedAt());
        response.setPaidBy(expense.getPaidBy().getId());
        response.setPaidByName(expense.getPaidBy().getName());

        List<ExpenseResponse.ParticipantResponse> participantResponses = expense.getParticipants().stream()
                .map(this::convertParticipantToResponse)
                .collect(Collectors.toList());
        response.setParticipants(participantResponses);

        return response;
    }

    private ExpenseResponse.ParticipantResponse convertParticipantToResponse(ExpenseParticipant participant) {
        ExpenseResponse.ParticipantResponse response = new ExpenseResponse.ParticipantResponse();
        response.setId(participant.getId());
        response.setUserId(participant.getUser().getId());
        response.setUserName(participant.getUser().getName());
        response.setAmount(participant.getAmount());
        response.setSource(participant.getSource());
        response.setSourceId(participant.getSourceId());
        response.setActive(participant.isActive());
        response.setPaid(participant.isPaid());
        response.setPaidAt(participant.getPaidAt());
        return response;
    }
}
