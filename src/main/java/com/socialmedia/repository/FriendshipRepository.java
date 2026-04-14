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

    Optional<Friendship> findByUser1AndUser2(User user1, User user2);

    boolean existsByUser1AndUser2AndStatus(User user1, User user2, FriendshipStatus status);

    List<Friendship> findByUser1_UserIDOrUser2_UserIDAndStatus(
            int userID1, int userID2, FriendshipStatus status);

    List<Friendship> findByUser2_UserIDAndStatus(int userID, FriendshipStatus status);

    List<Friendship> findByUser1_UserIDAndStatus(int userID, FriendshipStatus status);
}