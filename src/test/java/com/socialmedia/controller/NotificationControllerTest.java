package com.socialmedia.controller;

import com.socialmedia.entity.Notification;
import com.socialmedia.security.JwtUtils;
import com.socialmedia.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NotificationController.class)
@AutoConfigureMockMvc(addFilters = false)
public class NotificationControllerTest {

    @Autowired MockMvc mockMvc;
    @MockBean NotificationService service;
    @MockBean JwtUtils jwtUtils;

    // ─── POST /api/notifications/create ──────────────────────────────────────────

    @Test
    void testCreateNotification_Success() throws Exception {
        when(service.createNotification(anyInt(), anyString())).thenReturn("Notification created successfully!");

        mockMvc.perform(post("/api/notifications/create")
                        .param("userID", "1")
                        .param("content", "Alert"))
               .andExpect(status().isOk())
               .andExpect(content().string("Notification created successfully!"));
    }

    @Test
    void testCreateNotification_UserNotFound() throws Exception {
        when(service.createNotification(anyInt(), anyString())).thenReturn("Error: User not found!");

        mockMvc.perform(post("/api/notifications/create")
                        .param("userID", "99")
                        .param("content", "Alert"))
               .andExpect(status().isOk())
               .andExpect(content().string("Error: User not found!"));
    }

    // ─── GET /api/notifications/user/{userID} ────────────────────────────────────

    @Test
    void testGetUserNotifications() throws Exception {
        Notification note = new Notification();
        note.setNotificationID(50);
        when(service.getUserNotifications(1)).thenReturn(List.of(note));

        mockMvc.perform(get("/api/notifications/user/1"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$[0].notificationID").value(50));
    }

    @Test
    void testGetUserNotifications_Empty() throws Exception {
        when(service.getUserNotifications(1)).thenReturn(List.of());

        mockMvc.perform(get("/api/notifications/user/1"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.length()").value(0));
    }

    // ─── GET /api/notifications/{notificationID} ─────────────────────────────────

    @Test
    void testGetNotification_Found() throws Exception {
        Notification note = new Notification();
        note.setNotificationID(50);
        note.setContent("You have a message");
        when(service.getNotification(50)).thenReturn(note);

        mockMvc.perform(get("/api/notifications/50"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.notificationID").value(50));
    }

    // ─── DELETE /api/notifications/{notificationID} ──────────────────────────────

    @Test
    void testDeleteNotification_Success() throws Exception {
        when(service.deleteNotification(50)).thenReturn("Notification deleted successfully!");

        mockMvc.perform(delete("/api/notifications/50"))
               .andExpect(status().isOk())   // ✅ Explicit status check added
               .andExpect(content().string("Notification deleted successfully!"));
    }

    @Test
    void testDeleteNotification_NotFound() throws Exception {
        when(service.deleteNotification(999)).thenReturn("Error: Notification not found!");

        mockMvc.perform(delete("/api/notifications/999"))
               .andExpect(status().isOk())
               .andExpect(content().string("Error: Notification not found!"));
    }

    // ─── GET /api/notifications/count/{userID} ────────────────────────────────────

    @Test
    void testCountNotifications() throws Exception {
        when(service.countNotifications(1)).thenReturn(3L);

        mockMvc.perform(get("/api/notifications/count/1"))
               .andExpect(status().isOk())   // ✅ Explicit status check added
               .andExpect(content().string("3"));
    }

    @Test
    void testCountNotifications_Zero() throws Exception {
        when(service.countNotifications(1)).thenReturn(0L);

        mockMvc.perform(get("/api/notifications/count/1"))
               .andExpect(status().isOk())
               .andExpect(content().string("0"));
    }
}
