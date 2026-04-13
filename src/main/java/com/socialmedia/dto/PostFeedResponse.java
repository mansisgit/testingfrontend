package com.socialmedia.dto;

import com.socialmedia.entity.Post;
import java.util.List;

public class PostFeedResponse {
    private List<Post> posts;
    private long totalCount;
    private int loadedCount;
    private int currentPage;
    private int seed;

    public PostFeedResponse(List<Post> posts, long totalCount, int loadedCount, int currentPage, int seed) {
        this.posts = posts;
        this.totalCount = totalCount;
        this.loadedCount = loadedCount;
        this.currentPage = currentPage;
        this.seed = seed;
    }

    // Getters and Setters
    public List<Post> getPosts() {
        return posts;
    }

    public void setPosts(List<Post> posts) {
        this.posts = posts;
    }

    public long getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(long totalCount) {
        this.totalCount = totalCount;
    }

    public int getLoadedCount() {
        return loadedCount;
    }

    public void setLoadedCount(int loadedCount) {
        this.loadedCount = loadedCount;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public int getSeed() {
        return seed;
    }

    public void setSeed(int seed) {
        this.seed = seed;
    }
}
