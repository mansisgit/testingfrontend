package com.socialmedia.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "Friends")
public class Friendship {

    @Id
    @Column(name = "friendshipID")
    private int friendshipID;

    @ManyToOne
    @JoinColumn(name = "userID1", nullable = false)
    private User user1;

    @ManyToOne
    @JoinColumn(name = "userID2", nullable = false)
    private User user2;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FriendshipStatus status;

    public Friendship() {
    }

    public int getFriendshipID() {
        return friendshipID;
    }

    public void setFriendshipID(int friendshipID) {
        this.friendshipID = friendshipID;
    }

    public User getUser1() {
        return user1;
    }

    public void setUser1(User user1) {
        this.user1 = user1;
    }

    public User getUser2() {
        return user2;
    }

    public void setUser2(User user2) {
        this.user2 = user2;
    }

    public FriendshipStatus getStatus() {
        return status;
    }

    public void setStatus(FriendshipStatus status) {
        this.status = status;
    }
}