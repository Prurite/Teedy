/** AI-genereated-content
 *  Tool: Github Copilot
 *  Version: Claude 3.7 Sonnet
 *  Usage: [Prompt:]
 *      With reference to given context, implement the model, dao and dto for chat messages,
 *      and update the sql file to create necessary table for it.
 *      [UserDao.java] [UserDto.java] [User.java] [dbupdate-032-0.sql]
 */

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