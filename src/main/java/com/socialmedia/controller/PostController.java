package com.socialmedia.controller;

import com.socialmedia.entity.Post;
import com.socialmedia.service.PostService;
import com.socialmedia.dto.PostFeedResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;

    @Autowired
    public PostController(PostService postService) {
        this.postService = postService;
    }

    @PostMapping("/create")
    public String createPost(@RequestParam int userID, @RequestBody Post post) {
        return postService.createPost(userID, post);
    }

    @GetMapping("/all")
    public List<Post> getAllPosts() {
        return postService.getAllPosts();
    }

    @GetMapping("/user/{userID}")
    public List<Post> getPostsByUserId(@PathVariable int userID) {
        return postService.getPostsByUserId(userID);
    }

    @GetMapping("/feed")
    public ResponseEntity<PostFeedResponse> getFeed(
            @RequestParam(required = false) Integer seed,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int limit) {
        return ResponseEntity.ok(postService.getPaginatedFeed(seed, page, limit));
    }
}
