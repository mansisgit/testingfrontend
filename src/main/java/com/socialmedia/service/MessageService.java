package com.socialmedia.service;

import com.socialmedia.entity.FriendshipStatus;
import com.socialmedia.entity.Message;
import com.socialmedia.entity.Notification;
import com.socialmedia.entity.User;
import com.socialmedia.repository.FriendshipRepository;
import com.socialmedia.repository.MessageRepository;
import com.socialmedia.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class MessageService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final FriendshipRepository friendshipRepository;

    // ✅ ADD THIS
    private final NotificationService notificationService;

    @Autowired
    public MessageService(MessageRepository messageRepository,
                          UserRepository userRepository,
                          FriendshipRepository friendshipRepository,
                          NotificationService notificationService) {
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
        this.friendshipRepository = friendshipRepository;
        this.notificationService = notificationService; // ✅ inject
    }

    // 🚀 UPDATED METHOD (AUTO NOTIFICATION ADDED)
    public String sendMessage(int senderID, int receiverID, int messageID, String text) {

        Optional<User> senderOpt = userRepository.findById(senderID);
        Optional<User> receiverOpt = userRepository.findById(receiverID);

        if (senderOpt.isEmpty() || receiverOpt.isEmpty()) {
            return "Error: Sender or Receiver not found!";
        }

        User sender = senderOpt.get();
        User receiver = receiverOpt.get();

        // ✅ Check friendship (both directions)
        boolean isFriend =
                friendshipRepository.existsByUser1AndUser2AndStatus(sender, receiver, FriendshipStatus.accepted) ||
                friendshipRepository.existsByUser1AndUser2AndStatus(receiver, sender, FriendshipStatus.accepted);

        if (!isFriend) {
            return "Error: You can only message your friends!";
        }

        // ✅ Create message
        Message message = new Message();
        message.setMessageID(messageID);
        message.setSender(sender);
        message.setReceiver(receiver);
        message.setMessageText(text);
        message.setTimestamp(LocalDateTime.now());

        messageRepository.save(message);

        // 🔔 ✅ AUTO CREATE NOTIFICATION
        notificationService.createNotification(
                receiverID,
                "New message from " + sender.getUsername()
        );

        return "Message sent successfully!";
    }

    // ✅ Conversation
    public List<Message> getConversation(int user1ID, int user2ID) {
        Optional<User> user1Opt = userRepository.findById(user1ID);
        Optional<User> user2Opt = userRepository.findById(user2ID);

        if (user1Opt.isPresent() && user2Opt.isPresent()) {
            User u1 = user1Opt.get();
            User u2 = user2Opt.get();
            return messageRepository
                    .findBySenderAndReceiverOrReceiverAndSenderOrderByTimestampAsc(u1, u2, u1, u2);
        }
        return List.of();
    }

    // ✅ Inbox
    public List<Message> getInbox(int userID) {
        return messageRepository.findByReceiver_UserIDOrderByTimestampDesc(userID);
    }

    // ✅ Sent
    public List<Message> getSent(int userID) {
        return messageRepository.findBySender_UserIDOrderByTimestampDesc(userID);
    }

    // ✅ Delete
    public String deleteMessage(int messageID) {
        if (messageRepository.existsById(messageID)) {
            messageRepository.deleteById(messageID);
            return "Message deleted successfully!";
        }
        return "Error: Message not found!";
    }

    // ✅ Count
    public long countMessagesBetweenUsers(int user1ID, int user2ID) {
        Optional<User> user1Opt = userRepository.findById(user1ID);
        Optional<User> user2Opt = userRepository.findById(user2ID);

        if (user1Opt.isPresent() && user2Opt.isPresent()) {
            User u1 = user1Opt.get();
            User u2 = user2Opt.get();
            return messageRepository.countBySenderAndReceiver(u1, u2)
                    + messageRepository.countBySenderAndReceiver(u2, u1);
        }
        return 0;
    }
}