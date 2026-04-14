package com.socialmedia.repository;

import com.socialmedia.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Integer> {

    List<Post> findByUser_UserID(int userID);
    List<Post> findByContentContainingIgnoreCase(String keyword);
}