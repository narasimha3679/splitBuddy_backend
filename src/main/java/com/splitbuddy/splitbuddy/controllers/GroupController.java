package com.splitbuddy.splitbuddy.controllers;

import java.util.List;

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
import com.splitbuddy.splitbuddy.models.Group;
import com.splitbuddy.splitbuddy.services.GroupService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;

    @GetMapping
    public ResponseEntity<List<Group>> getAllGroups() {
        List<Group> groups = groupService.getAllGroups();
        return ResponseEntity.ok(groups);
    }

    @GetMapping("/me")
    public ResponseEntity<List<Group>> getMyGroups() {
        List<Group> groups = groupService.getMyGroups();
        return ResponseEntity.ok(groups);
    }

    @PostMapping
    public ResponseEntity<Group> createGroup(@Valid @RequestBody CreateGroupRequest request) {
        Group group = groupService.createGroup(request);
        return ResponseEntity.ok(group);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGroup(@PathVariable String id) {
        groupService.deleteGroup(id);
        return ResponseEntity.noContent().build();
    }
}
