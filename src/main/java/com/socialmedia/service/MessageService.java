package com.socialmedia.service;

import com.socialmedia.entity.FriendshipStatus;
import com.socialmedia.entity.Message;
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

    @Autowired
    public MessageService(MessageRepository messageRepository, UserRepository userRepository, FriendshipRepository friendshipRepository) {
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
        this.friendshipRepository = friendshipRepository;
    }

    public String sendMessage(int senderID, int receiverID, int messageID, String text) {
        Optional<User> senderOpt = userRepository.findById(senderID);
        Optional<User> receiverOpt = userRepository.findById(receiverID);

        if (senderOpt.isPresent() && receiverOpt.isPresent()) {
            User sender = senderOpt.get();
            User receiver = receiverOpt.get();

            // Check if they are friends (accepted status)
            boolean isFriend = friendshipRepository.existsByUser1AndUser2AndStatus(sender, receiver, FriendshipStatus.accepted) ||
                               friendshipRepository.existsByUser1AndUser2AndStatus(receiver, sender, FriendshipStatus.accepted);

            if (!isFriend) {
                return "Error: You can only message your friends!";
            }

            Message message = new Message();
            message.setMessageID(messageID);
            message.setSender(senderOpt.get());
            message.setReceiver(receiverOpt.get());
            message.setMessageText(text);
            message.setTimestamp(LocalDateTime.now());
            
            messageRepository.save(message);
            return "Message sent successfully!";
        }
        return "Error: Sender or Receiver not found!";
    }

    public List<Message> getConversation(int user1ID, int user2ID) {
        Optional<User> user1Opt = userRepository.findById(user1ID);
        Optional<User> user2Opt = userRepository.findById(user2ID);

        if (user1Opt.isPresent() && user2Opt.isPresent()) {
            User u1 = user1Opt.get();
            User u2 = user2Opt.get();
            return messageRepository.findBySenderAndReceiverOrReceiverAndSenderOrderByTimestampAsc(u1, u2, u1, u2);
        }
        return List.of();
    }

    public List<Message> getInbox(int userID) {
        return messageRepository.findByReceiver_UserIDOrderByTimestampDesc(userID);
    }

    public List<Message> getSent(int userID) {
        return messageRepository.findBySender_UserIDOrderByTimestampDesc(userID);
    }

    public String deleteMessage(int messageID) {
        if (messageRepository.existsById(messageID)) {
            messageRepository.deleteById(messageID);
            return "Message deleted successfully!";
        }
        return "Error: Message not found!";
    }

    public long countMessagesBetweenUsers(int user1ID, int user2ID) {
        Optional<User> user1Opt = userRepository.findById(user1ID);
        Optional<User> user2Opt = userRepository.findById(user2ID);

        if (user1Opt.isPresent() && user2Opt.isPresent()) {
            User u1 = user1Opt.get();
            User u2 = user2Opt.get();
            return messageRepository.countBySenderAndReceiver(u1, u2) + messageRepository.countBySenderAndReceiver(u2, u1);
        }
        return 0;
    }
}
