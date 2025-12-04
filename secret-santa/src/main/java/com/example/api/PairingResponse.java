package com.example.api;

public class PairingResponse {
    public Long id;
    public Long groupId;
    public String groupName;
    public Long giverId;
    public String giverName;
    public String giverEmail;
    public Long receiverId;
    public String receiverName;
    public String receiverEmail;
    public String receiverWishlist;

    public PairingResponse(Long id, Long groupId, String groupName,
                          Long giverId, String giverName, String giverEmail,
                          Long receiverId, String receiverName, String receiverEmail,
                          String receiverWishlist) {
        this.id = id;
        this.groupId = groupId;
        this.groupName = groupName;
        this.giverId = giverId;
        this.giverName = giverName;
        this.giverEmail = giverEmail;
        this.receiverId = receiverId;
        this.receiverName = receiverName;
        this.receiverEmail = receiverEmail;
        this.receiverWishlist = receiverWishlist;
    }
}