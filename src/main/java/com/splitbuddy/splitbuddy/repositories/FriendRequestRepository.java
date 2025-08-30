package com.splitbuddy.splitbuddy.repositories;

import com.splitbuddy.splitbuddy.models.FriendRequest;
import com.splitbuddy.splitbuddy.models.FriendRequestStatus;
import com.splitbuddy.splitbuddy.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FriendRequestRepository extends JpaRepository<FriendRequest, Long> {

    List<FriendRequest> findByReceiverIdAndStatus(Long receiverId, FriendRequestStatus status);

    Optional<FriendRequest> findBySenderAndReceiver(User sender, User receiver);

}
