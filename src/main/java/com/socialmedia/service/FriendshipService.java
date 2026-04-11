package com.socialmedia.service;

import com.socialmedia.entity.Friendship;
import com.socialmedia.entity.FriendshipStatus;
import com.socialmedia.entity.User;
import com.socialmedia.repository.FriendshipRepository;
import com.socialmedia.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class FriendshipService {

    private final FriendshipRepository friendshipRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Autowired
    public FriendshipService(FriendshipRepository friendshipRepository, UserRepository userRepository, NotificationService notificationService) {
        this.friendshipRepository = friendshipRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    public String sendFriendRequest(int userID1, int userID2, int friendshipID) {
        if (userID1 == userID2) {
            return "Error: You cannot add yourself as a friend!";
        }

        Optional<User> user1Opt = userRepository.findById(userID1);
        Optional<User> user2Opt = userRepository.findById(userID2);

        if (user1Opt.isPresent() && user2Opt.isPresent()) {
            User user1 = user1Opt.get();
            User user2 = user2Opt.get();

            // Check if friendship already exists
            if (friendshipRepository.findByUser1AndUser2(user1, user2).isPresent() ||
                friendshipRepository.findByUser1AndUser2(user2, user1).isPresent()) {
                return "Error: Friendship or request already exists!";
            }

            Friendship friendship = new Friendship(friendshipID, user1, user2, FriendshipStatus.pending);
            friendshipRepository.save(friendship);

            // Auto-Notification
            notificationService.createNotification(user2, 
                user1.getUsername() + " sent you a friend request!");

            return "Friend request sent!";
        }
        return "Error: One or both users not found!";
    }

    public String acceptFriendRequest(int friendshipID) {
        Optional<Friendship> friendshipOpt = friendshipRepository.findById(friendshipID);
        if (friendshipOpt.isPresent()) {
            Friendship friendship = friendshipOpt.get();
            friendship.setStatus(FriendshipStatus.accepted);
            friendshipRepository.save(friendship);

            // Auto-Notification
            notificationService.createNotification(friendship.getUser1(), 
                friendship.getUser2().getUsername() + " accepted your friend request!");

            return "Friend request accepted!";
        }
        return "Error: Friendship request not found!";
    }

    public List<Friendship> getFriendsList(int userID) {
        return friendshipRepository.findByUser1_UserIDOrUser2_UserID(userID, userID);
    }
}

