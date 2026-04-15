package com.socialmedia.service;

import com.socialmedia.entity.Comment;
import com.socialmedia.entity.Post;
import com.socialmedia.entity.User;
import com.socialmedia.repository.CommentRepository;
import com.socialmedia.repository.PostRepository;
import com.socialmedia.repository.UserRepository;
import com.socialmedia.exception.GlobalExceptionHandler.*;
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
        throw new ResourceNotFoundException("Error: Post or User not found!");
    }

    public List<Comment> getCommentsByPostId(int postID) {
        return commentRepository.findByPost_PostID(postID);
    }
    
    public List<Comment> getCommentsByPostAndUser(int postID, int userID) {
        return commentRepository.findByPost_PostIDAndUser_UserID(postID, userID);
    }
    
    public Comment getCommentById(int commentID) {
        return commentRepository.findById(commentID)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found!"));
    }
    
    public List<Comment> getCommentsByUser(int userID) {
        return commentRepository.findByUser_UserID(userID);
    }
    
    public String deleteComment(int commentID) {
        if (commentRepository.existsById(commentID)) {
            commentRepository.deleteById(commentID);
            return "Comment deleted successfully!";
        } else {
            throw new ResourceNotFoundException("Error: Comment not found!");
        }
    }
    
    public String updateComment(int commentID, Comment updatedComment) {
        Optional<Comment> commentOpt = commentRepository.findById(commentID);

        if (commentOpt.isPresent()) {
            Comment comment = commentOpt.get();

            comment.setCommentText(updatedComment.getCommentText());
            comment.setTimestamp(java.time.LocalDateTime.now());

            commentRepository.save(comment);
            return "Comment updated successfully!";
        }

        throw new ResourceNotFoundException("Error: Comment not found!");
    }
    
    public long getCommentCountByPost(int postID) {
        return commentRepository.countByPost_PostID(postID);
    }

    public long getCommentCountByUser(int userID) {
        return commentRepository.countByUser_UserID(userID);
    }
    
    public String deleteCommentsByPost(int postID) {
        commentRepository.deleteByPost_PostID(postID);
        return "All comments for this post deleted!";
    }

    public String deleteCommentsByUser(int userID) {
        commentRepository.deleteByUser_UserID(userID);
        return "All comments of this user deleted!";
    }
}
