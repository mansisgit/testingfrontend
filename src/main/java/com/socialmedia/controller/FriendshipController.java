package com.socialmedia.controller;

import com.socialmedia.dto.FriendRequestDto;
import com.socialmedia.entity.Friendship;
import com.socialmedia.entity.Notification;
import com.socialmedia.service.FriendshipService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/friendships")
public class FriendshipController {

    private final FriendshipService friendshipService;

    public FriendshipController(FriendshipService friendshipService) {
        this.friendshipService = friendshipService;
    }

    @PostMapping("/request")
    public ResponseEntity<String> sendRequest(@RequestBody FriendRequestDto dto) {
        String response = friendshipService.sendFriendRequest(
                dto.getFriendshipId(),
                dto.getSenderId(),
                dto.getReceiverId()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{friendshipID}/accept")
    public ResponseEntity<String> acceptRequest(@PathVariable int friendshipID) {
        return ResponseEntity.ok(friendshipService.acceptFriendRequest(friendshipID));
    }

    @PatchMapping("/{friendshipID}/reject")
    public ResponseEntity<String> rejectRequest(@PathVariable int friendshipID) {
        return ResponseEntity.ok(friendshipService.rejectFriendRequest(friendshipID));
    }

    @GetMapping("/{userID}/list")
    public ResponseEntity<List<Friendship>> getFriendsList(@PathVariable int userID) {
        return ResponseEntity.ok(friendshipService.getFriendsList(userID));
    }

    @GetMapping("/{userID}/requests/incoming")
    public ResponseEntity<List<Friendship>> getIncomingRequests(@PathVariable int userID) {
        return ResponseEntity.ok(friendshipService.getIncomingRequests(userID));
    }

    @GetMapping("/{userID}/requests/outgoing")
    public ResponseEntity<List<Friendship>> getOutgoingRequests(@PathVariable int userID) {
        return ResponseEntity.ok(friendshipService.getOutgoingRequests(userID));
    }

    @GetMapping("/status")
    public ResponseEntity<String> checkStatus(
            @RequestParam int userID,
            @RequestParam int targetID) {
        return ResponseEntity.ok(friendshipService.checkStatus(userID, targetID));
    }

    @GetMapping("/{userID}/notifications")
    public ResponseEntity<List<Notification>> getFriendNotifications(@PathVariable int userID) {
        return ResponseEntity.ok(friendshipService.getFriendNotifications(userID));
    }
}