package com.splitbuddy.splitbuddy.controllers;

import com.splitbuddy.splitbuddy.dto.request.FriendRequestDto;
import com.splitbuddy.splitbuddy.dto.response.FriendResponse;
import com.splitbuddy.splitbuddy.dto.response.PendingFriendRequestsResponseDto;
import com.splitbuddy.splitbuddy.models.FriendRequestStatus;
import com.splitbuddy.splitbuddy.services.FriendService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/friends")
@CrossOrigin(origins = "*")
public class FriendController {

    @Autowired
    private FriendService friendService;

    @GetMapping("/test")
    public ResponseEntity<String> testEndpoint() {
        return ResponseEntity.ok("Friend controller is working!");
    }

    @PostMapping("/requests")
    public ResponseEntity<String> sendFriendRequest(@Valid @RequestBody FriendRequestDto request) {
        try {
            friendService.sendFriendRequest(request);
            return new ResponseEntity<>("Friend request sent successfully", HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to send friend request: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/requests/{requestId}")
    public ResponseEntity<String> respondToFriendRequest(
            @PathVariable Long requestId,
            @RequestParam String response) {
        try {
            FriendRequestStatus status;
            if ("ACCEPTED".equalsIgnoreCase(response)) {
                status = FriendRequestStatus.ACCEPTED;
            } else if ("REJECTED".equalsIgnoreCase(response)) {
                status = FriendRequestStatus.REJECTED;
            } else {
                return new ResponseEntity<>("Invalid response. Use 'ACCEPTED' or 'REJECTED'", HttpStatus.BAD_REQUEST);
            }

            friendService.respondToFriendRequest(requestId, status);
            return ResponseEntity.ok("Friend request " + response.toLowerCase());
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to respond to friend request: " + e.getMessage(),
                    HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/requests/{requestId}/accept")
    public ResponseEntity<String> acceptFriendRequest(@PathVariable Long requestId) {
        try {
            friendService.acceptFriendRequest(requestId);
            return ResponseEntity.ok("Friend request accepted");
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to accept friend request: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/requests/{requestId}/reject")
    public ResponseEntity<String> rejectFriendRequest(@PathVariable Long requestId) {
        try {
            friendService.rejectFriendRequest(requestId);
            return ResponseEntity.ok("Friend request rejected");
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to reject friend request: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/{userId}")
    public ResponseEntity<List<FriendResponse>> getFriends(@PathVariable Long userId) {
        try {
            List<FriendResponse> friends = friendService.getFriends(userId);
            return ResponseEntity.ok(friends);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/{userId}/pending-requests")
    public ResponseEntity<List<PendingFriendRequestsResponseDto>> getPendingRequests(@PathVariable Long userId) {
        try {
            List<PendingFriendRequestsResponseDto> requests = friendService.getPendingRequests(userId);
            return ResponseEntity.ok(requests);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
}
