package com.socialmedia.service;

import com.socialmedia.entity.*;
import com.socialmedia.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MessageServiceTest {

    @Mock MessageRepository msgRepo;
    @Mock UserRepository userRepo;
    @Mock FriendshipRepository friendRepo;
    @InjectMocks MessageService service;

    // ✅ Use @BeforeEach to avoid field-level init issues with Mockito
    private User u1;
    private User u2;
    private Message msg;

    @BeforeEach
    void setUp() {
        u1 = new User(1, "A", "a@email.com", "pass");
        u2 = new User(2, "B", "b@email.com", "pass");
        msg = new Message();
    }

    // ─── sendMessage ────────────────────────────────────────────────────────────

    @Test
    void testSendMessage_Success() {
        when(userRepo.findById(1)).thenReturn(Optional.of(u1));
        when(userRepo.findById(2)).thenReturn(Optional.of(u2));
        // Service checks BOTH directions of friendship — mock only the needed one
        when(friendRepo.existsByUser1AndUser2AndStatus(u1, u2, FriendshipStatus.accepted)).thenReturn(true);

        String result = service.sendMessage(1, 2, 100, "Hi");

        assertEquals("Message sent successfully!", result);
        verify(msgRepo, times(1)).save(any(Message.class));
    }

    @Test
    void testSendMessage_NotFriends() {
        when(userRepo.findById(1)).thenReturn(Optional.of(u1));
        when(userRepo.findById(2)).thenReturn(Optional.of(u2));
        // Both directions return false → not friends
        when(friendRepo.existsByUser1AndUser2AndStatus(u1, u2, FriendshipStatus.accepted)).thenReturn(false);
        when(friendRepo.existsByUser1AndUser2AndStatus(u2, u1, FriendshipStatus.accepted)).thenReturn(false);

        String result = service.sendMessage(1, 2, 100, "Hi");

        assertEquals("Error: You can only message your friends!", result);
        verify(msgRepo, never()).save(any());
    }

    @Test
    void testSendMessage_FriendsViaReverseDirection() {
        // ✅ Tests the OR branch: receiver is User1 in friendship table
        when(userRepo.findById(1)).thenReturn(Optional.of(u1));
        when(userRepo.findById(2)).thenReturn(Optional.of(u2));
        when(friendRepo.existsByUser1AndUser2AndStatus(u1, u2, FriendshipStatus.accepted)).thenReturn(false);
        when(friendRepo.existsByUser1AndUser2AndStatus(u2, u1, FriendshipStatus.accepted)).thenReturn(true);

        assertEquals("Message sent successfully!", service.sendMessage(1, 2, 100, "Hello"));
        verify(msgRepo).save(any(Message.class));
    }

    @Test
    void testSendMessage_SenderNotFound() {
        when(userRepo.findById(1)).thenReturn(Optional.empty());
        when(userRepo.findById(2)).thenReturn(Optional.of(u2));

        assertEquals("Error: Sender or Receiver not found!", service.sendMessage(1, 2, 100, "Hi"));
        verify(msgRepo, never()).save(any());
    }

    @Test
    void testSendMessage_ReceiverNotFound() {
        when(userRepo.findById(1)).thenReturn(Optional.of(u1));
        when(userRepo.findById(2)).thenReturn(Optional.empty());

        assertEquals("Error: Sender or Receiver not found!", service.sendMessage(1, 2, 100, "Hi"));
        verify(msgRepo, never()).save(any());
    }

    // ─── getInbox / getSent / getConversation ───────────────────────────────────

    @Test
    void testGetInbox() {
        when(msgRepo.findByReceiver_UserIDOrderByTimestampDesc(2)).thenReturn(List.of(msg));

        List<Message> inbox = service.getInbox(2);

        assertEquals(1, inbox.size());
        verify(msgRepo).findByReceiver_UserIDOrderByTimestampDesc(2);
    }

    @Test
    void testGetSent() {
        when(msgRepo.findBySender_UserIDOrderByTimestampDesc(1)).thenReturn(List.of(msg));

        List<Message> sent = service.getSent(1);

        assertEquals(1, sent.size());
        verify(msgRepo).findBySender_UserIDOrderByTimestampDesc(1);
    }

    @Test
    void testGetConversation_Success() {
        when(userRepo.findById(1)).thenReturn(Optional.of(u1));
        when(userRepo.findById(2)).thenReturn(Optional.of(u2));
        when(msgRepo.findBySenderAndReceiverOrReceiverAndSenderOrderByTimestampAsc(u1, u2, u1, u2))
                .thenReturn(List.of(msg));

        List<Message> convo = service.getConversation(1, 2);

        assertEquals(1, convo.size());
    }

    @Test
    void testGetConversation_UserNotFound_ReturnsEmpty() {
        when(userRepo.findById(1)).thenReturn(Optional.empty());
        when(userRepo.findById(2)).thenReturn(Optional.of(u2));

        List<Message> convo = service.getConversation(1, 2);

        assertTrue(convo.isEmpty());
        verify(msgRepo, never()).findBySenderAndReceiverOrReceiverAndSenderOrderByTimestampAsc(any(), any(), any(), any());
    }

    // ─── deleteMessage ──────────────────────────────────────────────────────────

    @Test
    void testDeleteMessage_Success() {
        when(msgRepo.existsById(100)).thenReturn(true);

        assertEquals("Message deleted successfully!", service.deleteMessage(100));
        verify(msgRepo).deleteById(100);
    }

    @Test
    void testDeleteMessage_NotFound() {
        when(msgRepo.existsById(999)).thenReturn(false);

        assertEquals("Error: Message not found!", service.deleteMessage(999));
        verify(msgRepo, never()).deleteById(anyInt());
    }

    // ─── countMessagesBetweenUsers ──────────────────────────────────────────────

    @Test
    void testCountMessages_Success() {
        when(userRepo.findById(1)).thenReturn(Optional.of(u1));
        when(userRepo.findById(2)).thenReturn(Optional.of(u2));
        // ✅ Service calls countBySenderAndReceiver TWICE (u1→u2 and u2→u1)
        when(msgRepo.countBySenderAndReceiver(u1, u2)).thenReturn(3L);
        when(msgRepo.countBySenderAndReceiver(u2, u1)).thenReturn(2L);

        assertEquals(5L, service.countMessagesBetweenUsers(1, 2));
    }

    @Test
    void testCountMessages_UserNotFound_ReturnsZero() {
        when(userRepo.findById(1)).thenReturn(Optional.empty());
        when(userRepo.findById(2)).thenReturn(Optional.of(u2));

        assertEquals(0L, service.countMessagesBetweenUsers(1, 2));
    }
}
