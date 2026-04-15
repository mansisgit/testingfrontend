package com.socialmedia.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.socialmedia.entity.Friendship;
import com.socialmedia.entity.FriendshipStatus;
import com.socialmedia.entity.Notification;
import com.socialmedia.entity.User;
import com.socialmedia.exception.GlobalExceptionHandler;
import com.socialmedia.exception.GlobalExceptionHandler.BadRequestException;
import com.socialmedia.exception.GlobalExceptionHandler.DuplicateResourceException;
import com.socialmedia.exception.GlobalExceptionHandler.ResourceNotFoundException;
import com.socialmedia.exception.GlobalExceptionHandler.UserNotFoundException;
import com.socialmedia.repository.FriendshipRepository;
import com.socialmedia.repository.UserRepository;

@Service
public class FriendshipService {

    private final FriendshipRepository friendshipRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public FriendshipService(FriendshipRepository friendshipRepository,
                             UserRepository userRepository,
                             NotificationService notificationService) {
        this.friendshipRepository = friendshipRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    public String sendFriendRequest(int friendshipID, int senderID, int receiverID) {

        if (senderID == receiverID) {
            throw new BadRequestException("Error: You cannot send a friend request to yourself!");
        }

        if (friendshipRepository.existsById(friendshipID)) {
            throw new DuplicateResourceException("Error: FriendshipID already exists, use a unique ID!");
        }

        User sender = userRepository.findById(senderID)
                .orElseThrow(() -> new UserNotFoundException("Sender not found with ID: " + senderID));

        User receiver = userRepository.findById(receiverID)
                .orElseThrow(() -> new UserNotFoundException("Receiver not found with ID: " + receiverID));

        if (friendshipRepository.findByUser1AndUser2(sender, receiver).isPresent() ||
            friendshipRepository.findByUser1AndUser2(receiver, sender).isPresent()) {
            throw new DuplicateResourceException("Error: A friendship or pending request already exists between these users!");
        }

        Friendship friendship = new Friendship();
        friendship.setFriendshipID(friendshipID);
        friendship.setUser1(sender);
        friendship.setUser2(receiver);
        friendship.setStatus(FriendshipStatus.pending);
        friendshipRepository.save(friendship);

        notificationService.createNotification(receiver,
                sender.getUsername() + " sent you a friend request!");

        return "Friend request sent successfully!";
    }

    public String acceptFriendRequest(int friendshipID) {

        Friendship friendship = friendshipRepository.findById(friendshipID)
                .orElseThrow(() -> new ResourceNotFoundException("Friend request not found with ID: " + friendshipID));

        if (friendship.getStatus() != FriendshipStatus.pending) {
            throw new BadRequestException("Error: This request is not in pending state!");
        }

        friendship.setStatus(FriendshipStatus.accepted);
        friendshipRepository.save(friendship);

        notificationService.createNotification(friendship.getUser1(),
                friendship.getUser2().getUsername() + " accepted your friend request!");

        return "Friend request accepted successfully!";
    }

    public String rejectFriendRequest(int friendshipID) {

        Friendship friendship = friendshipRepository.findById(friendshipID)
                .orElseThrow(() -> new ResourceNotFoundException("Friend request not found with ID: " + friendshipID));

        if (friendship.getStatus() != FriendshipStatus.pending) {
            throw new BadRequestException("Error: Only pending requests can be rejected!");
        }

        friendshipRepository.delete(friendship);
        return "Friend request rejected!";
    }

    public List<Friendship> getFriendsList(int userID) {
        return friendshipRepository.findByUser1_UserIDOrUser2_UserIDAndStatus(
                userID, userID, FriendshipStatus.accepted);
    }

    public List<Friendship> getIncomingRequests(int userID) {
        return friendshipRepository.findByUser2_UserIDAndStatus(userID, FriendshipStatus.pending);
    }

    public List<Friendship> getOutgoingRequests(int userID) {
        return friendshipRepository.findByUser1_UserIDAndStatus(userID, FriendshipStatus.pending);
    }

    public String checkStatus(int userID, int targetID) {

        User user = userRepository.findById(userID)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userID));

