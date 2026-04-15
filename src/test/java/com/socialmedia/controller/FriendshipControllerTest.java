package com.socialmedia.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.socialmedia.dto.FriendRequestDto;
import com.socialmedia.service.FriendshipService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// 1. We tell Spring to ONLY load the Controller layer, keeping the test blazing fast!
@WebMvcTest(FriendshipController.class)
// 2. We disable Spring Security (JWT) just for this test so we don't get 401 Unauthorized errors!
@AutoConfigureMockMvc(addFilters = false)
public class FriendshipControllerTest {

    // 3. MockMvc is our "fake Postman". It simulates HTTP requests for us.
    @Autowired
    private MockMvc mockMvc;

    // 4. We mock the Service layer, because the Controller shouldn't care how the Service works.
    @MockBean
    private FriendshipService friendshipService;

    // We also mock JwtUtils because Spring Security tries to load your JwtAuthFilter!
    @MockBean
    private com.socialmedia.security.JwtUtils jwtUtils;

    // 5. Converts our Java Objects into JSON strings
    @Autowired
    private ObjectMapper objectMapper;



    @Test
    void testSendRequest_Returns201Created() throws Exception {
        // Arrange
        FriendRequestDto requestDto = new FriendRequestDto();
        requestDto.setFriendshipId(999);
        requestDto.setSenderId(1);
        requestDto.setReceiverId(2);

        // Tell the mocked service what to return when called
        when(friendshipService.sendFriendRequest(999, 1, 2))
                .thenReturn("Friend request sent successfully!");

        // Act & Assert (Using our fake Postman)
        mockMvc.perform(post("/api/v1/friendships/request")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto))) // Converts DTO to JSON
                .andExpect(status().isCreated()) // Expect HTTP 201 Created
                .andExpect(content().string("Friend request sent successfully!")); // Expect exact text
    }

    /* ====================================
     * TEST: GET /api/v1/friendships/{userID}/count
     * ==================================== */

    @Test
    void testGetFriendCount_Returns200Ok() throws Exception {
        // Arrange
        when(friendshipService.getFriendCount(5)).thenReturn(42L);

        // Act & Assert
        mockMvc.perform(get("/api/v1/friendships/5/count"))
                .andExpect(status().isOk()) // Expect HTTP 200 OK
                .andExpect(content().string("42")); 
    }

    /* ====================================
     * TEST: PUT /api/v1/friendships/{friendshipID}/accept
     * ==================================== */

    @Test
    void testAcceptRequest_Returns200Ok() throws Exception {
        // Arrange
        when(friendshipService.acceptFriendRequest(100))
                .thenReturn("Friend request accepted successfully!");

        // Act & Assert
        mockMvc.perform(put("/api/v1/friendships/100/accept"))
                .andExpect(status().isOk())
                .andExpect(content().string("Friend request accepted successfully!"));
    }
}
