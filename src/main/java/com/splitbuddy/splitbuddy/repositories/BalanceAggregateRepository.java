package com.splitbuddy.splitbuddy.repositories;

import com.splitbuddy.splitbuddy.models.BalanceAggregate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface BalanceAggregateRepository extends JpaRepository<BalanceAggregate, Long> {

    // Find balance between two specific friends
    @Query("SELECT ba FROM BalanceAggregate ba " +
            "WHERE ba.balanceType = 'FRIEND_TO_FRIEND' " +
            "AND ((ba.user1.id = :userId1 AND ba.user2.id = :userId2) " +
            "OR (ba.user1.id = :userId2 AND ba.user2.id = :userId1))")
    Optional<BalanceAggregate> findFriendBalance(@Param("userId1") Long userId1, @Param("userId2") Long userId2);

    // Find all friend balances for a user
    @Query("SELECT ba FROM BalanceAggregate ba " +
            "WHERE ba.balanceType = 'FRIEND_TO_FRIEND' " +
            "AND (ba.user1.id = :userId OR ba.user2.id = :userId)")
    List<BalanceAggregate> findAllFriendBalancesForUser(@Param("userId") Long userId);

    // Find balance between a user and a group
    @Query("SELECT ba FROM BalanceAggregate ba " +
            "WHERE ba.balanceType = 'USER_TO_GROUP' " +
            "AND ba.user.id = :userId AND ba.group.id = :groupId")
    Optional<BalanceAggregate> findGroupBalance(@Param("userId") Long userId, @Param("groupId") Long groupId);

    // Find all group balances for a user
    @Query("SELECT ba FROM BalanceAggregate ba " +
            "WHERE ba.balanceType = 'USER_TO_GROUP' " +
            "AND ba.user.id = :userId")
    List<BalanceAggregate> findAllGroupBalancesForUser(@Param("userId") Long userId);

    // Find all group balances for a specific group
    @Query("SELECT ba FROM BalanceAggregate ba " +
            "WHERE ba.balanceType = 'USER_TO_GROUP' " +
            "AND ba.group.id = :groupId")
    List<BalanceAggregate> findAllBalancesForGroup(@Param("groupId") Long groupId);

    // Get total balance for a user (sum of all friend balances)
    @Query("SELECT COALESCE(SUM(CASE " +
            "WHEN ba.user1.id = :userId THEN ba.balance " +
            "WHEN ba.user2.id = :userId THEN -ba.balance " +
            "END), 0) FROM BalanceAggregate ba " +
            "WHERE ba.balanceType = 'FRIEND_TO_FRIEND' " +
            "AND (ba.user1.id = :userId OR ba.user2.id = :userId)")
    BigDecimal getTotalFriendBalanceForUser(@Param("userId") Long userId);

    // Get total balance for a user across all groups
    @Query("SELECT COALESCE(SUM(ba.balance), 0) FROM BalanceAggregate ba " +
            "WHERE ba.balanceType = 'USER_TO_GROUP' " +
            "AND ba.user.id = :userId")
    BigDecimal getTotalGroupBalanceForUser(@Param("userId") Long userId);

    // Check if balance exists between two users
    boolean existsByUser1IdAndUser2IdAndBalanceType(Long user1Id, Long user2Id,
            BalanceAggregate.BalanceType balanceType);

    // Check if balance exists between user and group
    boolean existsByUserIdAndGroupIdAndBalanceType(Long userId, Long groupId, BalanceAggregate.BalanceType balanceType);
}
