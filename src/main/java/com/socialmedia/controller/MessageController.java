package com.socialmedia.controller;

import com.socialmedia.entity.Message;
import com.socialmedia.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    private final MessageService messageService;

    @Autowired
    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @PostMapping("/send")
    public String sendMessage(
            @RequestParam int senderID, 
            @RequestParam int receiverID, 
            @RequestParam int messageID, 
            @RequestBody String text) {
        return messageService.sendMessage(senderID, receiverID, messageID, text);
    }

    @GetMapping("/conversation")
    public List<Message> getConversation(@RequestParam int user1ID, @RequestParam int user2ID) {
        return messageService.getConversation(user1ID, user2ID);
    }

    @GetMapping("/inbox/{userID}")
    public List<Message> getInbox(@PathVariable int userID) {
        return messageService.getInbox(userID);
    }

    @GetMapping("/sent/{userID}")
    public List<Message> getSent(@PathVariable int userID) {
        return messageService.getSent(userID);
    }

    @DeleteMapping("/{messageID}")
    public String deleteMessage(@PathVariable int messageID) {
        return messageService.deleteMessage(messageID);
    }

    @GetMapping("/count")
    public long getMessageCount(@RequestParam int user1ID, @RequestParam int user2ID) {
        return messageService.countMessagesBetweenUsers(user1ID, user2ID);
    }
}
