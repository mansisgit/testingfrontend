package com.socialmedia.service;

import com.socialmedia.entity.User;
import com.socialmedia.repository.UserRepository;
import com.socialmedia.security.JwtUtils;
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
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtUtils jwtUtils;

    @InjectMocks
    private UserService userService;

    private User sampleUser;

    @BeforeEach
    void setUp() {
        sampleUser = new User(1, "testuser", "test@example.com", "password123");
    }

    @Test
    void testRegisterUser_Success() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(sampleUser);

        String result = userService.registerUser(sampleUser);

        assertEquals("User registered successfully!", result);
        verify(userRepository, times(1)).save(sampleUser);
    }

    @Test
    void testRegisterUser_UsernameTaken() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(sampleUser));

        assertThrows(RuntimeException.class, () -> userService.registerUser(sampleUser));
    }

    @Test
    void testLoginUser_Success() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(sampleUser));
        when(jwtUtils.generateToken("testuser")).thenReturn("token123");

        String token = userService.loginUser("testuser", "password123");

        assertEquals("token123", token);
    }

    @Test
    void testLoginUser_InvalidPassword() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(sampleUser));

        assertThrows(RuntimeException.class, () -> userService.loginUser("testuser", "wrongpassword"));
    }

    @Test
    void testGetUserById_Success() {
        when(userRepository.findById(1)).thenReturn(Optional.of(sampleUser));

        User user = userService.getUserById(1);

        assertNotNull(user);
        assertEquals("testuser", user.getUsername());
    }

    @Test
    void testUpdateUser() {
        when(userRepository.findById(1)).thenReturn(Optional.of(sampleUser));
        when(userRepository.save(any(User.class))).thenReturn(sampleUser);

        User updated = new User(1, "newname", "new@email.com", "newpass");
        User result = userService.updateUser(1, updated);

        assertEquals("newname", result.getUsername());
        assertEquals("new@email.com", result.getEmail());
    }

    @Test
    void testDeleteUser() {
        when(userRepository.findById(1)).thenReturn(Optional.of(sampleUser));

        String result = userService.deleteUser(1);

        assertEquals("User deleted successfully!", result);
        verify(userRepository, times(1)).delete(sampleUser);
    }
}
