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
}

