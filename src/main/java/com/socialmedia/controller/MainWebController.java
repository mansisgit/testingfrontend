package com.socialmedia.controller;

import com.socialmedia.entity.*;
import com.socialmedia.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

@Controller
public class MainWebController {

    private final UserService userService;
    private final PostService postService;
    private final MessageService messageService;
    private final NotificationService notificationService;
    private final FriendshipService friendshipService;
    private final GroupService groupService;
    private final LikeService likeService;
    private final CommentService commentService;
    private final java.util.Random random = new java.util.Random();

    @Autowired
    public MainWebController(UserService userService, PostService postService, 
                             MessageService messageService, NotificationService notificationService,
                             FriendshipService friendshipService, GroupService groupService,
                             LikeService likeService,
                             CommentService commentService) {
        this.userService = userService;
        this.postService = postService;
        this.messageService = messageService;
        this.notificationService = notificationService;
        this.friendshipService = friendshipService;
        this.groupService = groupService;
        this.likeService = likeService;
        this.commentService = commentService;
    }

    private User getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        return userService.getAllUsers().stream()
                .filter(u -> u.getUsername().equals(username))
                .findFirst()
                .orElse(null);
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String register() {
        return "register";
    }

    @PostMapping("/logout")
    public String logout(jakarta.servlet.http.HttpServletResponse response) {
        // Clear the JWT Cookie
        jakarta.servlet.http.Cookie cookie = new jakarta.servlet.http.Cookie("JWT-TOKEN", null);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(0); // Delete immediate
        response.addCookie(cookie);
        
        // Clear Security Context
        SecurityContextHolder.clearContext();
        
        return "redirect:/login?logout";
    }

    @PostMapping("/register")
    public String registerUser(User user, Model model) {
        try {
            userService.registerUser(user);
            return "redirect:/login?registered=true";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "register";
        }
    }

    @GetMapping("/")
    public String index() {
        return "redirect:/homepage";
    }

    @GetMapping("/homepage")
    public String homepage(Model model) {
        User user = getAuthenticatedUser();
        if (user == null) return "redirect:/login";

        List<Post> posts = postService.getAllPosts();
        posts.sort((p1, p2) -> {
            if (p1.getTimestamp() == null || p2.getTimestamp() == null) return 0;
            return p2.getTimestamp().compareTo(p1.getTimestamp());
        });
        
        Map<Integer, Long> likeCounts = new HashMap<>();
        Map<Integer, Boolean> userLiked = new HashMap<>();
        Map<Integer, List<Comment>> postComments = new HashMap<>();

        for (Post p : posts) {
            likeCounts.put(p.getPostID(), likeService.getLikeCount(p.getPostID()));
            userLiked.put(p.getPostID(), likeService.checkUserLikeOnPost(p.getPostID(), user.getUserID()));
            postComments.put(p.getPostID(), commentService.getCommentsByPostId(p.getPostID()));
        }

        model.addAttribute("currentUser", user);
        model.addAttribute("posts", posts);
        model.addAttribute("likeCounts", likeCounts);
        model.addAttribute("userLiked", userLiked);
        model.addAttribute("postComments", postComments);
        model.addAttribute("notificationCount", (long) notificationService.getUserNotifications(user.getUserID()).size());
        model.addAttribute("content", "feed");
        model.addAttribute("activePage", "feed");
        model.addAttribute("title", "Feed");
        return "layout";
    }

