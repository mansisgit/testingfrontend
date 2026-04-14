package com.socialmedia.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.socialmedia.entity.Notification;
import com.socialmedia.entity.User;
import com.socialmedia.repository.NotificationRepository;
import com.socialmedia.repository.UserRepository;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final Random random = new Random();

    @Autowired
    public NotificationService(NotificationRepository notificationRepository, UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    public List<Notification> getUserNotifications(int userID) {
        return notificationRepository.findByUser_UserIDOrderByTimestampDesc(userID);
    }

    // Backward compatibility for FriendshipService
    public List<Notification> getNotificationsForUser(int userID) {
        return getUserNotifications(userID);
    }

    public String createNotification(int userID, String content) {
        Optional<User> userOpt = userRepository.findById(userID);
        if (userOpt.isPresent()) {
            createNotification(userOpt.get(), content);
            return "Notification created successfully!";
        }
        return "Error: User not found!";
    }

    // Backward compatibility for existing services calling createNotification(User, String)
    public void createNotification(User user, String content) {
        Notification notification = new Notification();
        int notificationID = 10000065 + random.nextInt(9000000);
        
        notification.setNotificationID(notificationID);
        notification.setUser(user);
        notification.setContent(content);
        notification.setTimestamp(LocalDateTime.now());
        
        notificationRepository.save(notification);
    }

    public Notification getNotification(int notificationID) {
        return notificationRepository.findById(notificationID).orElse(null);
    }

    public long countNotifications(int userID) {
        return notificationRepository.countByUser_UserID(userID);
    }

    public String deleteNotification(int notificationID) {
        if (notificationRepository.existsById(notificationID)) {
            notificationRepository.deleteById(notificationID);
            return "Notification deleted successfully!";
        }
        return "Error: Notification not found!";
    }
}