package com.socialmedia.repository;

import com.socialmedia.entity.SocialGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GroupRepository extends JpaRepository<SocialGroup, Integer> {
    List<SocialGroup> findByAdmin_UserID(int adminID);
    List<SocialGroup> findByGroupNameContainingIgnoreCase(String groupName);
    long countByAdmin_UserID(int adminID);
}