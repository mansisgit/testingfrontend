package com.socialmedia.controller;

import com.socialmedia.entity.Like;
import com.socialmedia.service.LikeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/likes")
public class LikeController {

    private final LikeService likeService;

    @Autowired
    public LikeController(LikeService likeService) {
        this.likeService = likeService;
    }

    @PostMapping("/add")
    public String likePost(@RequestParam int postID, @RequestParam int userID, @RequestBody Like like) {
        return likeService.likePost(postID, userID, like);
    }

    @GetMapping("/count/{postID}")
    public long getLikeCount(@PathVariable int postID) {
        return likeService.getLikeCount(postID);
    }
}
