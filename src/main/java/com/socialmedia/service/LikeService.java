package com.socialmedia.service;

import com.socialmedia.entity.Like;
import com.socialmedia.entity.Post;
import com.socialmedia.entity.User;
import com.socialmedia.repository.LikeRepository;
import com.socialmedia.repository.PostRepository;
import com.socialmedia.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class LikeService {

    private final LikeRepository likeRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Autowired
    public LikeService(LikeRepository likeRepository, PostRepository postRepository, 
                      UserRepository userRepository, NotificationService notificationService) {
        this.likeRepository = likeRepository;
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    public String likePost(int postID, int userID, Like like) {
        Optional<Post> postOpt = postRepository.findById(postID);
        Optional<User> userOpt = userRepository.findById(userID);

        if (postOpt.isPresent() && userOpt.isPresent()) {
            Post post = postOpt.get();
            User user = userOpt.get();

            // Check if already liked
            if (likeRepository.findByPostAndUser(post, user).isPresent()) {
                return "Error: User already liked this post!";
            }

            like.setPost(post);
            like.setUser(user);
            like.setTimestamp(LocalDateTime.now());
            likeRepository.save(like);

            notificationService.createNotification(post.getUser(), 
                user.getUsername() + " liked your post: " + post.getContent());

            return "Post liked successfully!";
        }
        return "Error: Post or User not found!";
    }

    public long getLikeCount(int postID) {
        return likeRepository.countByPost_PostID(postID);
    }
}
