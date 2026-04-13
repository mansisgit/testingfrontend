package com.socialmedia.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "Comments")
public class Comment {

    @Id
    @Column(name = "commentID")
    private int commentID;

    @ManyToOne
    @JoinColumn(name = "postID", nullable = false)
    private Post post;

    @ManyToOne
    @JoinColumn(name = "userID", nullable = false)
    private User user;

    @Column(name = "comment_text", columnDefinition = "TEXT")
    private String commentText;

    @Column(name = "timestamp")
    private LocalDateTime timestamp;

    public Comment() {
    }

    public Comment(int commentID, Post post, User user, String commentText, LocalDateTime timestamp) {
        this.commentID = commentID;
        this.post = post;
        this.user = user;
        this.commentText = commentText;
        this.timestamp = timestamp;
    }

    public int getCommentID() {
        return commentID;
    }

    public void setCommentID(int commentID) {
        this.commentID = commentID;
    }

    public Post getPost() {
        return post;
    }

    public void setPost(Post post) {
        this.post = post;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getCommentText() {
        return commentText;
    }

    public void setCommentText(String commentText) {
        this.commentText = commentText;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
