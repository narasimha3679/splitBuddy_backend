package com.splitbuddy.splitbuddy.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.splitbuddy.splitbuddy.dto.request.CreateGroupRequest;
import com.splitbuddy.splitbuddy.exceptions.GroupNotFoundException;
import com.splitbuddy.splitbuddy.models.Group;
import com.splitbuddy.splitbuddy.models.User;
import com.splitbuddy.splitbuddy.services.GroupService;

@WebMvcTest(GroupController.class)
class GroupControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GroupService groupService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(username = "test-user-id")
    void getAllGroups_ShouldReturn200() throws Exception {
        // Given
        List<Group> groups = Arrays.asList(new Group(), new Group());
        when(groupService.getAllGroups()).thenReturn(groups);

        // When & Then
        mockMvc.perform(get("/api/groups"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @WithMockUser(username = "test-user-id")
    void getMyGroups_ShouldReturn200() throws Exception {
        // Given
        List<Group> groups = Arrays.asList(new Group(), new Group());
        when(groupService.getMyGroups()).thenReturn(groups);

        // When & Then
        mockMvc.perform(get("/api/groups/me"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @WithMockUser(username = "test-user-id")
    void createGroup_ShouldReturn200() throws Exception {
        // Given
        CreateGroupRequest request = new CreateGroupRequest();
        request.setName("Test Group");

        Group group = new Group();
        group.setName("Test Group");
        when(groupService.createGroup(any(CreateGroupRequest.class))).thenReturn(group);

        // When & Then
        mockMvc.perform(post("/api/groups")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Group"));
    }

    @Test
    @WithMockUser(username = "test-user-id")
    void deleteGroup_ShouldReturn204() throws Exception {
        // Given
        String groupId = UUID.randomUUID().toString();

        // When & Then
        mockMvc.perform(delete("/api/groups/{id}", groupId))
                .andExpect(status().isNoContent());
    }

}
