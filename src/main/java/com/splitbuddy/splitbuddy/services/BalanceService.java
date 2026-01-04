package com.splitbuddy.splitbuddy.services;

import com.splitbuddy.splitbuddy.dto.response.FriendBalanceResponse;
import com.splitbuddy.splitbuddy.dto.response.FriendExpensesResponse;
import com.splitbuddy.splitbuddy.dto.response.GroupBalanceResponse;
import com.splitbuddy.splitbuddy.dto.response.UserBalanceSummaryResponse;
import com.splitbuddy.splitbuddy.exceptions.UserNotFoundException;
import com.splitbuddy.splitbuddy.models.BalanceAggregate;
import com.splitbuddy.splitbuddy.models.Expense;
import com.splitbuddy.splitbuddy.models.ExpenseParticipant;
import com.splitbuddy.splitbuddy.models.Group;
import com.splitbuddy.splitbuddy.models.User;
import com.splitbuddy.splitbuddy.repositories.BalanceAggregateRepository;
import com.splitbuddy.splitbuddy.repositories.ExpenseRepository;
import com.splitbuddy.splitbuddy.repositories.GroupRepository;
import com.splitbuddy.splitbuddy.repositories.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class BalanceService {

    @Autowired
    private BalanceAggregateRepository balanceAggregateRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private ExpenseRepository expenseRepository;

    /**
     * Update balances when a new expense is created
     */
    @Transactional
    public void updateBalancesForExpense(Expense expense) {
        log.info("Updating balances for expense ID: {}", expense.getId());

        User payer = expense.getPaidBy();
        BigDecimal expenseAmount = expense.getAmount();

        // Update friend-to-friend balances
        updateFriendBalances(expense, payer);

        // Update group balances
        updateGroupBalances(expense, payer);
    }

    /**
     * Reverse balances when an expense is deleted or before it's updated
     */
    @Transactional
    public void reverseBalancesForExpense(Expense expense) {
        log.info("Reversing balances for expense ID: {}", expense.getId());

        User payer = expense.getPaidBy();

        // Reverse friend-to-friend balances
        for (ExpenseParticipant participant : expense.getParticipants()) {
            User participantUser = participant.getUser();

            if (participantUser.getId().equals(payer.getId())) {
                continue;
            }

            BigDecimal participantAmount = participant.getAmount();
            Long userId1 = Math.min(payer.getId(), participantUser.getId());
            Long userId2 = Math.max(payer.getId(), participantUser.getId());

            Optional<BalanceAggregate> existingBalance = balanceAggregateRepository
                    .findFriendBalance(userId1, userId2);

            if (existingBalance.isPresent()) {
                BalanceAggregate balance = existingBalance.get();
                BigDecimal balanceChange;
                if (payer.getId() < participantUser.getId()) {
                    balanceChange = participantAmount.negate(); // Reverse: subtract instead of add
                } else {
                    balanceChange = participantAmount; // Reverse: add instead of subtract
                }
                balance.setBalance(balance.getBalance().add(balanceChange));
                balanceAggregateRepository.save(balance);
            }
        }

        // Reverse group balances
        Map<Long, List<ExpenseParticipant>> groupParticipants = expense.getParticipants().stream()
                .filter(ep -> ep.getSource() == ExpenseParticipant.ParticipantSource.GROUP)
                .collect(Collectors.groupingBy(ExpenseParticipant::getSourceId));

        for (Map.Entry<Long, List<ExpenseParticipant>> entry : groupParticipants.entrySet()) {
            Long groupId = entry.getKey();
            List<ExpenseParticipant> participants = entry.getValue();

            for (ExpenseParticipant participant : participants) {
                User participantUser = participant.getUser();
                BigDecimal participantAmount = participant.getAmount();

                Optional<BalanceAggregate> existingBalance = balanceAggregateRepository
                        .findGroupBalance(participantUser.getId(), groupId);

                if (existingBalance.isPresent()) {
                    BalanceAggregate balance = existingBalance.get();
                    BigDecimal balanceChange;
                    if (participantUser.getId().equals(payer.getId())) {
                        balanceChange = (expense.getAmount().subtract(participantAmount)).negate();
                    } else {
                        balanceChange = participantAmount; // Reverse: add instead of negate
                    }
                    balance.setBalance(balance.getBalance().add(balanceChange));
                    balanceAggregateRepository.save(balance);
                }
            }
        }
    }


    private void updateFriendBalances(Expense expense, User payer) {
        Map<Long, BigDecimal> participantAmounts = expense.getParticipants().stream()
                .collect(Collectors.toMap(
                        ep -> ep.getUser().getId(),
                        ExpenseParticipant::getAmount));

        // Update balance between payer and each participant
        for (ExpenseParticipant participant : expense.getParticipants()) {
            User participantUser = participant.getUser();

            // Skip if this is the payer
            if (participantUser.getId().equals(payer.getId())) {
                continue;
            }

            BigDecimal participantAmount = participant.getAmount();
            Long userId1 = Math.min(payer.getId(), participantUser.getId());
            Long userId2 = Math.max(payer.getId(), participantUser.getId());

            // Find existing balance or create new one
            Optional<BalanceAggregate> existingBalance = balanceAggregateRepository
                    .findFriendBalance(userId1, userId2);

            BigDecimal balanceChange;
            if (payer.getId() < participantUser.getId()) {
                // Payer is user1, so positive balance means participant owes payer
                balanceChange = participantAmount;
            } else {
                // Payer is user2, so negative balance means participant owes payer
                balanceChange = participantAmount.negate();
            }

            if (existingBalance.isPresent()) {
                BalanceAggregate balance = existingBalance.get();
                balance.setBalance(balance.getBalance().add(balanceChange));
                balance.setLastExpenseId(expense.getId());
                balanceAggregateRepository.save(balance);
            } else {
                User user1 = userRepository.findById(userId1).orElseThrow();
                User user2 = userRepository.findById(userId2).orElseThrow();
                BalanceAggregate newBalance = new BalanceAggregate(user1, user2, balanceChange, expense.getId());
                balanceAggregateRepository.save(newBalance);
            }
        }
    }

    private void updateGroupBalances(Expense expense, User payer) {
        // Group expenses are handled differently - we track user's balance with the
        // group
        Map<Long, List<ExpenseParticipant>> groupParticipants = expense.getParticipants().stream()
                .filter(ep -> ep.getSource() == ExpenseParticipant.ParticipantSource.GROUP)
                .collect(Collectors.groupingBy(ExpenseParticipant::getSourceId));

        for (Map.Entry<Long, List<ExpenseParticipant>> entry : groupParticipants.entrySet()) {
            Long groupId = entry.getKey();
            List<ExpenseParticipant> participants = entry.getValue();

            // Update balance for each participant with the group
            for (ExpenseParticipant participant : participants) {
                User participantUser = participant.getUser();
                BigDecimal participantAmount = participant.getAmount();

                Optional<BalanceAggregate> existingBalance = balanceAggregateRepository
                        .findGroupBalance(participantUser.getId(), groupId);

                BigDecimal balanceChange;
                if (participantUser.getId().equals(payer.getId())) {
                    // Participant paid, so they have a positive balance with the group
                    balanceChange = expense.getAmount().subtract(participantAmount);
                } else {
                    // Someone else paid, so participant owes their share
                    balanceChange = participantAmount.negate();
                }

                if (existingBalance.isPresent()) {
                    BalanceAggregate balance = existingBalance.get();
                    balance.setBalance(balance.getBalance().add(balanceChange));
                    balance.setLastExpenseId(expense.getId());
                    balanceAggregateRepository.save(balance);
                } else {
                    Group group = groupRepository.findById(groupId).orElseThrow();
                    BalanceAggregate newBalance = new BalanceAggregate(participantUser, group, balanceChange,
                            expense.getId());
                    balanceAggregateRepository.save(newBalance);
                }
            }
        }
    }

    /**
     * Get user balance summary using aggregated data
     */
    public UserBalanceSummaryResponse getUserBalanceSummary(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + userId));

        BigDecimal totalFriendBalance = balanceAggregateRepository.getTotalFriendBalanceForUser(userId);
        BigDecimal totalGroupBalance = balanceAggregateRepository.getTotalGroupBalanceForUser(userId);
        BigDecimal netBalance = totalFriendBalance.add(totalGroupBalance);

        UserBalanceSummaryResponse response = new UserBalanceSummaryResponse();
        response.setUserId(userId);
        response.setUserName(user.getName());
        response.setTotalOwed(netBalance.compareTo(BigDecimal.ZERO) > 0 ? netBalance : BigDecimal.ZERO);
        response.setTotalOwes(netBalance.compareTo(BigDecimal.ZERO) < 0 ? netBalance.abs() : BigDecimal.ZERO);
        response.setNetBalance(netBalance);

        return response;
    }

    /**
     * Get friend balances using aggregated data
     */
    public List<FriendBalanceResponse> getFriendBalances(Long userId) {
        List<BalanceAggregate> friendBalances = balanceAggregateRepository.findAllFriendBalancesForUser(userId);

        return friendBalances.stream()
                .map(balance -> {
                    FriendBalanceResponse response = new FriendBalanceResponse();

                    // Determine which user is the friend and calculate the balance from user's
                    // perspective
                    if (balance.getUser1().getId().equals(userId)) {
                        response.setFriendId(balance.getUser2().getId());
                        response.setFriendName(balance.getUser2().getName());
                        response.setBalance(balance.getBalance()); // Positive means friend owes user
                    } else {
                        response.setFriendId(balance.getUser1().getId());
                        response.setFriendName(balance.getUser1().getName());
                        response.setBalance(balance.getBalance().negate()); // Negative means user owes friend
                    }

                    return response;
                })
                .collect(Collectors.toList());
    }

    /**
     * Get balance between two specific friends
     */
    public FriendExpensesResponse getFriendExpenses(Long userId, Long friendId) {
        User friend = userRepository.findById(friendId)
                .orElseThrow(() -> new UserNotFoundException("Friend not found: " + friendId));

        // Get aggregated balance
        Optional<BalanceAggregate> balanceOpt = balanceAggregateRepository.findFriendBalance(userId, friendId);
        BigDecimal netBalance = balanceOpt.map(BalanceAggregate::getBalance).orElse(BigDecimal.ZERO);

        // Adjust balance based on user order
        if (balanceOpt.isPresent() && balanceOpt.get().getUser2().getId().equals(userId)) {
            netBalance = netBalance.negate();
        }

        // Get recent expenses for context (increased limit to 50)
        List<Expense> recentExpenses = expenseRepository.findExpensesBetweenFriends(userId, friendId)
                .stream()
                .limit(50)
                .collect(Collectors.toList());

        FriendExpensesResponse response = new FriendExpensesResponse();
        response.setFriendId(friendId);
        response.setFriendName(friend.getName());
        response.setTotalOwedToFriend(netBalance.compareTo(BigDecimal.ZERO) < 0 ? netBalance.abs() : BigDecimal.ZERO);
        response.setTotalOwedByFriend(netBalance.compareTo(BigDecimal.ZERO) > 0 ? netBalance : BigDecimal.ZERO);
        response.setNetBalance(netBalance);
        response.setSharedExpenses(recentExpenses.stream()
                .map(this::convertToExpenseResponse)
                .collect(Collectors.toList()));

        return response;
    }

    /**
     * Get group balances for a user
     */
    public List<GroupBalanceResponse> getGroupBalances(Long userId) {
        List<BalanceAggregate> groupBalances = balanceAggregateRepository.findAllGroupBalancesForUser(userId);

        return groupBalances.stream()
                .map(balance -> {
                    GroupBalanceResponse response = new GroupBalanceResponse();
                    response.setGroupId(balance.getGroup().getId());
                    response.setGroupName(balance.getGroup().getName());
                    response.setBalance(balance.getBalance());
                    return response;
                })
                .collect(Collectors.toList());
    }

    /**
     * Get all balances for a specific group
     */
    public List<GroupBalanceResponse> getGroupBalancesForGroup(Long groupId) {
        List<BalanceAggregate> groupBalances = balanceAggregateRepository.findAllBalancesForGroup(groupId);

        return groupBalances.stream()
                .map(balance -> {
                    GroupBalanceResponse response = new GroupBalanceResponse();
                    response.setGroupId(balance.getGroup().getId());
                    response.setGroupName(balance.getGroup().getName());
                    response.setUserId(balance.getUser().getId());
                    response.setUserName(balance.getUser().getName());
                    response.setBalance(balance.getBalance());
                    return response;
                })
                .collect(Collectors.toList());
    }

    /**
     * Recalculate all balances (for data migration or fixing inconsistencies)
     */
    @Transactional
    public void recalculateAllBalances() {
        log.info("Starting full balance recalculation...");

        // Clear all existing balances
        balanceAggregateRepository.deleteAll();

        // Get all expenses and recalculate
        List<Expense> allExpenses = expenseRepository.findAll();
        for (Expense expense : allExpenses) {
            updateBalancesForExpense(expense);
        }

        log.info("Completed full balance recalculation for {} expenses", allExpenses.size());
    }

    private com.splitbuddy.splitbuddy.dto.response.ExpenseResponse convertToExpenseResponse(Expense expense) {
        com.splitbuddy.splitbuddy.dto.response.ExpenseResponse response = new com.splitbuddy.splitbuddy.dto.response.ExpenseResponse();
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

        List<com.splitbuddy.splitbuddy.dto.response.ExpenseResponse.ParticipantResponse> participantResponses = expense
                .getParticipants().stream()
                .map(this::convertParticipantToResponse)
                .collect(Collectors.toList());
        response.setParticipants(participantResponses);

        return response;
    }

    private com.splitbuddy.splitbuddy.dto.response.ExpenseResponse.ParticipantResponse convertParticipantToResponse(
            ExpenseParticipant participant) {
        com.splitbuddy.splitbuddy.dto.response.ExpenseResponse.ParticipantResponse response = new com.splitbuddy.splitbuddy.dto.response.ExpenseResponse.ParticipantResponse();
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

    /**
     * Update balance when a participant's payment status changes
     * @param expense The expense
     * @param participant The participant whose status changed
     * @param isPaid The new payment status
     */
    @Transactional
    public void updateBalanceForPayment(Expense expense, ExpenseParticipant participant, boolean isPaid) {
        User payer = expense.getPaidBy();
        User participantUser = participant.getUser();
        BigDecimal amount = participant.getAmount();

        // If user is the payer, this logic doesn't apply (payer doesn't owe themselves)
        if (payer.getId().equals(participantUser.getId())) {
            return;
        }

        // Determine the impact on balance
        // If marked as PAID: The debt is settled, so we REVERSE the balance impact
        // If marked as UNPAID: The debt is active again, so we APPLY the balance impact
        
        // We can reuse the logic by thinking about "balance change"
        // Original logic: Payer paid -> Participant owes Payer
        // If Payer < Participant: Balance += Amount (positive means Payer is owed)
        // If Payer > Participant: Balance -= Amount (negative means Payer is owed)
        
        // When marking as PAID (settled):
        // We need to negate the original impact.
        // If Payer < Participant: Balance -= Amount
        // If Payer > Participant: Balance += Amount
        
        // When marking as UNPAID (active debt):
        // We re-apply the original impact.
        // If Payer < Participant: Balance += Amount
        // If Payer > Participant: Balance -= Amount
        
        BigDecimal balanceChange;
        
        if (isPaid) {
            // Debt settled - reverse the balance
            if (payer.getId() < participantUser.getId()) {
                balanceChange = amount.negate();
            } else {
                balanceChange = amount;
            }
        } else {
            // Debt active - add the balance
            if (payer.getId() < participantUser.getId()) {
                balanceChange = amount;
            } else {
                balanceChange = amount.negate();
            }
        }
        
        // Update Friend Balance
        Long userId1 = Math.min(payer.getId(), participantUser.getId());
        Long userId2 = Math.max(payer.getId(), participantUser.getId());
        
        Optional<BalanceAggregate> existingBalance = balanceAggregateRepository
                .findFriendBalance(userId1, userId2);

        if (existingBalance.isPresent()) {
            BalanceAggregate balance = existingBalance.get();
            balance.setBalance(balance.getBalance().add(balanceChange));
            balance.setLastExpenseId(expense.getId());
            balanceAggregateRepository.save(balance);
        } else {
            // This suggests a balance record was missing, which is odd if an expense exists,
            // but we can create it.
             User user1 = userRepository.findById(userId1).orElseThrow();
            User user2 = userRepository.findById(userId2).orElseThrow();
            BalanceAggregate newBalance = new BalanceAggregate(user1, user2, balanceChange, expense.getId());
            balanceAggregateRepository.save(newBalance);
        }
        
        // Update Group Balance if applicable
        if (participant.getSource() == ExpenseParticipant.ParticipantSource.GROUP && participant.getSourceId() != null) {
            Long groupId = participant.getSourceId();
            
            // For group balances:
            // Payer balance with group: +Amount (they paid on behalf of group)
            // Participant balance with group: -Amount (they owe the group)
            
            // When PAID (Settled):
            // Effectively, the participant has "paid back" the group (or rather, the payer via the group context).
            // But usually "Mark as Paid" in a group context implies the debt to the PAYER is settled.
            // In Splitwise/similar apps, "Mark as Paid" usually creates a "Payment" transaction.
            // Here we are modifying the state of the original expense participation.
            
            // If we strictly follow the logic that "Participant owes Payer":
            // The Group Balance Aggregate tracks "How much does User X owe the Group?".
            // If User X pays User Y (Payer) directly, that settles the debt effectively.
            
            // Update Participant's Group Balance
            Optional<BalanceAggregate> partGroupBalance = balanceAggregateRepository
                    .findGroupBalance(participantUser.getId(), groupId);
            
            if (partGroupBalance.isPresent()) {
                BalanceAggregate balance = partGroupBalance.get();
                // If PAId: Participant no longer owes the group (add amount effectively)
                // If UNPAID: Participant owes the group (subtract amount)
                 BigDecimal change = isPaid ? amount : amount.negate();
                 balance.setBalance(balance.getBalance().add(change));
                 balanceAggregateRepository.save(balance);
            }
            
            // We do NOT update the Payer's group balance here because the Payer "spent" the money regardless.
            // The fact that they got paid back is a separate settlement details.
            // However, for "Net Balance" calculation, if the Payer is "owed" less, their net balance should decrease.
            // Our Group Balance model might be "User vs Group", where positive = User is Owed by Group.
            
            // Let's look at `updateGroupBalances`:
             // Payer: balance += (Total - OwnShare)  [Positive = Group owes Payer]
             // Participant: balance -= Share         [Negative = Participant owes Group]
             
             // So if Participant PAYS Payer:
             // Participant: balance += Share (Debt erased, moves towards 0)
             // Payer: balance -= Share (They are owed less by the group/network)
             
             Optional<BalanceAggregate> payerGroupBalance = balanceAggregateRepository
                    .findGroupBalance(payer.getId(), groupId);
             
             if (payerGroupBalance.isPresent()) {
                 BalanceAggregate balance = payerGroupBalance.get();
                 // If PAID: Payer is owed less (Subtract share)
                 // If UNPAID: Payer is owed more (Add share)
                 BigDecimal change = isPaid ? amount.negate() : amount;
                 balance.setBalance(balance.getBalance().add(change));
                 balanceAggregateRepository.save(balance);
             }
        }
    }
}

