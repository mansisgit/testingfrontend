package com.socialmedia.service;

import com.socialmedia.entity.User;
import com.socialmedia.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final com.socialmedia.security.JwtUtils jwtUtils;

    @Autowired
    public UserService(UserRepository userRepository, com.socialmedia.security.JwtUtils jwtUtils) {
        this.userRepository = userRepository;
        this.jwtUtils = jwtUtils;
    }

    public String registerUser(User user) {
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            throw new RuntimeException("Error: Username is already taken!");
        }
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new RuntimeException("Error: Email is already registered!");
        }
        userRepository.save(user);
        return "User registered successfully!";
    }

    public String loginUser(String username, String password) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (user.getPassword().equals(password)) {
                // Return JWT token on successful login
                return jwtUtils.generateToken(username);
            } else {
                throw new RuntimeException("Error: Invalid password!");
            }
        }
        throw new RuntimeException("Error: User not found!");
    }
}
