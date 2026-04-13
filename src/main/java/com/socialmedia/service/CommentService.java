package com.socialmedia.service;

import com.socialmedia.entity.Comment;
import com.socialmedia.entity.Post;
import com.socialmedia.entity.User;
import com.socialmedia.repository.CommentRepository;
import com.socialmedia.repository.PostRepository;
import com.socialmedia.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Autowired
    public CommentService(CommentRepository commentRepository, PostRepository postRepository, 
                         UserRepository userRepository, NotificationService notificationService) {
        this.commentRepository = commentRepository;
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    public String addComment(int postID, int userID, Comment comment) {
        Optional<Post> postOpt = postRepository.findById(postID);
        Optional<User> userOpt = userRepository.findById(userID);

        if (postOpt.isPresent() && userOpt.isPresent()) {
            Post post = postOpt.get();
            User user = userOpt.get();

            comment.setPost(post);
            comment.setUser(user);
            comment.setTimestamp(LocalDateTime.now());
            commentRepository.save(comment);

            notificationService.createNotification(post.getUser(), 
                user.getUsername() + " commented on your post: " + comment.getCommentText());

            return "Comment added successfully!";
        }
        return "Error: Post or User not found!";
    }

    public List<Comment> getCommentsByPostId(int postID) {
        return commentRepository.findByPost_PostID(postID);
    }
}
