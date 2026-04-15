package com.socialmedia.repository;

import com.socialmedia.entity.Like;
import com.socialmedia.entity.Post;
import com.socialmedia.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LikeRepository extends JpaRepository<Like, Integer> {
    Optional<Like> findByPostAndUser(Post post, User user);
    long countByPost_PostID(int postID);
    void deleteByPostAndUser(Post post, User user);
    List<Like> findByPost_PostID(int postID);
    long countByUser_UserID(int userID);
    @Query("SELECT l.post, COUNT(l) as likeCount FROM Like l GROUP BY l.post ORDER BY likeCount DESC")
    List<Object[]> findTopLikedPosts();
    void deleteByPost_PostID(int postID);
    void deleteByUser_UserID(int userID);
    Optional<Like> findById(int likeID);
}
