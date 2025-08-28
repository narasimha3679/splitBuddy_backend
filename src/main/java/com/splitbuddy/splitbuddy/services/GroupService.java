package com.splitbuddy.splitbuddy.services;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.splitbuddy.splitbuddy.dto.request.CreateGroupRequest;
import com.splitbuddy.splitbuddy.exceptions.GroupNotFoundException;
import com.splitbuddy.splitbuddy.exceptions.UserNotFoundException;
import com.splitbuddy.splitbuddy.models.Group;
import com.splitbuddy.splitbuddy.models.User;
import com.splitbuddy.splitbuddy.repositories.GroupRepository;
import com.splitbuddy.splitbuddy.repositories.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GroupService {

    private final GroupRepository groupRepository;
    private final UserRepository userRepository;

    public List<Group> getAllGroups() {
        return groupRepository.findAll();
    }

    public List<Group> getMyGroups() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userIdString = authentication.getName();
        UUID userId = UUID.fromString(userIdString);
        return groupRepository.findByMembers_Id(userId);
    }

    public Group createGroup(CreateGroupRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String creatorIdString = authentication.getName();
        UUID creatorId = UUID.fromString(creatorIdString);

        User creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new UserNotFoundException("Creator not found with ID: " + creatorId));

        Group group = new Group();
        group.setName(request.getName());
        group.setCreatedBy(creator);

        Set<User> members = new HashSet<>();
        // Add creator as a member by default
        members.add(creator);

        if (request.getMemberIds() != null) {
            for (String memberIdString : request.getMemberIds()) {
                try {
                    UUID memberId = UUID.fromString(memberIdString);
                    if (memberId.equals(creatorId)) {
                        continue; // already added
                    }
                    Optional<User> userOpt = userRepository.findById(memberId);
                    userOpt.ifPresent(members::add);
                } catch (IllegalArgumentException ignored) {
                    // skip invalid UUIDs silently
                }
            }
        }

        group.setMembers(members);
        return groupRepository.save(group);
    }

    public void deleteGroup(String id) {
        try {
            UUID groupId = UUID.fromString(id);
            if (!groupRepository.existsById(groupId)) {
                throw new GroupNotFoundException("Group not found with ID: " + id);
            }
            groupRepository.deleteById(groupId);
        } catch (IllegalArgumentException e) {
            throw new GroupNotFoundException("Invalid group ID format: " + id);
        }
    }
}
