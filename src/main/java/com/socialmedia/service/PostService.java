package com.socialmedia.service;

import com.socialmedia.dto.PostFeedResponse;
import com.socialmedia.entity.Post;
import com.socialmedia.entity.User;
import com.socialmedia.repository.PostRepository;
import com.socialmedia.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;

    @Autowired
    public PostService(PostRepository postRepository, UserRepository userRepository) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
    }

    public String createPost(int userID, Post post) {
        Optional<User> userOpt = userRepository.findById(userID);
        if (userOpt.isPresent()) {
            post.setUser(userOpt.get());
            post.setTimestamp(LocalDateTime.now());
            postRepository.save(post);
            return "Post created successfully!";
        }
        return "Error: User not found!";
    }

    public List<Post> getAllPosts() {
        return postRepository.findAll();
    }

    public List<Post> getPostsByUserId(int userID) {
        return postRepository.findByUser_UserID(userID);
    }

    public PostFeedResponse getPaginatedFeed(Integer seed, int page, int limit) {
        // Generate new seed if not provided
        int resolvedSeed = (seed == null) ? new Random().nextInt(100000) : seed;

        // Fetch ALL posts
        List<Post> allPosts = postRepository.findAll();

        // Shuffle using the seed (consistent order for same seed)
        Collections.shuffle(allPosts, new Random(resolvedSeed));

        // Manual pagination
        int fromIndex = page * limit;
        int toIndex = Math.min(fromIndex + limit, allPosts.size());

        if (fromIndex >= allPosts.size()) {
            return new PostFeedResponse(Collections.emptyList(), allPosts.size(), 0, page, resolvedSeed);
        }

        List<Post> paginated = allPosts.subList(fromIndex, toIndex);

        return new PostFeedResponse(paginated, allPosts.size(), paginated.size(), page, resolvedSeed);
    }
}