    @GetMapping("/search")
    public String search(@RequestParam(required = false) String q, Model model) {
        User user = getAuthenticatedUser();
        if (user == null) return "redirect:/login";

        List<User> userResults = new ArrayList<>();
        List<Post> postResults = new ArrayList<>();

        if (q != null && !q.trim().isEmpty()) {
            String query = q.trim();
            
            // 1. Try Post ID search if numeric
            if (query.matches("\\d+")) {
                try {
                    Post p = postService.getPostById(Integer.parseInt(query));
                    if (p != null) postResults.add(p);
                } catch (Exception ignored) {}
            }

            // 2. Search Users by Username
            userResults.addAll(userService.searchUsers(query));

            // 3. Search Posts by Content (if not already found by ID)
            List<Post> byContent = postService.searchPosts(query);
            for (Post p : byContent) {
                if (postResults.stream().noneMatch(existing -> existing.getPostID() == p.getPostID())) {
                    postResults.add(p);
                }
            }
        }

        // Prepare post metadata for display
        Map<Integer, Long> likeCounts = new HashMap<>();
        Map<Integer, Boolean> userLiked = new HashMap<>();
        Map<Integer, List<Comment>> postComments = new HashMap<>();

        for (Post p : postResults) {
            likeCounts.put(p.getPostID(), likeService.getLikeCount(p.getPostID()));
            userLiked.put(p.getPostID(), likeService.checkUserLikeOnPost(p.getPostID(), user.getUserID()));
            postComments.put(p.getPostID(), commentService.getCommentsByPostId(p.getPostID()));
        }

        model.addAttribute("currentUser", user);
        model.addAttribute("query", q);
        model.addAttribute("userResults", userResults);
        model.addAttribute("postResults", postResults);
        model.addAttribute("likeCounts", likeCounts);
        model.addAttribute("userLiked", userLiked);
        model.addAttribute("postComments", postComments);
        model.addAttribute("notificationCount", (long) notificationService.getUserNotifications(user.getUserID()).size());
        model.addAttribute("content", "search");
        model.addAttribute("title", "Search Results");
        return "layout";
    }

    @GetMapping("/profile/edit")
    public String editProfile(Model model) {
        User user = getAuthenticatedUser();
        if (user == null) return "redirect:/login";

        model.addAttribute("currentUser", user);
        model.addAttribute("content", "edit-profile");
        model.addAttribute("activePage", "profile");
        model.addAttribute("title", "Edit Profile");
        return "layout";
    }

    @PostMapping("/profile/update")
    public String updateProfile(String username, String email, String password, Model model) {
        User user = getAuthenticatedUser();
        if (user == null) return "redirect:/login";

        try {
            user.setUsername(username);
            user.setEmail(email);
            if (password != null && !password.isEmpty()) {
                user.setPassword(password); // Note: Should be hashed in production
            }
            userService.updateUser(user.getUserID(), user);
            return "redirect:/profile/" + user.getUserID() + "?updated=true";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return editProfile(model);
        }
    }

    @GetMapping("/profile/{userId}")
    public String profile(@PathVariable int userId, Model model) {
        User currentUser = getAuthenticatedUser();
        if (currentUser == null) return "redirect:/login";
        
        User profileUser = userService.getUserById(userId);

        List<Post> posts = postService.getPostsByUserId(userId);
        posts.sort((p1, p2) -> {
            if (p1.getTimestamp() == null || p2.getTimestamp() == null) return 0;
            return p2.getTimestamp().compareTo(p1.getTimestamp());
        });
        
        Map<Integer, Long> likeCounts = new HashMap<>();
        Map<Integer, Boolean> userLiked = new HashMap<>();
        Map<Integer, List<Comment>> postComments = new HashMap<>();

        for (Post p : posts) {
            likeCounts.put(p.getPostID(), likeService.getLikeCount(p.getPostID()));
            userLiked.put(p.getPostID(), likeService.checkUserLikeOnPost(p.getPostID(), currentUser.getUserID()));
            postComments.put(p.getPostID(), commentService.getCommentsByPostId(p.getPostID()));
        }

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("user", profileUser);
        model.addAttribute("posts", posts);
        model.addAttribute("likeCounts", likeCounts);
        model.addAttribute("userLiked", userLiked);
        model.addAttribute("postComments", postComments);
        model.addAttribute("friendCount", friendshipService.getFriendCount(userId));
        model.addAttribute("content", "profile");
        model.addAttribute("activePage", "profile");
        model.addAttribute("title", profileUser.getUsername() + "'s Profile");
        return "layout";
    }

