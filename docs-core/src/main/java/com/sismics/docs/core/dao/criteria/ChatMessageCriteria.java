package com.sismics.docs.core.dao.criteria;

/**
 * Chat message criteria.
 *
 * @author pruri
 */
public class ChatMessageCriteria {
    /**
     * User ID.
     */
    private String userId;
    
    /**
     * Sender ID.
     */
    private String senderId;
    
    /**
     * Receiver ID.
     */
    private String receiverId;
    
    /**
     * Unread only.
     */
    private Boolean unreadOnly;

    public String getUserId() {
        return userId;
    }

    public ChatMessageCriteria setUserId(String userId) {
        this.userId = userId;
        return this;
    }

    public String getSenderId() {
        return senderId;
    }

    public ChatMessageCriteria setSenderId(String senderId) {
        this.senderId = senderId;
        return this;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public ChatMessageCriteria setReceiverId(String receiverId) {
        this.receiverId = receiverId;
        return this;
    }

    public Boolean getUnreadOnly() {
        return unreadOnly;
    }

    public ChatMessageCriteria setUnreadOnly(Boolean unreadOnly) {
        this.unreadOnly = unreadOnly;
        return this;
    }
}