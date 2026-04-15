package com.socialmedia.service;

import com.socialmedia.entity.SocialGroup;
import com.socialmedia.entity.User;
import com.socialmedia.repository.GroupRepository;
import com.socialmedia.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

//Updated Group Service
@Service
public class GroupService {

    private final GroupRepository groupRepository;
    private final UserRepository userRepository;

    @Autowired
    public GroupService(GroupRepository groupRepository, UserRepository userRepository) {
        this.groupRepository = groupRepository;
        this.userRepository = userRepository;
    }

    public String createGroup(int adminID, SocialGroup group) {
        Optional<User> adminOpt = userRepository.findById(adminID);
        if (adminOpt.isPresent()) {
            group.setAdmin(adminOpt.get());
            groupRepository.save(group);
            return "Group '" + group.getGroupName() + "' created successfully!";
        }
        return "Error: Admin user not found!";
    }

    public List<SocialGroup> getGroupsByAdmin(int adminID) {
        return groupRepository.findByAdmin_UserID(adminID);
    }

    public List<SocialGroup> searchGroups(String name) {
        return groupRepository.findByGroupNameContainingIgnoreCase(name);
    }

    public SocialGroup getGroupById(int groupID) {
        return groupRepository.findById(groupID).orElse(null);
    }

    public List<SocialGroup> getAllGroups() {
        return groupRepository.findAll();
    }

    public long countGroupsByAdmin(int adminID) {
        return groupRepository.countByAdmin_UserID(adminID);
    }

    public SocialGroup updateGroupName(int groupID, String newName) {
        Optional<SocialGroup> groupOpt = groupRepository.findById(groupID);
        if (groupOpt.isPresent()) {
            SocialGroup group = groupOpt.get();
            group.setGroupName(newName);
            return groupRepository.save(group);
        }
        return null; 
    }

    public List<SocialGroup> getGroupsByUser(int userID) {
        return groupRepository.findByAdmin_UserID(userID);
    }
}