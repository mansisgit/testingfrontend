package com.socialmedia.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.socialmedia.entity.Notification;
import com.socialmedia.service.NotificationService;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    @Autowired
    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping("/user/{userID}")
    public List<Notification> getUserNotifications(@PathVariable int userID) {
        return notificationService.getUserNotifications(userID);
    }

    @PostMapping("/create")
    public String createNotification(
            @RequestParam int userID, 
            @RequestParam String content) {
        return notificationService.createNotification(userID, content);
    }

    @GetMapping("/{notificationID}")
    public Notification getNotification(@PathVariable int notificationID) {
        return notificationService.getNotification(notificationID);
    }

    @GetMapping("/count/{userID}")
    public long countNotifications(@PathVariable int userID) {
        return notificationService.countNotifications(userID);
    }

    @DeleteMapping("/{notificationID}")
    public String deleteNotification(@PathVariable int notificationID) {
        return notificationService.deleteNotification(notificationID);
    }
}
