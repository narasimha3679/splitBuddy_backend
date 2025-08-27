package com.splitbuddy.splitbuddy.services;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException;
import org.springframework.stereotype.Service;

import com.splitbuddy.splitbuddy.models.FriendRequest;
import com.splitbuddy.splitbuddy.models.FriendRequestStatus;
import com.splitbuddy.splitbuddy.models.Friendship;
import com.splitbuddy.splitbuddy.models.User;
import com.splitbuddy.splitbuddy.repositories.FriendRequestRepository;
import com.splitbuddy.splitbuddy.repositories.FriendshipRepository;
import com.splitbuddy.splitbuddy.repositories.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FriendService {

    private final FriendshipRepository friendshipRepository;
    private final FriendRequestRepository friendRequestRepository;
    private final UserRepository userRepository;

    public FriendRequest sendFriendRequest(User sender, User receiver) {
        if (sender.equals(receiver)) {
            throw new IllegalArgumentException("Cannot send friend request to yourself");
        }

        Optional<FriendRequest> existingRequest = friendRequestRepository
                .findBySenderAndReceiver(sender, receiver);

        if (existingRequest.isPresent()) {
            throw new IllegalStateException("Friend request already exists");
        }

        FriendRequest request = new FriendRequest();
        request.setSender(sender);
        request.setReceiver(receiver);
        request.setStatus(FriendRequestStatus.PENDING);
        request.setCreatedAt(LocalDateTime.now());

        return friendRequestRepository.save(request);
    }

    public FriendRequest respondToFriendRequest(UUID requestId, FriendRequestStatus response) throws NotFoundException {
        FriendRequest request = friendRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException());

        if (request.getStatus() != FriendRequestStatus.PENDING) {
            throw new IllegalStateException("Request already processed");
        }

        request.setStatus(response);

        if (response == FriendRequestStatus.ACCEPTED) {
            createFriendship(request.getSender(), request.getReceiver());
        }

        return friendRequestRepository.save(request);
    }

    private void createFriendship(User user1, User user2) {
        // Create bidirectional friendship
        Friendship friendship1 = new Friendship();
        friendship1.setUser(user1);
        friendship1.setFriend(user2);
        friendship1.setBecameFriendsAt(LocalDateTime.now());

        Friendship friendship2 = new Friendship();
        friendship2.setUser(user2);
        friendship2.setFriend(user1);
        friendship2.setBecameFriendsAt(LocalDateTime.now());

        friendshipRepository.saveAll(Arrays.asList(friendship1, friendship2));
    }

    public List<User> getFriends(UUID userId) throws NotFoundException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException());

        return friendshipRepository.findByUser(user).stream()
                .map(friendship -> friendship.getFriend())
                .collect(Collectors.toList());
    }

    public List<FriendRequest> getPendingRequests(User receiver) {
        return friendRequestRepository.findByReceiverAndStatus(receiver, FriendRequestStatus.PENDING);
    }
}
