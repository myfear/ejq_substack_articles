package com.example.api;

public class GroupMembershipResponse {
    public Long id;
    public Long groupId;
    public String groupName;
    public String participantName;
    public String participantEmail;
    public String wishlist;

    public GroupMembershipResponse() {
    }

    public GroupMembershipResponse(Long id, Long groupId, String groupName, 
                                   String participantName, String participantEmail, 
                                   String wishlist) {
        this.id = id;
        this.groupId = groupId;
        this.groupName = groupName;
        this.participantName = participantName;
        this.participantEmail = participantEmail;
        this.wishlist = wishlist;
    }
}