    @PostMapping("/post/{postId}/like")
    @ResponseBody
    public Object likePost(@PathVariable int postId, 
                           @RequestHeader(value = "Referer", required = false) String referer,
                           @RequestHeader(value = "X-Requested-With", required = false) String requestedWith) {
        User user = getAuthenticatedUser();
        if (user != null) {
            boolean isLiked;
            if (likeService.checkUserLikeOnPost(postId, user.getUserID())) {
                likeService.removeLike(postId, user.getUserID());
                isLiked = false;
            } else {
                Like like = new Like();
                like.setLikeID(1000000 + random.nextInt(9000000));
                likeService.likePost(postId, user.getUserID(), like);
                isLiked = true;
            }

            if ("XMLHttpRequest".equals(requestedWith)) {
                Map<String, Object> response = new HashMap<>();
                response.put("isLiked", isLiked);
                response.put("newLikeCount", likeService.getLikeCount(postId));
                return response;
            }
        }
        return "redirect:" + (referer != null ? referer : "/");
    }

    @PostMapping("/post/{postId}/comment")
    @ResponseBody
    public Object addComment(@PathVariable int postId, 
                             @RequestParam String content, 
                             @RequestHeader(value = "Referer", required = false) String referer,
                             @RequestHeader(value = "X-Requested-With", required = false) String requestedWith) {
        User user = getAuthenticatedUser();
        if (user != null && !content.trim().isEmpty()) {
            Comment comment = new Comment();
            comment.setCommentID(1000000 + random.nextInt(9000000));
            comment.setCommentText(content);
            commentService.addComment(postId, user.getUserID(), comment);

            if ("XMLHttpRequest".equals(requestedWith)) {
                Map<String, Object> response = new HashMap<>();
                response.put("status", "success");
                response.put("username", user.getUsername());
                response.put("content", content);
                return response;
            }
        }
        return "redirect:" + (referer != null ? referer : "/");
    }

    @PostMapping("/messages/send")
    public String sendMessage(@RequestParam int receiverId, @RequestParam String content) {
        User sender = getAuthenticatedUser();
        if (sender != null && !content.trim().isEmpty()) {
            int messageID = 1000000 + random.nextInt(9000000);
            messageService.sendMessage(sender.getUserID(), receiverId, messageID, content);
            
            // Trigger Notification
            User receiver = userService.getUserById(receiverId);
            notificationService.createNotification(receiver, "New message from " + sender.getUsername() + ": " + content);
            
            return "redirect:/inbox?with=" + receiverId;
        }
        return "redirect:/inbox";
    }

    @PostMapping("/post/create")
    public String createPost(@RequestParam String content) {
        User user = getAuthenticatedUser();
        if (user != null && !content.trim().isEmpty()) {
            Post post = new Post();
            post.setPostID(1000000 + random.nextInt(9000000));
            post.setContent(content);
            postService.createPost(user.getUserID(), post);
        }
        return "redirect:/?posted=true";
    }

    @PostMapping("/friendship/request/{targetUserId}")
    @ResponseBody
    public Object sendRequest(@PathVariable int targetUserId, 
                              @RequestHeader(value = "Referer", required = false) String referer,
                              @RequestHeader(value = "X-Requested-With", required = false) String requestedWith) {
        User user = getAuthenticatedUser();
        if (user != null) {
            int friendshipID = 1000000 + random.nextInt(9000000);
            friendshipService.sendFriendRequest(friendshipID, user.getUserID(), targetUserId);
            
            if ("XMLHttpRequest".equals(requestedWith)) {
                Map<String, String> response = new HashMap<>();
                response.put("status", "requested");
                return response;
            }
        }
        return "redirect:" + (referer != null ? referer : "/network");
    }

    @PostMapping("/friendship/accept/{friendshipId}")
    @ResponseBody
    public Object acceptRequest(@PathVariable int friendshipId,
                                @RequestHeader(value = "X-Requested-With", required = false) String requestedWith) {
        friendshipService.acceptFriendRequest(friendshipId);
        if ("XMLHttpRequest".equals(requestedWith)) {
            Map<String, String> response = new HashMap<>();
            response.put("status", "accepted");
            return response;
        }
        return "redirect:/network";
    }

