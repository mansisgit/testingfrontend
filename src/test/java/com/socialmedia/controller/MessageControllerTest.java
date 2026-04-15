package com.socialmedia.controller;

import com.socialmedia.entity.Message;
import com.socialmedia.security.JwtUtils;
import com.socialmedia.service.MessageService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MessageController.class)
@AutoConfigureMockMvc(addFilters = false)
public class MessageControllerTest {

    @Autowired MockMvc mockMvc;
    @MockBean MessageService service;
    @MockBean JwtUtils jwtUtils;

    // ─── POST /api/messages/send ─────────────────────────────────────────────────

    @Test
    void testSendMessage_Success() throws Exception {
        when(service.sendMessage(anyInt(), anyInt(), anyInt(), anyString()))
                .thenReturn("Message sent successfully!");

        // ✅ Fixed: added contentType(TEXT_PLAIN) to match @RequestBody String
        mockMvc.perform(post("/api/messages/send")
                        .param("senderID", "1")
                        .param("receiverID", "2")
                        .param("messageID", "10")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("Hi"))
               .andExpect(status().isOk())
               .andExpect(content().string("Message sent successfully!"));
    }

    @Test
    void testSendMessage_NotFriends() throws Exception {
        when(service.sendMessage(anyInt(), anyInt(), anyInt(), anyString()))
                .thenReturn("Error: You can only message your friends!");

        mockMvc.perform(post("/api/messages/send")
                        .param("senderID", "1")
                        .param("receiverID", "2")
                        .param("messageID", "10")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("Hi"))
               .andExpect(status().isOk())
               .andExpect(content().string("Error: You can only message your friends!"));
    }

    // ─── GET /api/messages/inbox/{userID} ────────────────────────────────────────

    @Test
    void testGetInbox() throws Exception {
        Message msg = new Message();
        msg.setMessageID(10);
        when(service.getInbox(1)).thenReturn(List.of(msg));

        mockMvc.perform(get("/api/messages/inbox/1"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$[0].messageID").value(10));
    }

    @Test
    void testGetInbox_Empty() throws Exception {
        when(service.getInbox(1)).thenReturn(List.of());

        mockMvc.perform(get("/api/messages/inbox/1"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.length()").value(0));
    }

    // ─── GET /api/messages/sent/{userID} ─────────────────────────────────────────

    @Test
    void testGetSent() throws Exception {
        Message msg = new Message();
        msg.setMessageID(20);
        when(service.getSent(1)).thenReturn(List.of(msg));

        mockMvc.perform(get("/api/messages/sent/1"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$[0].messageID").value(20));
    }

    // ─── GET /api/messages/conversation ──────────────────────────────────────────

    @Test
    void testGetConversation() throws Exception {
        Message msg = new Message();
        msg.setMessageID(30);
        when(service.getConversation(1, 2)).thenReturn(List.of(msg));

        mockMvc.perform(get("/api/messages/conversation")
                        .param("user1ID", "1")
                        .param("user2ID", "2"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$[0].messageID").value(30));
    }

    // ─── DELETE /api/messages/{messageID} ────────────────────────────────────────

    @Test
    void testDeleteMessage_Success() throws Exception {
        when(service.deleteMessage(10)).thenReturn("Message deleted successfully!");

        mockMvc.perform(delete("/api/messages/10"))
               .andExpect(status().isOk())   // ✅ Added explicit status check
               .andExpect(content().string("Message deleted successfully!"));
    }

    @Test
    void testDeleteMessage_NotFound() throws Exception {
        when(service.deleteMessage(999)).thenReturn("Error: Message not found!");

        mockMvc.perform(delete("/api/messages/999"))
               .andExpect(status().isOk())
               .andExpect(content().string("Error: Message not found!"));
    }

    // ─── GET /api/messages/count ──────────────────────────────────────────────────

    @Test
    void testGetMessageCount() throws Exception {
        when(service.countMessagesBetweenUsers(1, 2)).thenReturn(5L);

        mockMvc.perform(get("/api/messages/count")
                        .param("user1ID", "1")
                        .param("user2ID", "2"))
               .andExpect(status().isOk())   // ✅ Added explicit status check
               .andExpect(content().string("5"));
    }

    @Test
    void testGetMessageCount_Zero() throws Exception {
        when(service.countMessagesBetweenUsers(1, 99)).thenReturn(0L);

        mockMvc.perform(get("/api/messages/count")
                        .param("user1ID", "1")
                        .param("user2ID", "99"))
               .andExpect(status().isOk())
               .andExpect(content().string("0"));
    }
}
