package com.splitbuddy.splitbuddy.controllers;

import java.util.List;
import java.util.UUID;

import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.splitbuddy.splitbuddy.dto.request.FriendRequestDto;
import com.splitbuddy.splitbuddy.dto.response.PendingFriendRequestsResponseDto;
import com.splitbuddy.splitbuddy.models.FriendRequest;
import com.splitbuddy.splitbuddy.models.FriendRequestStatus;
import com.splitbuddy.splitbuddy.models.User;
import com.splitbuddy.splitbuddy.repositories.UserRepository;
import com.splitbuddy.splitbuddy.services.FriendService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/friends")
@RequiredArgsConstructor
public class FriendController {
    private final FriendService friendService;
    private final UserRepository userRepository;

    @PostMapping("/requests")
    public ResponseEntity<FriendRequest> sendRequest(
            @RequestBody FriendRequestDto requestDto) throws NotFoundException {
        User sender = userRepository.findById(requestDto.getSenderId())
                .orElseThrow(() -> new NotFoundException());
        User receiver = userRepository.findById(requestDto.getReceiverId())
                .orElseThrow(() -> new NotFoundException());

        FriendRequest request = friendService.sendFriendRequest(sender, receiver);
        return ResponseEntity.ok(request);
    }

    @PutMapping("/requests/{requestId}")
    public ResponseEntity<FriendRequest> respondToRequest(
            @PathVariable UUID requestId,
            @RequestParam FriendRequestStatus response) throws NotFoundException {
        FriendRequest request = friendService.respondToFriendRequest(requestId, response);
        return ResponseEntity.ok(request);
    }

    @GetMapping("/{userId}/friends")
    public ResponseEntity<List<User>> getFriends(@PathVariable UUID userId) throws NotFoundException {
        List<User> friends = friendService.getFriends(userId);
        return ResponseEntity.ok(friends);
    }

    @GetMapping("/{userId}/pending-requests")
    public ResponseEntity<List<PendingFriendRequestsResponseDto>> getPendingRequests(@PathVariable UUID userId)
            throws NotFoundException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException());
        List<PendingFriendRequestsResponseDto> requests = friendService.getPendingRequests(user);
        return ResponseEntity.ok(requests);
    }
}
