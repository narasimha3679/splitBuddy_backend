package com.splitbuddy.splitbuddy.services;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.splitbuddy.splitbuddy.dto.request.CreateGroupRequest;
import com.splitbuddy.splitbuddy.exceptions.GroupNotFoundException;
import com.splitbuddy.splitbuddy.exceptions.InvalidOperationException;
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
        Long userId = Long.valueOf(userIdString);
        return groupRepository.findByMembers_Id(userId);
    }

    public Group getGroupById(String groupId) {
        try {
            Long id = Long.valueOf(groupId);
            return groupRepository.findById(id)
                    .orElseThrow(() -> new GroupNotFoundException("Group not found with ID: " + groupId));
        } catch (NumberFormatException e) {
            throw new GroupNotFoundException("Invalid group ID format: " + groupId);
        }
    }

    public Group createGroup(CreateGroupRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String creatorIdString = authentication.getName();
        Long creatorId = Long.valueOf(creatorIdString);

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
                    Long memberId = Long.valueOf(memberIdString);
                    if (memberId.equals(creatorId)) {
                        continue; // already added
                    }
                    Optional<User> userOpt = userRepository.findById(memberId);
                    userOpt.ifPresent(members::add);
                } catch (NumberFormatException ignored) {
                    // skip invalid Long values silently
                }
            }
        }

        group.setMembers(members);
        return groupRepository.save(group);
    }

    @Transactional
    public Group addMembers(String groupId, List<String> memberIds) {
        Group group = getGroupById(groupId);
        
        // Verify current user has permission (must be creator or member)
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long currentUserId = Long.valueOf(authentication.getName());
        
        boolean isMember = group.getMembers().stream()
                .anyMatch(m -> m.getId().equals(currentUserId));
        
        if (!isMember) {
            throw new InvalidOperationException("Only group members can add new members");
        }

        Set<User> members = new HashSet<>(group.getMembers());
        
        for (String memberIdString : memberIds) {
            try {
                Long memberId = Long.valueOf(memberIdString);
                Optional<User> userOpt = userRepository.findById(memberId);
                userOpt.ifPresent(members::add);
            } catch (NumberFormatException ignored) {
                // skip invalid Long values silently
            }
        }
        
        group.setMembers(members);
        return groupRepository.save(group);
    }

    @Transactional
    public void removeMember(String groupId, String userId) {
        Group group = getGroupById(groupId);
        
        // Verify current user has permission (must be creator)
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long currentUserId = Long.valueOf(authentication.getName());
        
        if (!group.getCreatedBy().getId().equals(currentUserId)) {
            throw new InvalidOperationException("Only the group creator can remove members");
        }

        Long memberToRemoveId;
        try {
            memberToRemoveId = Long.valueOf(userId);
        } catch (NumberFormatException e) {
            throw new UserNotFoundException("Invalid user ID format: " + userId);
        }

        // Cannot remove the creator
        if (group.getCreatedBy().getId().equals(memberToRemoveId)) {
            throw new InvalidOperationException("Cannot remove the group creator");
        }

        // Find and remove the member
        Set<User> members = new HashSet<>(group.getMembers());
        boolean removed = members.removeIf(m -> m.getId().equals(memberToRemoveId));
        
        if (!removed) {
            throw new UserNotFoundException("User is not a member of this group");
        }
        
        group.setMembers(members);
        groupRepository.save(group);
    }

    public void deleteGroup(String id) {
        try {
            Long groupId = Long.valueOf(id);
            if (!groupRepository.existsById(groupId)) {
                throw new GroupNotFoundException("Group not found with ID: " + id);
            }
            groupRepository.deleteById(groupId);
        } catch (NumberFormatException e) {
            throw new GroupNotFoundException("Invalid group ID format: " + id);
        }
    }
}

