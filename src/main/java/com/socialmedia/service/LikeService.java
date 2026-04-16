package com.socialmedia.service;

import com.socialmedia.entity.Like;
import com.socialmedia.entity.Post;
import com.socialmedia.entity.User;
import com.socialmedia.repository.LikeRepository;
import com.socialmedia.repository.PostRepository;
import com.socialmedia.repository.UserRepository;
import com.socialmedia.exception.GlobalExceptionHandler.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
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

            if (likeRepository.findByPostAndUser(post, user).isPresent()) {
                throw new DuplicateResourceException("Error: User already liked this post!");
            }

            like.setPost(post);
            like.setUser(user);
            like.setTimestamp(LocalDateTime.now());
            likeRepository.save(like);

            notificationService.createNotification(post.getUser(), 
                user.getUsername() + " liked your post: " + post.getContent());

            return "Post liked successfully!";
        }
        throw new ResourceNotFoundException("Error: Post or User not found!");
    }

    public long getLikeCount(int postID) {
        return likeRepository.countByPost_PostID(postID);
    }
    
    public String removeLike(int postID, int userID) {
        Optional<Post> postOpt = postRepository.findById(postID);
        Optional<User> userOpt = userRepository.findById(userID);

        if (postOpt.isPresent() && userOpt.isPresent()) {
            Post post = postOpt.get();
            User user = userOpt.get();

            // Check if like exists
            Optional<Like> likeOpt = likeRepository.findByPostAndUser(post, user);

            if (likeOpt.isPresent()) {
                likeRepository.deleteByPostAndUser(post, user);
                return "Like removed successfully!";
            } else {
                throw new ResourceNotFoundException("Error: Like does not exist!");
            }
        }
        throw new ResourceNotFoundException("Error: Post or User not found!");
    }
    
    public boolean isPostLikedByUser(int postID, int userID) {
        Optional<Post> postOpt = postRepository.findById(postID);
        Optional<User> userOpt = userRepository.findById(userID);

        if (postOpt.isPresent() && userOpt.isPresent()) {
            return likeRepository.findByPostAndUser(postOpt.get(), userOpt.get()).isPresent();
        }
        return false;
    }
    
    public List<Like> getLikesByPost(int postID) {
        return likeRepository.findByPost_PostID(postID);
    }
    
    public String toggleLike(int postID, int userID) {
        Optional<Post> postOpt = postRepository.findById(postID);
        Optional<User> userOpt = userRepository.findById(userID);

        if (postOpt.isPresent() && userOpt.isPresent()) {
            Post post = postOpt.get();
            User user = userOpt.get();

            Optional<Like> likeOpt = likeRepository.findByPostAndUser(post, user);

            if (likeOpt.isPresent()) {
                likeRepository.delete(likeOpt.get());
                return "Like removed!";
            } else {
                Like like = new Like();
                like.setPost(post);
                like.setUser(user);
                like.setTimestamp(java.time.LocalDateTime.now());
                likeRepository.save(like);
                return "Post liked!";
            }
        }
        throw new ResourceNotFoundException("Error: Post or User not found!");
    }
    
    public long getTotalLikesByUser(int userID) {
        return likeRepository.countByUser_UserID(userID);
    }
    
    public List<Object[]> getTopLikedPosts() {
        return likeRepository.findTopLikedPosts();
    }
    
    public String deleteLikesByPost(int postID) {
        likeRepository.deleteByPost_PostID(postID);
        return "All likes for this post deleted!";
    }
    
    public String deleteLikesByUser(int userID) {
        likeRepository.deleteByUser_UserID(userID);
        return "All likes of this user deleted!";
    }
    
    public Like getLikeById(int likeID) {
        return likeRepository.findById(likeID)
                .orElseThrow(() -> new ResourceNotFoundException("Like not found!"));
    }

    public boolean checkUserLikeOnPost(int postID, int userID) {
        Optional<Post> postOpt = postRepository.findById(postID);
        Optional<User> userOpt = userRepository.findById(userID);

        if (postOpt.isPresent() && userOpt.isPresent()) {
            return likeRepository.findByPostAndUser(postOpt.get(), userOpt.get()).isPresent();
        }
        return false;
    }
    
    public Like getLikeByPostAndUser(int postID, int userID) {
        Optional<Post> postOpt = postRepository.findById(postID);
        Optional<User> userOpt = userRepository.findById(userID);

        if (postOpt.isPresent() && userOpt.isPresent()) {
            return likeRepository.findByPostAndUser(postOpt.get(), userOpt.get())
                    .orElseThrow(() -> new ResourceNotFoundException("Like not found!"));
        }

        throw new ResourceNotFoundException("Post or User not found!");
    }
}
