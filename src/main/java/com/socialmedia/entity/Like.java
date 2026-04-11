package com.socialmedia.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "Likes")
public class Like {

    @Id
    @Column(name = "likeID")
    private int likeID;

    @ManyToOne
    @JoinColumn(name = "postID", nullable = false)
    private Post post;

    @ManyToOne
    @JoinColumn(name = "userID", nullable = false)
    private User user;

    @Column(name = "timestamp")
    private LocalDateTime timestamp;

    // Default Constructor
    public Like() {
    }

    // Parameterized Constructor
    public Like(int likeID, Post post, User user, LocalDateTime timestamp) {
        this.likeID = likeID;
        this.post = post;
        this.user = user;
        this.timestamp = timestamp;
    }

    // Getters and Setters
    public int getLikeID() {
        return likeID;
    }

    public void setLikeID(int likeID) {
        this.likeID = likeID;
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

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
