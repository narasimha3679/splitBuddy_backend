package com.splitbuddy.splitbuddy.repositories;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.splitbuddy.splitbuddy.models.Friendship;
import com.splitbuddy.splitbuddy.models.User;

public interface FriendshipRepository extends JpaRepository<Friendship, UUID> {

    List<Friendship> findByUser(User user);

    boolean existsByUserAndFriend(User user, User friend);
}
