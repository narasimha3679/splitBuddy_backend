package com.splitbuddy.splitbuddy.controllers;

import com.splitbuddy.splitbuddy.dto.request.FriendRequestDto;
import com.splitbuddy.splitbuddy.dto.response.FriendResponse;
import com.splitbuddy.splitbuddy.models.FriendRequestStatus;
import com.splitbuddy.splitbuddy.services.FriendService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import com.splitbuddy.splitbuddy.dto.response.FriendRequestResponse;

/**
 * Friend Controller
 * 
 * Handles friend requests and friend management.
 * 
 * Frontend Types: See expo/splitbuddy/src/types/api-contracts.ts
 * - FriendRequestDto, FriendRequestResponse, FriendResponse
 * 
 * API Documentation: See backend/API_DOCUMENTATION.md#friends
 * OpenAPI Spec: See backend/openapi.yaml#/paths/friends
 * 
 * @see expo/splitbuddy/src/utils/api.ts for frontend API functions
 */
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
    public ResponseEntity<FriendRequestResponse> sendFriendRequest(@Valid @RequestBody FriendRequestDto request) {
        try {
            FriendRequestResponse response = friendService.sendFriendRequest(request);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/requests/{requestId}")
    public ResponseEntity<FriendRequestResponse> respondToFriendRequest(
            @PathVariable Long requestId,
            @RequestParam String response) {
        try {
            FriendRequestStatus status;
            if ("ACCEPTED".equalsIgnoreCase(response)) {
                status = FriendRequestStatus.ACCEPTED;
            } else if ("REJECTED".equalsIgnoreCase(response)) {
                status = FriendRequestStatus.REJECTED;
            } else {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }

            FriendRequestResponse friendRequestResponse = friendService.respondToFriendRequest(requestId, status);
            return ResponseEntity.ok(friendRequestResponse);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/requests/{requestId}/accept")
    public ResponseEntity<FriendRequestResponse> acceptFriendRequest(@PathVariable Long requestId) {
        try {
            FriendRequestResponse response = friendService.acceptFriendRequest(requestId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/requests/{requestId}/reject")
    public ResponseEntity<FriendRequestResponse> rejectFriendRequest(@PathVariable Long requestId) {
        try {
            FriendRequestResponse response = friendService.rejectFriendRequest(requestId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
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
    public ResponseEntity<List<FriendRequestResponse>> getPendingRequests(@PathVariable Long userId) {
        try {
            List<FriendRequestResponse> requests = friendService.getPendingRequests(userId);
            return ResponseEntity.ok(requests);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/requests/{requestId}")
    public ResponseEntity<FriendRequestResponse> getFriendRequest(@PathVariable Long requestId) {
        try {
            FriendRequestResponse request = friendService.getFriendRequest(requestId);
            return ResponseEntity.ok(request);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
}
