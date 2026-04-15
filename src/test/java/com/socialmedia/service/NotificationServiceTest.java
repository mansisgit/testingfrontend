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
public class NotificationServiceTest {

    @Mock NotificationRepository noteRepo;
    @Mock UserRepository userRepo;
    @InjectMocks NotificationService service;

    private User u1;
    private Notification note;

    @BeforeEach
    void setUp() {
        u1 = new User(1, "A", "a@email.com", "pass");
        note = new Notification();
        note.setNotificationID(100);
        note.setUser(u1);
        note.setContent("Test Alert");
    }

    // ─── createNotification (int, String) ───────────────────────────────────────

    @Test
    void testCreateNotification_Success() {
        when(userRepo.findById(1)).thenReturn(Optional.of(u1));

        String result = service.createNotification(1, "Alert");

        assertEquals("Notification created successfully!", result);
        verify(noteRepo, times(1)).save(any(Notification.class));
    }

    @Test
    void testCreateNotification_UserNotFound() {
        when(userRepo.findById(99)).thenReturn(Optional.empty());

        String result = service.createNotification(99, "Alert");

        assertEquals("Error: User not found!", result);
        verify(noteRepo, never()).save(any());
    }

    // ─── createNotification (User, String) — backward-compat overload ───────────

    @Test
    void testCreateNotification_UserOverload_SavesNotification() {
        // ✅ Tests the void overload called by FriendshipService etc.
        service.createNotification(u1, "Friend request accepted");

        verify(noteRepo, times(1)).save(any(Notification.class));
    }

    // ─── getUserNotifications ───────────────────────────────────────────────────

    @Test
    void testGetUserNotifications_ReturnsList() {
        when(noteRepo.findByUser_UserIDOrderByTimestampDesc(1)).thenReturn(List.of(note));

        List<Notification> result = service.getUserNotifications(1);

        assertEquals(1, result.size());
        assertEquals(100, result.get(0).getNotificationID());
    }

    @Test
    void testGetUserNotifications_Empty() {
        when(noteRepo.findByUser_UserIDOrderByTimestampDesc(1)).thenReturn(List.of());

        assertTrue(service.getUserNotifications(1).isEmpty());
    }

    // ─── getNotificationsForUser (backward-compat alias) ────────────────────────

    @Test
    void testGetNotificationsForUser_DelegatesToGetUserNotifications() {
        when(noteRepo.findByUser_UserIDOrderByTimestampDesc(1)).thenReturn(List.of(note));

        List<Notification> result = service.getNotificationsForUser(1);

        assertEquals(1, result.size());
    }

    // ─── getNotification ────────────────────────────────────────────────────────

    @Test
    void testGetNotification_Found() {
        when(noteRepo.findById(100)).thenReturn(Optional.of(note));

        Notification result = service.getNotification(100);

        assertNotNull(result);
        assertEquals(100, result.getNotificationID());
    }

    @Test
    void testGetNotification_NotFound_ReturnsNull() {
        when(noteRepo.findById(999)).thenReturn(Optional.empty());

        assertNull(service.getNotification(999));
    }

    // ─── countNotifications ─────────────────────────────────────────────────────

    @Test
    void testCountNotifications() {
        when(noteRepo.countByUser_UserID(1)).thenReturn(5L);

        assertEquals(5L, service.countNotifications(1));
    }

    @Test
    void testCountNotifications_Zero() {
        when(noteRepo.countByUser_UserID(1)).thenReturn(0L);

        assertEquals(0L, service.countNotifications(1));
    }

    // ─── deleteNotification ─────────────────────────────────────────────────────

    @Test
    void testDeleteNotification_Success() {
        when(noteRepo.existsById(100)).thenReturn(true);

        String result = service.deleteNotification(100);

        assertEquals("Notification deleted successfully!", result);
        verify(noteRepo, times(1)).deleteById(100);
    }

    @Test
    void testDeleteNotification_NotFound() {
        when(noteRepo.existsById(999)).thenReturn(false);

        String result = service.deleteNotification(999);

        assertEquals("Error: Notification not found!", result);
        verify(noteRepo, never()).deleteById(anyInt());
    }
}
