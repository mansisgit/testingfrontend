package com.socialmedia.repository;

import com.socialmedia.entity.Friendship;
import com.socialmedia.entity.FriendshipStatus;
import com.socialmedia.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FriendshipRepository extends JpaRepository<Friendship, Integer> {
    
    // Find friendship between two specific users
    Optional<Friendship> findByUser1AndUser2(User user1, User user2);
    
    // Find all friendships for a specific user (where they are either user1 or user2)
    List<Friendship> findByUser1_UserIDOrUser2_UserID(int userID1, int userID2);

    // Check if an accepted friendship exists between two users
    boolean existsByUser1AndUser2AndStatus(User user1, User user2, FriendshipStatus status);
}

