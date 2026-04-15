package com.socialmedia.controller;

import com.socialmedia.entity.SocialGroup;
import com.socialmedia.service.GroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/groups")
public class GroupController {

    private final GroupService groupService;

    @Autowired
    public GroupController(GroupService groupService) {
        this.groupService = groupService;
    }

    @PostMapping("/create")
    public String createGroup(@RequestParam int adminID, @RequestBody SocialGroup group) {
        return groupService.createGroup(adminID, group);
    }

    @GetMapping("/admin/{adminID}")
    public List<SocialGroup> getGroupsByAdmin(@PathVariable int adminID) {
        return groupService.getGroupsByAdmin(adminID);
    }

    @GetMapping("/search")
    public List<SocialGroup> searchGroups(@RequestParam String name) {
        return groupService.searchGroups(name);
    }

    @GetMapping("/{groupID}")
    public SocialGroup getGroupById(@PathVariable int groupID) {
        return groupService.getGroupById(groupID);
    }

    @GetMapping
    public List<SocialGroup> getAllGroups() {
        return groupService.getAllGroups();
    }

    @GetMapping("/admin/{adminID}/count")
    public long countGroupsByAdmin(@PathVariable int adminID) {
        return groupService.countGroupsByAdmin(adminID);
    }

    @PutMapping("/{groupID}")
    public SocialGroup updateGroupName(@PathVariable int groupID, @RequestParam String newName) {
        return groupService.updateGroupName(groupID, newName);
    }

    @GetMapping("/user/{userID}")
    public List<SocialGroup> getGroupsByUser(@PathVariable int userID) {
        return groupService.getGroupsByUser(userID);
    }
} 


