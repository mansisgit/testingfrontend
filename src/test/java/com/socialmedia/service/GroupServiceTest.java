package com.socialmedia.service;

import com.socialmedia.entity.SocialGroup;
import com.socialmedia.entity.User;
import com.socialmedia.repository.GroupRepository;
import com.socialmedia.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GroupService Unit Tests")
class GroupServiceTest {

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private GroupService groupService;


    private User adminUser;
    private SocialGroup group1;
    private SocialGroup group2;

    @BeforeEach
    void setUp() {
        adminUser = new User(1, "john_doe", "john@example.com", "secret");

        group1 = new SocialGroup(101, "Java Developers", adminUser);
        group2 = new SocialGroup(102, "Spring Boot Fans", adminUser);
    }


    @Test
    @DisplayName("createGroup: admin found → saves group and returns success message")
    void createGroup_whenAdminExists_shouldSaveAndReturnSuccess() {
        when(userRepository.findById(1)).thenReturn(Optional.of(adminUser));
        when(groupRepository.save(group1)).thenReturn(group1);

        String result = groupService.createGroup(1, group1);

        assertEquals("Group 'Java Developers' created successfully!", result);
        verify(userRepository, times(1)).findById(1);
        verify(groupRepository, times(1)).save(group1);
    }

    @Test
    @DisplayName("createGroup: admin not found → returns error message, save never called")
    void createGroup_whenAdminNotFound_shouldReturnErrorMessage() {
        when(userRepository.findById(99)).thenReturn(Optional.empty());

        String result = groupService.createGroup(99, group1);

        assertEquals("Error: Admin user not found!", result);
        verify(userRepository, times(1)).findById(99);
        verify(groupRepository, never()).save(any());
    }


    @Test
    @DisplayName("getGroupsByAdmin: returns all groups for given admin ID")
    void getGroupsByAdmin_shouldReturnListOfGroups() {
        List<SocialGroup> groups = Arrays.asList(group1, group2);
        when(groupRepository.findByAdmin_UserID(1)).thenReturn(groups);

        List<SocialGroup> result = groupService.getGroupsByAdmin(1);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Java Developers", result.get(0).getGroupName());
        verify(groupRepository, times(1)).findByAdmin_UserID(1);
    }

    @Test
    @DisplayName("getGroupsByAdmin: admin has no groups → returns empty list")
    void getGroupsByAdmin_whenNoGroups_shouldReturnEmptyList() {
        when(groupRepository.findByAdmin_UserID(1)).thenReturn(Collections.emptyList());

        List<SocialGroup> result = groupService.getGroupsByAdmin(1);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }


    @Test
    @DisplayName("searchGroups: returns groups matching keyword (case-insensitive)")
    void searchGroups_shouldReturnMatchingGroups() {
        when(groupRepository.findByGroupNameContainingIgnoreCase("java"))
                .thenReturn(List.of(group1));

        List<SocialGroup> result = groupService.searchGroups("java");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.get(0).getGroupName().toLowerCase().contains("java"));
        verify(groupRepository, times(1)).findByGroupNameContainingIgnoreCase("java");
    }

    @Test
    @DisplayName("searchGroups: no matching groups → returns empty list")
    void searchGroups_whenNoMatch_shouldReturnEmptyList() {
        when(groupRepository.findByGroupNameContainingIgnoreCase("xyz"))
                .thenReturn(Collections.emptyList());

        List<SocialGroup> result = groupService.searchGroups("xyz");

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }


    @Test
    @DisplayName("getGroupById: group found → returns the group")
    void getGroupById_whenExists_shouldReturnGroup() {
        when(groupRepository.findById(101)).thenReturn(Optional.of(group1));

        SocialGroup result = groupService.getGroupById(101);

        assertNotNull(result);
        assertEquals(101, result.getGroupID());
        assertEquals("Java Developers", result.getGroupName());
        verify(groupRepository, times(1)).findById(101);
    }

    @Test
    @DisplayName("getGroupById: group not found → returns null")
    void getGroupById_whenNotFound_shouldReturnNull() {
        when(groupRepository.findById(999)).thenReturn(Optional.empty());

        SocialGroup result = groupService.getGroupById(999);

        assertNull(result);
        verify(groupRepository, times(1)).findById(999);
    }


    @Test
    @DisplayName("getAllGroups: returns all groups in the repository")
    void getAllGroups_shouldReturnAllGroups() {
        List<SocialGroup> allGroups = Arrays.asList(group1, group2);
        when(groupRepository.findAll()).thenReturn(allGroups);

        List<SocialGroup> result = groupService.getAllGroups();

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(groupRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("getAllGroups: repository is empty → returns empty list")
    void getAllGroups_whenEmpty_shouldReturnEmptyList() {
        when(groupRepository.findAll()).thenReturn(Collections.emptyList());

        List<SocialGroup> result = groupService.getAllGroups();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }


    @Test
    @DisplayName("countGroupsByAdmin: returns correct count for admin")
    void countGroupsByAdmin_shouldReturnCount() {
        when(groupRepository.countByAdmin_UserID(1)).thenReturn(5L);

        long result = groupService.countGroupsByAdmin(1);

        assertEquals(5L, result);
        verify(groupRepository, times(1)).countByAdmin_UserID(1);
    }

    @Test
    @DisplayName("countGroupsByAdmin: admin has no groups → returns 0")
    void countGroupsByAdmin_whenNoGroups_shouldReturnZero() {
        when(groupRepository.countByAdmin_UserID(99)).thenReturn(0L);

        long result = groupService.countGroupsByAdmin(99);

        assertEquals(0L, result);
    }


    @Test
    @DisplayName("updateGroupName: group found → updates name and returns saved group")
    void updateGroupName_whenGroupExists_shouldUpdateAndReturnGroup() {
        when(groupRepository.findById(101)).thenReturn(Optional.of(group1));

        SocialGroup updatedGroup = new SocialGroup(101, "Updated Group Name", adminUser);
        when(groupRepository.save(any(SocialGroup.class))).thenReturn(updatedGroup);

        SocialGroup result = groupService.updateGroupName(101, "Updated Group Name");

        assertNotNull(result);
        assertEquals("Updated Group Name", result.getGroupName());
        verify(groupRepository, times(1)).findById(101);
        verify(groupRepository, times(1)).save(any(SocialGroup.class));
    }

    @Test
    @DisplayName("updateGroupName: group not found → returns null, save never called")
    void updateGroupName_whenGroupNotFound_shouldReturnNull() {
        when(groupRepository.findById(999)).thenReturn(Optional.empty());

        SocialGroup result = groupService.updateGroupName(999, "New Name");

        assertNull(result);
        verify(groupRepository, times(1)).findById(999);
        verify(groupRepository, never()).save(any());
    }


    @Test
    @DisplayName("getGroupsByUser: returns groups for given user ID (delegates to admin lookup)")
    void getGroupsByUser_shouldReturnGroupsForUser() {
        List<SocialGroup> groups = Arrays.asList(group1, group2);
        when(groupRepository.findByAdmin_UserID(1)).thenReturn(groups);

        List<SocialGroup> result = groupService.getGroupsByUser(1);

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(groupRepository, times(1)).findByAdmin_UserID(1);
    }

    @Test
    @DisplayName("getGroupsByUser: user has no groups → returns empty list")
    void getGroupsByUser_whenNoGroups_shouldReturnEmptyList() {
        when(groupRepository.findByAdmin_UserID(7)).thenReturn(Collections.emptyList());

        List<SocialGroup> result = groupService.getGroupsByUser(7);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}
