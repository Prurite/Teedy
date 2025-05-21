/** AI-genereated-content
 *  Tool: Github Copilot
 *  Version: Claude 3.7 Sonnet
 *  Usage: [Prompt:]
 *      With reference to given context, implement the model, dao and dto for chat messages,
 *      and update the sql file to create necessary table for it.
 *      [UserDao.java] [UserDto.java] [User.java] [dbupdate-032-0.sql]
 */

package com.sismics.docs.core.model.jpa;

import com.google.common.base.MoreObjects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Date;

/**
 * Chat message entity.
 * 
 * @author pruri
 */
@Entity
@Table(name = "T_CHAT_MESSAGE")
public class ChatMessage implements Loggable {
    /**
     * Message ID.
     */
    @Id
    @Column(name = "CHM_ID_C", length = 36)
    private String id;
    
    /**
     * Sender user ID.
     */
    @Column(name = "CHM_IDSENDER_C", nullable = false, length = 36)
    private String senderId;
    
    /**
     * Receiver user ID.
     */
    @Column(name = "CHM_IDRECEIVER_C", nullable = false, length = 36)
    private String receiverId;
    
    /**
     * Message content.
     */
    @Column(name = "CHM_CONTENT_C", nullable = false, length = 4000)
    private String content;
    
    /**
     * Creation date.
     */
    @Column(name = "CHM_CREATEDATE_D", nullable = false)
    private Date createDate;
    
    /**
     * Read status.
     */
    @Column(name = "CHM_READ_B", nullable = false)
    private boolean read;
    
    /**
     * Deletion date.
     */
    @Column(name = "CHM_DELETEDATE_D")
    private Date deleteDate;

    public String getId() {
        return id;
    }

    public ChatMessage setId(String id) {
        this.id = id;
        return this;
    }

    public String getSenderId() {
        return senderId;
    }

    public ChatMessage setSenderId(String senderId) {
        this.senderId = senderId;
        return this;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public ChatMessage setReceiverId(String receiverId) {
        this.receiverId = receiverId;
        return this;
    }

    public String getContent() {
        return content;
    }

    public ChatMessage setContent(String content) {
        this.content = content;
        return this;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public ChatMessage setCreateDate(Date createDate) {
        this.createDate = createDate;
        return this;
    }

    public boolean isRead() {
        return read;
    }

    public ChatMessage setRead(boolean read) {
        this.read = read;
        return this;
    }

    @Override
    public Date getDeleteDate() {
        return deleteDate;
    }

    public ChatMessage setDeleteDate(Date deleteDate) {
        this.deleteDate = deleteDate;
        return this;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("senderId", senderId)
                .add("receiverId", receiverId)
                .add("content", content)
                .add("createDate", createDate)
                .add("read", read)
                .toString();
    }

    @Override
    public String toMessage() {
        return content;
    }
}