    @PostMapping("/friendship/reject/{friendshipId}")
    @ResponseBody
    public Object rejectRequest(@PathVariable int friendshipId,
                                @RequestHeader(value = "X-Requested-With", required = false) String requestedWith) {
        friendshipService.rejectFriendRequest(friendshipId);
        if ("XMLHttpRequest".equals(requestedWith)) {
            Map<String, String> response = new HashMap<>();
            response.put("status", "rejected");
            return response;
        }
        return "redirect:/network";
    }

    @PostMapping("/notification/delete/{id}")
    @ResponseBody
    public Object deleteNotification(@PathVariable int id, 
                                     @RequestHeader(value = "Referer", required = false) String referer,
                                     @RequestHeader(value = "X-Requested-With", required = false) String requestedWith) {
        notificationService.deleteNotification(id);
        if ("XMLHttpRequest".equals(requestedWith)) {
            Map<String, String> response = new HashMap<>();
            response.put("status", "deleted");
            return response;
        }
        return "redirect:" + (referer != null ? referer : "/notifications");
    }

    @GetMapping("/inbox")
    public String inbox(@RequestParam(required = false) Integer with, Model model) {
        User user = getAuthenticatedUser();
        if (user == null) return "redirect:/login";

        // Get full friends list for the sidebar
        List<Friendship> friendsList = friendshipService.getFriendsList(user.getUserID());
        List<User> friends = friendsList.stream()
                .map(f -> f.getUser1().getUserID() == user.getUserID() ? f.getUser2() : f.getUser1())
                .toList();

        if (with != null) {
            User targetFriend = userService.getUserById(with);
            if (targetFriend != null) {
                model.addAttribute("activeChat", targetFriend);
                model.addAttribute("conversation", messageService.getConversation(user.getUserID(), with));
            }
        }

        model.addAttribute("currentUser", user);
        model.addAttribute("friends", friends);
        model.addAttribute("content", "inbox");
        model.addAttribute("activePage", "inbox");
        model.addAttribute("title", "Messages");
        return "layout";
    }

    @GetMapping("/notifications")
    public String notifications(Model model) {
        User user = getAuthenticatedUser();
        if (user == null) return "redirect:/login";

        model.addAttribute("currentUser", user);
        model.addAttribute("notifications", notificationService.getUserNotifications(user.getUserID()));
        model.addAttribute("content", "notifications");
        model.addAttribute("activePage", "notifications");
        model.addAttribute("title", "Notifications");
        return "layout";
    }

    @GetMapping("/network")
    public String network(Model model) {
        User user = getAuthenticatedUser();
        if (user == null) return "redirect:/login";

        model.addAttribute("currentUser", user);
        model.addAttribute("incomingRequests", friendshipService.getIncomingRequests(user.getUserID()));
        model.addAttribute("suggestions", friendshipService.getFriendRecommendations(user.getUserID()));
        model.addAttribute("content", "network");
        model.addAttribute("activePage", "network");
        model.addAttribute("title", "Network");
        return "layout";
    }

    @PostMapping("/group/create")
    public String createGroup(@RequestParam String groupName) {
        User user = getAuthenticatedUser();
        if (user != null && !groupName.trim().isEmpty()) {
            SocialGroup group = new SocialGroup();
            group.setGroupID(1000000 + random.nextInt(9000000));
            group.setGroupName(groupName);
            groupService.createGroup(user.getUserID(), group);
        }
        return "redirect:/groups";
    }

    @GetMapping("/groups")
    public String groups(Model model) {
        User user = getAuthenticatedUser();
        if (user == null) return "redirect:/login";

        model.addAttribute("currentUser", user);
        model.addAttribute("allGroups", groupService.getAllGroups());
        model.addAttribute("userGroups", groupService.getGroupsByUser(user.getUserID()));
        model.addAttribute("content", "groups");
        model.addAttribute("activePage", "groups");
        model.addAttribute("title", "Groups");
        return "layout";
    }
}
