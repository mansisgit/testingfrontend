package com.socialmedia.repository;

import com.socialmedia.entity.Message;
import com.socialmedia.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Integer> {
    
    // Find conversation between two users
    List<Message> findBySenderAndReceiverOrReceiverAndSenderOrderByTimestampAsc(
        User sender1, User receiver1, User sender2, User receiver2
    );

    List<Message> findByReceiver_UserIDOrderByTimestampDesc(int userID);

    List<Message> findBySender_UserIDOrderByTimestampDesc(int userID);

    long countBySenderAndReceiver(User sender, User receiver);
}
