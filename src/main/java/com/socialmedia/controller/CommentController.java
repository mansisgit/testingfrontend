package com.socialmedia.controller;

import com.socialmedia.entity.Comment;
import com.socialmedia.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comments")
public class CommentController {

    private final CommentService commentService;

    @Autowired
    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping("/add")
    public String addComment(@RequestParam int postID, @RequestParam int userID, @RequestBody Comment comment) {
        return commentService.addComment(postID, userID, comment);
    }

    @GetMapping("/post/{postID}")
    public List<Comment> getCommentsByPostId(@PathVariable int postID) {
        return commentService.getCommentsByPostId(postID);
    }
}