        User target = userRepository.findById(targetID)
                .orElseThrow(() -> new UserNotFoundException("Target user not found with ID: " + targetID));

        return friendshipRepository.findByUser1AndUser2(user, target)
                .map(f -> "Status: " + f.getStatus().name())
                .orElse(
                    friendshipRepository.findByUser1AndUser2(target, user)
                            .map(f -> "Status: " + f.getStatus().name())
                            .orElse("No relationship found between these users")
                );
    }

    public List<Notification> getFriendNotifications(int userID) {
        return notificationService.getNotificationsForUser(userID);
    }


    public long getFriendCount(int userID) {
        return getFriendsList(userID).size();
    }

    public boolean areFriends(int userID, int targetID) {
        User user = userRepository.findById(userID)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userID));
        User target = userRepository.findById(targetID)
                .orElseThrow(() -> new UserNotFoundException("Target user not found with ID: " + targetID));

        return friendshipRepository.existsByUser1AndUser2AndStatus(user, target, FriendshipStatus.accepted) ||
               friendshipRepository.existsByUser1AndUser2AndStatus(target, user, FriendshipStatus.accepted);
    }

    public List<User> getMutualFriends(int userID, int targetID) {
        List<Friendship> userFriends = getFriendsList(userID);
        List<Friendship> targetFriends = getFriendsList(targetID);

        // Extract User objects for userID's friends
        List<User> userFriendList = userFriends.stream()
                .map(f -> f.getUser1().getUserID() == userID ? f.getUser2() : f.getUser1())
                .toList();

        // Extract User objects for targetID's friends
        List<User> targetFriendList = targetFriends.stream()
                .map(f -> f.getUser1().getUserID() == targetID ? f.getUser2() : f.getUser1())
                .toList();

        // Find intersection
        return userFriendList.stream()
                .filter(u -> targetFriendList.stream().anyMatch(t -> t.getUserID() == u.getUserID()))
                .toList();
    }

    public List<User> getFriendRecommendations(int userID) {
        // Find existing friends
        List<Friendship> accepted = getFriendsList(userID);
        
        // Find pending requests
        List<Friendship> pendingIncoming = getIncomingRequests(userID);
        List<Friendship> pendingOutgoing = getOutgoingRequests(userID);

        List<Integer> excludedUserIDs = new java.util.ArrayList<>();
        excludedUserIDs.add(userID); // Exclude self

        // Add accepted friends to exclusion list
        accepted.forEach(f -> excludedUserIDs.add(f.getUser1().getUserID() == userID ? f.getUser2().getUserID() : f.getUser1().getUserID()));
        // Add pending relationships to exclusion list
        pendingIncoming.forEach(f -> excludedUserIDs.add(f.getUser1().getUserID()));
        pendingOutgoing.forEach(f -> excludedUserIDs.add(f.getUser2().getUserID()));

        // Get all users, filter out excluded, and limit to 10 suggestions
        return userRepository.findAll().stream()
                .filter(u -> !excludedUserIDs.contains(u.getUserID()))
                .limit(10)
                .toList();
    }
    public List<User> searchFriends(int userID, String query) {

        if (query == null || query.trim().isEmpty()) {
            throw new GlobalExceptionHandler.BadRequestException("Search query cannot be empty");
        }

        List<Friendship> friendships = getFriendsList(userID);

        if (friendships == null || friendships.isEmpty()) {
            throw new GlobalExceptionHandler.ResourceNotFoundException("No friends found for this user");
        }

        String lowerQuery = query.toLowerCase();

        List<User> result = friendships.stream()
                .map(f -> f.getUser1().getUserID() == userID ? f.getUser2() : f.getUser1())
                .filter(u -> u != null &&
                        u.getUsername() != null &&
                        u.getUsername().toLowerCase().contains(lowerQuery))
                .toList();

        if (result.isEmpty()) {
            throw new GlobalExceptionHandler.ResourceNotFoundException("No matching friends found");
        }

        return result;
    }
}

















