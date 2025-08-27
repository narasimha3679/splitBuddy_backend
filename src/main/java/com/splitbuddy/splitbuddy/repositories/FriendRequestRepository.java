package com.splitbuddy.splitbuddy.repositories;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.splitbuddy.splitbuddy.models.FriendRequest;
import com.splitbuddy.splitbuddy.models.FriendRequestStatus;
import com.splitbuddy.splitbuddy.models.User;

public interface FriendRequestRepository extends JpaRepository<FriendRequest, UUID> {

    List<FriendRequest> findByReceiverAndStatus(User receiver, FriendRequestStatus status);

    Optional<FriendRequest> findBySenderAndReceiver(User sender, User receiver);
}
