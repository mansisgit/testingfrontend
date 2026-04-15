package com.socialmedia.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.socialmedia.entity.SocialGroup;
import com.socialmedia.entity.User;
import com.socialmedia.service.GroupService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@ExtendWith(MockitoExtension.class)
@DisplayName("GroupController Web Layer Tests")
class GroupControllerTest {

    @Mock
    private GroupService groupService;

    @InjectMocks
    private GroupController groupController;

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();


    private User adminUser;
    private SocialGroup group1;
    private SocialGroup group2;
    private SocialGroup group3;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(groupController).build();

        adminUser = new User(1, "john_doe", "john@example.com", "secret");
        group1 = new SocialGroup(101, "Java Developers", adminUser);
        group2 = new SocialGroup(102, "Spring Boot Fans", adminUser);
        group3 = new SocialGroup(103, "Cloud Architects", adminUser);
    }


    @Test
    @DisplayName("POST /api/groups/create → admin found → 200 OK with success message")
    void createGroup_whenAdminExists_shouldReturn200WithSuccessMessage() throws Exception {
        when(groupService.createGroup(anyInt(), any(SocialGroup.class)))
                .thenReturn("Group 'Java Developers' created successfully!");

        mockMvc.perform(post("/api/groups/create")
                        .param("adminID", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(group1)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("created successfully")));

        verify(groupService, times(1)).createGroup(anyInt(), any(SocialGroup.class));
    }

    @Test
    @DisplayName("POST /api/groups/create → admin not found → 200 OK with error message")
    void createGroup_whenAdminNotFound_shouldReturn200WithErrorMessage() throws Exception {
        when(groupService.createGroup(anyInt(), any(SocialGroup.class)))
                .thenReturn("Error: Admin user not found!");

        mockMvc.perform(post("/api/groups/create")
                        .param("adminID", "99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(group1)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Admin user not found")));
    }


    @Test
    @DisplayName("GET /api/groups/admin/{adminID} → returns list of groups for admin")
    void getGroupsByAdmin_shouldReturn200WithGroupsList() throws Exception {
        List<SocialGroup> groups = Arrays.asList(group1, group2);
        when(groupService.getGroupsByAdmin(1)).thenReturn(groups);

        mockMvc.perform(get("/api/groups/admin/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].groupID", is(101)))
                .andExpect(jsonPath("$[0].groupName", is("Java Developers")))
                .andExpect(jsonPath("$[1].groupID", is(102)))
                .andExpect(jsonPath("$[1].groupName", is("Spring Boot Fans")));

        verify(groupService, times(1)).getGroupsByAdmin(1);
    }

    @Test
    @DisplayName("GET /api/groups/admin/{adminID} → admin has no groups → returns empty list")
    void getGroupsByAdmin_whenNoGroups_shouldReturnEmptyList() throws Exception {
        when(groupService.getGroupsByAdmin(1)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/groups/admin/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }


    @Test
    @DisplayName("GET /api/groups/search?name=java → returns matching groups")
    void searchGroups_shouldReturn200WithMatchingGroups() throws Exception {
        when(groupService.searchGroups("java")).thenReturn(List.of(group1));

        mockMvc.perform(get("/api/groups/search")
                        .param("name", "java"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].groupName", is("Java Developers")));

        verify(groupService, times(1)).searchGroups("java");
    }

    @Test
    @DisplayName("GET /api/groups/search?name=xyz → no matches → returns empty list")
    void searchGroups_whenNoMatch_shouldReturnEmptyList() throws Exception {
        when(groupService.searchGroups("xyz")).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/groups/search")
                        .param("name", "xyz"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }


    @Test
    @DisplayName("GET /api/groups/{groupID} → group found → returns group JSON")
    void getGroupById_whenFound_shouldReturn200WithGroup() throws Exception {
        when(groupService.getGroupById(101)).thenReturn(group1);

        mockMvc.perform(get("/api/groups/101"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.groupID", is(101)))
                .andExpect(jsonPath("$.groupName", is("Java Developers")));

        verify(groupService, times(1)).getGroupById(101);
    }

    @Test
    @DisplayName("GET /api/groups/{groupID} → group not found → returns null body")
    void getGroupById_whenNotFound_shouldReturnNullBody() throws Exception {
        when(groupService.getGroupById(999)).thenReturn(null);

        mockMvc.perform(get("/api/groups/999"))
                .andExpect(status().isOk());

        verify(groupService, times(1)).getGroupById(999);
    }


    @Test
    @DisplayName("GET /api/groups → returns all groups")
    void getAllGroups_shouldReturn200WithAllGroups() throws Exception {
        List<SocialGroup> allGroups = Arrays.asList(group1, group2, group3);
        when(groupService.getAllGroups()).thenReturn(allGroups);

        mockMvc.perform(get("/api/groups"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].groupID", is(101)))
                .andExpect(jsonPath("$[1].groupID", is(102)))
                .andExpect(jsonPath("$[2].groupID", is(103)));

        verify(groupService, times(1)).getAllGroups();
    }

    @Test
    @DisplayName("GET /api/groups → no groups exist → returns empty list")
    void getAllGroups_whenEmpty_shouldReturnEmptyList() throws Exception {
        when(groupService.getAllGroups()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/groups"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }


    @Test
    @DisplayName("GET /api/groups/admin/{adminID}/count → returns count of groups")
    void countGroupsByAdmin_shouldReturn200WithCount() throws Exception {
        when(groupService.countGroupsByAdmin(1)).thenReturn(3L);

        mockMvc.perform(get("/api/groups/admin/1/count"))
                .andExpect(status().isOk())
                .andExpect(content().string("3"));

        verify(groupService, times(1)).countGroupsByAdmin(1);
    }

    @Test
    @DisplayName("GET /api/groups/admin/{adminID}/count → admin has no groups → returns 0")
    void countGroupsByAdmin_whenNoGroups_shouldReturnZero() throws Exception {
        when(groupService.countGroupsByAdmin(99)).thenReturn(0L);

        mockMvc.perform(get("/api/groups/admin/99/count"))
                .andExpect(status().isOk())
                .andExpect(content().string("0"));
    }


    @Test
    @DisplayName("PUT /api/groups/{groupID}?newName=... → group found → returns updated group JSON")
    void updateGroupName_whenGroupFound_shouldReturn200WithUpdatedGroup() throws Exception {
        SocialGroup updatedGroup = new SocialGroup(101, "Updated Group Name", adminUser);
        when(groupService.updateGroupName(eq(101), eq("Updated Group Name"))).thenReturn(updatedGroup);

        mockMvc.perform(put("/api/groups/101")
                        .param("newName", "Updated Group Name"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.groupID", is(101)))
                .andExpect(jsonPath("$.groupName", is("Updated Group Name")));

        verify(groupService, times(1)).updateGroupName(101, "Updated Group Name");
    }

    @Test
    @DisplayName("PUT /api/groups/{groupID}?newName=... → group not found → returns null body")
    void updateGroupName_whenGroupNotFound_shouldReturnNullBody() throws Exception {
        when(groupService.updateGroupName(eq(999), eq("New Name"))).thenReturn(null);

        mockMvc.perform(put("/api/groups/999")
                        .param("newName", "New Name"))
                .andExpect(status().isOk());

        verify(groupService, times(1)).updateGroupName(999, "New Name");
    }


    @Test
    @DisplayName("GET /api/groups/user/{userID} → returns groups for given user")
    void getGroupsByUser_shouldReturn200WithGroupsList() throws Exception {
        List<SocialGroup> groups = Arrays.asList(group1, group2);
        when(groupService.getGroupsByUser(1)).thenReturn(groups);

        mockMvc.perform(get("/api/groups/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].groupID", is(101)))
                .andExpect(jsonPath("$[1].groupID", is(102)));

        verify(groupService, times(1)).getGroupsByUser(1);
    }

    @Test
    @DisplayName("GET /api/groups/user/{userID} → user has no groups → returns empty list")
    void getGroupsByUser_whenNoGroups_shouldReturnEmptyList() throws Exception {
        when(groupService.getGroupsByUser(7)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/groups/user/7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }
}
