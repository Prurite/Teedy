/** AI-genereated-content
 *  Tool: Github Copilot
 *  Version: Claude 3.7 Sonnet
 *  Usage: [Prompt:]
 *      With reference to given context, implement the model, dao and dto for chat messages,
 *      and update the sql file to create necessary table for it.
 *      [UserDao.java] [UserDto.java] [User.java] [dbupdate-032-0.sql]
 */

package com.sismics.docs.core.dao.dto;

import com.google.common.base.MoreObjects;

/**
 * Chat message DTO.
 *
 * @author pruri
 */
public class ChatMessageDto {
    /**
     * Message ID.
     */
    private String id;
    
    /**
     * Sender user ID.
     */
    private String senderId;
    
    /**
     * Sender username.
     */
    private String senderUsername;
    
    /**
     * Receiver user ID.
     */
    private String receiverId;
    
    /**
     * Receiver username.
     */
    private String receiverUsername;
    
    /**
     * Message content.
     */
    private String content;
    
    /**
     * Creation date of this message.
     */
    private Long createTimestamp;
    
    /**
     * Read status.
     */
    private boolean read;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getSenderUsername() {
        return senderUsername;
    }

    public void setSenderUsername(String senderUsername) {
        this.senderUsername = senderUsername;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public String getReceiverUsername() {
        return receiverUsername;
    }

    public void setReceiverUsername(String receiverUsername) {
        this.receiverUsername = receiverUsername;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Long getCreateTimestamp() {
        return createTimestamp;
    }

    public void setCreateTimestamp(Long createTimestamp) {
        this.createTimestamp = createTimestamp;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("senderId", senderId)
                .add("receiverId", receiverId)
                .add("content", content)
                .add("createTimestamp", createTimestamp)
                .add("read", read)
                .toString();
    }
}