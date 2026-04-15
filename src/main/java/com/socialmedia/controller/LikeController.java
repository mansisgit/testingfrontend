package com.socialmedia.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.socialmedia.entity.Like;
import com.socialmedia.service.LikeService;

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
    
    @DeleteMapping("/remove")
    public String removeLike(@RequestParam int postID, @RequestParam int userID) {
        return likeService.removeLike(postID, userID);
    }
    
    @GetMapping("/check")
    public boolean checkLike(@RequestParam int postID, @RequestParam int userID) {
        return likeService.isPostLikedByUser(postID, userID);
    }
    
    @GetMapping("/post/{postID}")
    public List<Like> getLikesByPost(@PathVariable int postID) {
        return likeService.getLikesByPost(postID);
    }
    
    @PostMapping("/toggle")
    public String toggleLike(@RequestParam int postID, @RequestParam int userID) {
        return likeService.toggleLike(postID, userID);
    }
    
    @GetMapping("/user/{userID}/count")
    public long getTotalLikesByUser(@PathVariable int userID) {
        return likeService.getTotalLikesByUser(userID);
    }
    
    @GetMapping("/top-posts")
    public List<Object[]> getTopLikedPosts() {
        return likeService.getTopLikedPosts();
    }
    
    @DeleteMapping("/post/{postID}")
    public String deleteLikesByPost(@PathVariable int postID) {
        return likeService.deleteLikesByPost(postID);
    }
    
    @DeleteMapping("/user/{userID}")
    public String deleteLikesByUser(@PathVariable int userID) {
        return likeService.deleteLikesByUser(userID);
    }
    
    @GetMapping("/{likeID}")
    public Like getLikeById(@PathVariable int likeID) {
        return likeService.getLikeById(likeID);
    }

    @GetMapping("/user/{userID}/post/{postID}")
    public boolean checkUserLikeOnPost(@PathVariable int userID,
                                      @PathVariable int postID) {
        return likeService.checkUserLikeOnPost(postID, userID);
    }
    
    @GetMapping("/post/{postID}/user/{userID}")
    public Like getLikeByPostAndUser(@PathVariable int postID,
                                    @PathVariable int userID) {
        return likeService.getLikeByPostAndUser(postID, userID);
    }
}
