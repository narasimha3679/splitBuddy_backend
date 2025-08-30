package com.splitbuddy.splitbuddy.services;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.splitbuddy.splitbuddy.dto.request.FriendRequestDto;
import com.splitbuddy.splitbuddy.dto.response.FriendResponse;
import com.splitbuddy.splitbuddy.dto.response.PendingFriendRequestsResponseDto;
import com.splitbuddy.splitbuddy.exceptions.DuplicateResourceException;
import com.splitbuddy.splitbuddy.exceptions.FriendRequestNotFoundException;
import com.splitbuddy.splitbuddy.exceptions.InvalidOperationException;
import com.splitbuddy.splitbuddy.exceptions.UserNotFoundException;
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

    public void sendFriendRequest(FriendRequestDto requestDto) {
        User sender = userRepository.findById(requestDto.getSenderId())
                .orElseThrow(() -> new UserNotFoundException("Sender not found"));
        User receiver = userRepository.findById(requestDto.getReceiverId())
                .orElseThrow(() -> new UserNotFoundException("Receiver not found"));

        if (sender.equals(receiver)) {
            throw new InvalidOperationException("Cannot send friend request to yourself");
        }

        // Check if a request already exists which is not rejected
        Optional<FriendRequest> existingRequest = friendRequestRepository
                .findBySenderAndReceiver(sender, receiver);

        if (existingRequest.isPresent() && existingRequest.get().getStatus() != FriendRequestStatus.REJECTED) {
            throw new DuplicateResourceException("Friend request already exists");
        }

        FriendRequest request = new FriendRequest();
        request.setSender(sender);
        request.setReceiver(receiver);
        request.setStatus(FriendRequestStatus.PENDING);
        request.setCreatedAt(LocalDateTime.now());

        friendRequestRepository.save(request);
    }

    public void acceptFriendRequest(Long requestId) {
        FriendRequest request = friendRequestRepository.findById(requestId)
                .orElseThrow(
                        () -> new FriendRequestNotFoundException("Friend request not found with ID: " + requestId));

        if (request.getStatus() != FriendRequestStatus.PENDING) {
            throw new InvalidOperationException("Request already processed");
        }

        request.setStatus(FriendRequestStatus.ACCEPTED);
        friendRequestRepository.save(request);

        createFriendship(request.getSender(), request.getReceiver());
    }

    public void rejectFriendRequest(Long requestId) {
        FriendRequest request = friendRequestRepository.findById(requestId)
                .orElseThrow(
                        () -> new FriendRequestNotFoundException("Friend request not found with ID: " + requestId));

        if (request.getStatus() != FriendRequestStatus.PENDING) {
            throw new InvalidOperationException("Request already processed");
        }

        request.setStatus(FriendRequestStatus.REJECTED);
        friendRequestRepository.save(request);
    }

    public FriendRequest respondToFriendRequest(Long requestId, FriendRequestStatus response) {
        FriendRequest request = friendRequestRepository.findById(requestId)
                .orElseThrow(
                        () -> new FriendRequestNotFoundException("Friend request not found with ID: " + requestId));

        if (request.getStatus() != FriendRequestStatus.PENDING) {
            throw new InvalidOperationException("Request already processed");
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

    public List<FriendResponse> getFriends(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));

        return friendshipRepository.findByUser(user).stream()
                .map(friendship -> {
                    User friend = friendship.getFriend();
                    return new FriendResponse(friend.getId(), friend.getName(), friend.getEmail());
                })
                .collect(Collectors.toList());
    }

    public List<PendingFriendRequestsResponseDto> getPendingRequests(Long userId) {
        User receiver = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));

        return friendRequestRepository.findByReceiverIdAndStatus(userId, FriendRequestStatus.PENDING)
                .stream()
                .map(request -> {
                    PendingFriendRequestsResponseDto dto = new PendingFriendRequestsResponseDto();
                    dto.setId(request.getId());
                    dto.setSenderName(request.getSender().getName());
                    dto.setSenderEmail(request.getSender().getEmail());
                    dto.setCreatedAt(request.getCreatedAt());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    public List<PendingFriendRequestsResponseDto> getPendingRequests(User receiver) {
        return friendRequestRepository.findByReceiverIdAndStatus(receiver.getId(), FriendRequestStatus.PENDING)
                .stream()
                .map(request -> {
                    PendingFriendRequestsResponseDto dto = new PendingFriendRequestsResponseDto();
                    dto.setId(request.getId());
                    dto.setSenderName(request.getSender().getName());
                    dto.setSenderEmail(request.getSender().getEmail());
                    dto.setCreatedAt(request.getCreatedAt());
                    return dto;
                })
                .collect(Collectors.toList());
    }
}
