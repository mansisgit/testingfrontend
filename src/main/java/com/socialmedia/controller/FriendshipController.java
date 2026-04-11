package com.socialmedia.controller;

import com.socialmedia.entity.Friendship;
import com.socialmedia.service.FriendshipService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/friends")
public class FriendshipController {

    private final FriendshipService friendshipService;

    @Autowired
    public FriendshipController(FriendshipService friendshipService) {
        this.friendshipService = friendshipService;
    }

    @PostMapping("/request")
    public String sendRequest(@RequestParam int senderID, @RequestParam int receiverID, @RequestParam int friendshipID) {
        return friendshipService.sendFriendRequest(senderID, receiverID, friendshipID);
    }

    @PostMapping("/accept/{friendshipID}")
    public String acceptRequest(@PathVariable int friendshipID) {
        return friendshipService.acceptFriendRequest(friendshipID);
    }

    @GetMapping("/list/{userID}")
    public List<Friendship> getFriends(@PathVariable int userID) {
        return friendshipService.getFriendsList(userID);
    }
}


