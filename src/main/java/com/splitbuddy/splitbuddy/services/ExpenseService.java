package com.splitbuddy.splitbuddy.services;

import com.splitbuddy.splitbuddy.dto.request.CreateExpenseRequest;
import com.splitbuddy.splitbuddy.dto.response.ExpenseResponse;
import com.splitbuddy.splitbuddy.dto.response.FriendBalanceResponse;
import com.splitbuddy.splitbuddy.dto.response.FriendExpensesResponse;
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

        log.info("Expense created successfully with ID: {}", savedExpense.getId());
        return convertToResponse(savedExpense);
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
        return calculateFriendBalances(userId);
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

        return calculateUserBalanceSummary(userId);
    }

    public UserBalanceSummaryResponse getUserBalanceSummaryById(String userIdString) {
        Long userId = Long.valueOf(userIdString);
        return calculateUserBalanceSummary(userId);
    }

    public FriendExpensesResponse getExpensesBetweenFriends(String friendIdString) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userIdString = authentication.getName();
        Long userId = Long.valueOf(userIdString);
        Long friendId = Long.valueOf(friendIdString);

        return calculateFriendExpenses(userId, friendId);
    }

    public List<ExpenseResponse> getAllExpensesForGroup(Long groupId) {
        List<Expense> expenses = expenseRepository.findAllExpensesForGroup(groupId);
        return expenses.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Calculate comprehensive balance summary for a user across all expenses
     * they're involved in.
     * 
     * Balance calculation logic:
     * - totalOwed: Sum of amounts others owe to the user (when user paid for
     * expenses)
     * - totalOwes: Sum of amounts user owes to others (when others paid for
     * expenses)
     * - netBalance: totalOwed - totalOwes (positive = user is owed money, negative
     * = user owes money)
     */
    private UserBalanceSummaryResponse calculateUserBalanceSummary(Long userId) {
        // Get all expenses where user is involved (as payer or participant)
        List<Expense> userExpenses = expenseRepository.findAllExpensesForUser(userId);

        BigDecimal totalOwed = BigDecimal.ZERO; // Amount others owe to this user
        BigDecimal totalOwes = BigDecimal.ZERO; // Amount this user owes to others

        for (Expense expense : userExpenses) {
            User payer = expense.getPaidBy();

            // Find the user's participation in this expense
            ExpenseParticipant userParticipation = null;
            for (ExpenseParticipant participant : expense.getParticipants()) {
                if (participant.getUser().getId().equals(userId)) {
                    userParticipation = participant;
                    break;
                }
            }

            // Skip if user is not a participant in this expense
            if (userParticipation == null) {
                continue;
            }

            BigDecimal userAmount = userParticipation.getAmount();

            if (payer.getId().equals(userId)) {
                // User paid for this expense
                // Calculate how much others owe to the user
                BigDecimal othersOweToUser = BigDecimal.ZERO;
                for (ExpenseParticipant participant : expense.getParticipants()) {
                    if (!participant.getUser().getId().equals(userId)) {
                        othersOweToUser = othersOweToUser.add(participant.getAmount());
                    }
                }
                totalOwed = totalOwed.add(othersOweToUser);
            } else {
                // Someone else paid for this expense
                // User owes the payer their share
                totalOwes = totalOwes.add(userAmount);
            }
        }

        // Get user details
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + userId));

        UserBalanceSummaryResponse response = new UserBalanceSummaryResponse();
        response.setUserId(userId);
        response.setUserName(user.getName());
        response.setTotalOwed(totalOwed);
        response.setTotalOwes(totalOwes);
        response.setNetBalance(totalOwed.subtract(totalOwes));

        return response;
    }

    private FriendExpensesResponse calculateFriendExpenses(Long userId, Long friendId) {
        // Get expenses shared between the two users
        List<Expense> sharedExpenses = expenseRepository.findExpensesBetweenFriends(userId, friendId);

        BigDecimal totalOwedToFriend = BigDecimal.ZERO; // Amount user owes to friend
        BigDecimal totalOwedByFriend = BigDecimal.ZERO; // Amount friend owes to user

        for (Expense expense : sharedExpenses) {
            User payer = expense.getPaidBy();

            // Find the amounts for both users in this expense
            BigDecimal userAmount = BigDecimal.ZERO;
            BigDecimal friendAmount = BigDecimal.ZERO;

            for (ExpenseParticipant participant : expense.getParticipants()) {
                if (participant.getUser().getId().equals(userId)) {
                    userAmount = participant.getAmount();
                } else if (participant.getUser().getId().equals(friendId)) {
                    friendAmount = participant.getAmount();
                }
            }

            if (payer.getId().equals(userId)) {
                // User paid, friend owes user
                totalOwedByFriend = totalOwedByFriend.add(friendAmount);
            } else if (payer.getId().equals(friendId)) {
                // Friend paid, user owes friend
                totalOwedToFriend = totalOwedToFriend.add(userAmount);
            }
        }

        // Get friend details
        User friend = userRepository.findById(friendId)
                .orElseThrow(() -> new UserNotFoundException("Friend not found: " + friendId));

        FriendExpensesResponse response = new FriendExpensesResponse();
        response.setFriendId(friendId);
        response.setFriendName(friend.getName());
        response.setTotalOwedToFriend(totalOwedToFriend);
        response.setTotalOwedByFriend(totalOwedByFriend);
        response.setNetBalance(totalOwedByFriend.subtract(totalOwedToFriend));
        response.setSharedExpenses(sharedExpenses.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList()));

        return response;
    }

    /**
     * Calculate individual balances with each friend for a user.
     * 
     * Balance calculation logic:
     * - Positive balance: Friend owes money to the user
     * - Negative balance: User owes money to the friend
     * - Zero balance: No outstanding balance between user and friend
     */
    private List<FriendBalanceResponse> calculateFriendBalances(Long userId) {
        // Get all expenses where user is involved (as payer or participant)
        List<Expense> userExpenses = expenseRepository.findAllExpensesForUser(userId);

        Map<Long, BigDecimal> friendBalances = new HashMap<>();

        for (Expense expense : userExpenses) {
            User payer = expense.getPaidBy();

            // Find the user's participation in this expense
            ExpenseParticipant userParticipation = null;
            for (ExpenseParticipant participant : expense.getParticipants()) {
                if (participant.getUser().getId().equals(userId)) {
                    userParticipation = participant;
                    break;
                }
            }

            // Skip if user is not a participant in this expense
            if (userParticipation == null) {
                continue;
            }

            BigDecimal userAmount = userParticipation.getAmount();

            // Calculate balances with each friend in this expense
            for (ExpenseParticipant participant : expense.getParticipants()) {
                User participantUser = participant.getUser();
                BigDecimal participantAmount = participant.getAmount();

                // Skip if this is the same user
                if (participantUser.getId().equals(userId)) {
                    continue;
                }

                Long friendId = participantUser.getId();
                BigDecimal currentBalance = friendBalances.getOrDefault(friendId, BigDecimal.ZERO);

                if (payer.getId().equals(userId)) {
                    // User paid for this expense, friend owes user
                    currentBalance = currentBalance.add(participantAmount);
                } else if (payer.getId().equals(friendId)) {
                    // Friend paid for this expense, user owes friend
                    currentBalance = currentBalance.subtract(userAmount);
                }

                friendBalances.put(friendId, currentBalance);
            }
        }

        // Convert to response format with proper friend names
        return friendBalances.entrySet().stream()
                .map(entry -> {
                    FriendBalanceResponse response = new FriendBalanceResponse();
                    response.setFriendId(entry.getKey());

                    // Get friend name from user repository
                    try {
                        User friend = userRepository.findById(entry.getKey())
                                .orElseThrow(() -> new UserNotFoundException("Friend not found: " + entry.getKey()));
                        response.setFriendName(friend.getName());
                    } catch (UserNotFoundException e) {
                        response.setFriendName("Unknown User");
                    }

                    response.setBalance(entry.getValue());
                    return response;
                })
                .collect(Collectors.toList());
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

        List<ExpenseResponse.ParticipantResponse> participantResponses = expense.getParticipants().stream()
                .map(this::convertParticipantToResponse)
                .collect(Collectors.toList());
        response.setParticipants(participantResponses);

        return response;
    }

    private ExpenseResponse.ParticipantResponse convertParticipantToResponse(ExpenseParticipant participant) {
        ExpenseResponse.ParticipantResponse response = new ExpenseResponse.ParticipantResponse();
        response.setUserId(participant.getUser().getId());
        response.setUserName(participant.getUser().getName());
        response.setAmount(participant.getAmount());
        response.setSource(participant.getSource());
        response.setSourceId(participant.getSourceId());
        response.setActive(participant.isActive());
        return response;
    }
}
