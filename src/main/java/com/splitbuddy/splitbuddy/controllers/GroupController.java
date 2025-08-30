package com.splitbuddy.splitbuddy.controllers;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import com.splitbuddy.splitbuddy.dto.request.CreateGroupRequest;
import com.splitbuddy.splitbuddy.dto.response.GroupResponse;
import com.splitbuddy.splitbuddy.models.Group;
import com.splitbuddy.splitbuddy.services.GroupService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;

    @GetMapping
    public ResponseEntity<List<GroupResponse>> getAllGroups() {
        List<Group> groups = groupService.getAllGroups();
        List<GroupResponse> responses = groups.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/me")
    public ResponseEntity<List<GroupResponse>> getMyGroups() {
        List<Group> groups = groupService.getMyGroups();
        List<GroupResponse> responses = groups.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @PostMapping
    public ResponseEntity<GroupResponse> createGroup(@Valid @RequestBody CreateGroupRequest request) {
        Group group = groupService.createGroup(request);
        return ResponseEntity.ok(convertToResponse(group));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGroup(@PathVariable String id) {
        groupService.deleteGroup(id);
        return ResponseEntity.noContent().build();
    }

    private GroupResponse convertToResponse(Group group) {
        GroupResponse response = new GroupResponse();
        response.setId(group.getId());
        response.setName(group.getName());
        response.setCreatedBy(group.getCreatedBy().getId());
        response.setCreatedByName(group.getCreatedBy().getName());

        List<GroupResponse.GroupMemberResponse> memberResponses = group.getMembers().stream()
                .map(member -> {
                    GroupResponse.GroupMemberResponse memberResponse = new GroupResponse.GroupMemberResponse();
                    memberResponse.setId(member.getId());
                    memberResponse.setName(member.getName());
                    memberResponse.setEmail(member.getEmail());
                    return memberResponse;
                })
                .collect(Collectors.toList());
        response.setMembers(memberResponses);

        return response;
    }
}
