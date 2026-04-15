package com.socialmedia.service;

import com.socialmedia.entity.Friendship;
import com.socialmedia.entity.FriendshipStatus;
import com.socialmedia.entity.User;
import com.socialmedia.repository.FriendshipRepository;
import com.socialmedia.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FriendshipServiceTest {

    // 1. We MOCK the dependencies so we don't accidentally touch the real database!
    @Mock
    private FriendshipRepository friendshipRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private NotificationService notificationService;

    // 2. We inject those fake Mock dependencies into the REAL service we want to test.
    @InjectMocks
    private FriendshipService friendshipService;

    // We keep some test users ready.
    private User sender;
    private User receiver;

    @BeforeEach
    void setUp() {
        // This runs before EVERY test to give us fresh objects.
        sender = new User(1, "senderUser", "sender@test.com", "password");
        receiver = new User(2, "receiverUser", "receiver@test.com", "password");
    }

    /* ====================================
     * TEST: sendFriendRequest()
     * ==================================== */

    @Test
    void testSendFriendRequest_Success() {
        // Arrange: Teach our mocks what to return when called!
        when(userRepository.findById(1)).thenReturn(Optional.of(sender));
        when(userRepository.findById(2)).thenReturn(Optional.of(receiver));
        when(friendshipRepository.existsById(100)).thenReturn(false);
        when(friendshipRepository.findByUser1AndUser2(sender, receiver)).thenReturn(Optional.empty());
        when(friendshipRepository.findByUser1AndUser2(receiver, sender)).thenReturn(Optional.empty());

        // Act: Run the real method
        String response = friendshipService.sendFriendRequest(100, 1, 2);

        // Assert: Make sure it worked properly and returned the correct string.
        assertEquals("Friend request sent successfully!", response);
        
        // Assert: Make sure 'save()' was called exactly ONE time!
        verify(friendshipRepository, times(1)).save(any(Friendship.class));
        
        // Assert: Make sure 'createNotification()' was called ONE time!
        verify(notificationService, times(1)).createNotification(eq(receiver), anyString());
    }

    @Test
    void testSendFriendRequest_FailsWhenSendingToSelf() {
        // Act
        String response = friendshipService.sendFriendRequest(100, 1, 1);

        // Assert
        assertEquals("Error: You cannot send a friend request to yourself!", response);
        
        // Ensure that it never touched the database save method!
        verify(friendshipRepository, never()).save(any());
    }

    @Test
    void testSendFriendRequest_FailsWhenUserNotFound() {
        // Arrange
        when(userRepository.findById(999)).thenReturn(Optional.empty()); // simulate missing user

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            friendshipService.sendFriendRequest(100, 999, 2);
        });

        assertTrue(exception.getMessage().contains("Sender not found"));
    }

    /* ====================================
     * TEST: acceptFriendRequest()
     * ==================================== */

    @Test
    void testAcceptFriendRequest_Success() {
        // Arrange
        Friendship pendingFriendship = new Friendship();
        pendingFriendship.setFriendshipID(200);
        pendingFriendship.setUser1(sender);
        pendingFriendship.setUser2(receiver);
        pendingFriendship.setStatus(FriendshipStatus.pending);

        when(friendshipRepository.findById(200)).thenReturn(Optional.of(pendingFriendship));

        // Act
        String response = friendshipService.acceptFriendRequest(200);

        // Assert
        assertEquals("Friend request accepted successfully!", response);
        assertEquals(FriendshipStatus.accepted, pendingFriendship.getStatus()); // make sure it flipped to accepted
        verify(friendshipRepository, times(1)).save(pendingFriendship);
        verify(notificationService, times(1)).createNotification(eq(sender), anyString());
    }
    /* ====================================
     * TEST: rejectFriendRequest()
     * ==================================== */

    @Test
    void testRejectFriendRequest_Success() {
        Friendship pendingFriendship = new Friendship();
        pendingFriendship.setStatus(FriendshipStatus.pending);

        when(friendshipRepository.findById(300)).thenReturn(Optional.of(pendingFriendship));

        String response = friendshipService.rejectFriendRequest(300);

        assertEquals("Friend request rejected!", response);
        verify(friendshipRepository, times(1)).delete(pendingFriendship);
    }

    /* ====================================
     * TEST: getFriendsList()
     * ==================================== */

    @Test
    void testGetFriendsList() {
        when(friendshipRepository.findByUser1_UserIDOrUser2_UserIDAndStatus(1, 1, FriendshipStatus.accepted))
                .thenReturn(java.util.List.of(new Friendship()));

        var list = friendshipService.getFriendsList(1);
        assertEquals(1, list.size());
    }

    /* ====================================
     * TEST: getIncoming & Outgoing Requests
     * ==================================== */

    @Test
    void testGetIncomingRequests() {
        when(friendshipRepository.findByUser2_UserIDAndStatus(2, FriendshipStatus.pending))
                .thenReturn(java.util.List.of(new Friendship()));
        var list = friendshipService.getIncomingRequests(2);
        assertEquals(1, list.size());
    }

    @Test
    void testGetOutgoingRequests() {
        when(friendshipRepository.findByUser1_UserIDAndStatus(1, FriendshipStatus.pending))
                .thenReturn(java.util.List.of(new Friendship()));
        var list = friendshipService.getOutgoingRequests(1);
        assertEquals(1, list.size());
    }

    /* ====================================
     * TEST: checkStatus()
     * ==================================== */

    @Test
    void testCheckStatus_WhenExists() {
        when(userRepository.findById(1)).thenReturn(Optional.of(sender));
        when(userRepository.findById(2)).thenReturn(Optional.of(receiver));
        
        Friendship f = new Friendship();
        f.setStatus(FriendshipStatus.accepted);
        when(friendshipRepository.findByUser1AndUser2(sender, receiver)).thenReturn(Optional.of(f));

        String status = friendshipService.checkStatus(1, 2);
        assertEquals("Status: accepted", status);
    }

    /* ====================================
     * TEST: Bonus Endpoints (Count, Mutual)
     * ==================================== */

    @Test
    void testGetFriendCount() {
        when(friendshipRepository.findByUser1_UserIDOrUser2_UserIDAndStatus(1, 1, FriendshipStatus.accepted))
                .thenReturn(java.util.List.of(new Friendship(), new Friendship()));

        long count = friendshipService.getFriendCount(1);
        assertEquals(2, count);
    }

    @Test
    void testAreFriends_True() {
        when(userRepository.findById(1)).thenReturn(Optional.of(sender));
        when(userRepository.findById(2)).thenReturn(Optional.of(receiver));
        when(friendshipRepository.existsByUser1AndUser2AndStatus(sender, receiver, FriendshipStatus.accepted))
                .thenReturn(true);

        assertTrue(friendshipService.areFriends(1, 2));
    }

    @Test
    void testGetMutualFriends() {
        Friendship f1 = new Friendship();
        f1.setUser1(sender);
        f1.setUser2(new User(3, "mutual", "m@m.com", "p"));

        Friendship f2 = new Friendship();
        f2.setUser1(receiver);
        f2.setUser2(new User(3, "mutual", "m@m.com", "p"));

        when(friendshipRepository.findByUser1_UserIDOrUser2_UserIDAndStatus(1, 1, FriendshipStatus.accepted))
                .thenReturn(java.util.List.of(f1));
        when(friendshipRepository.findByUser1_UserIDOrUser2_UserIDAndStatus(2, 2, FriendshipStatus.accepted))
                .thenReturn(java.util.List.of(f2));

        var mutuals = friendshipService.getMutualFriends(1, 2);
        assertEquals(1, mutuals.size());
        assertEquals(3, mutuals.get(0).getUserID());
    }
}
