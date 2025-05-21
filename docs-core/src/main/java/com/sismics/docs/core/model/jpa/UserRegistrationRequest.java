/** AI-genereated-content
 *  Tool: Github Copilot
 *  Version: Claude 3.7 Sonnet
 *  Usage: [Prompt:]
 *      Complete the file UserRegistrationRequest based on given references.
 *      [dbupdate-032-0.sql] [User.java]
 */

package com.sismics.docs.core.model.jpa;

import com.google.common.base.MoreObjects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Date;

/**
 * User registration request entity.
 * 
 * @author pruri
 */
@Entity
@Table(name = "T_USER_REGISTRATION_REQUEST")
public class UserRegistrationRequest implements Loggable {
    /**
     * Request ID.
     */
    @Id
    @Column(name = "URR_ID_C", length = 36)
    private String id;
    
    /**
     * Name of the user requesting registration.
     */
    @Column(name = "URR_NAME_C", length = 100)
    private String name;
    
    /**
     * Email address of the user.
     */
    @Column(name = "URR_EMAIL_C", nullable = false, length = 255)
    private String email;
    
    /**
     * Status of the request (default: PENDING).
     */
    @Column(name = "URR_STATUS_C", nullable = false, length = 20)
    private String status;
    
    /**
     * Creation date.
     */
    @Column(name = "URR_CREATEDATE_D", nullable = false)
    private Date createDate;
    
    /**
     * Update date.
     */
    @Column(name = "URR_UPDATEDATE_D")
    private Date updateDate;
    
    /**
     * Delete date.
     */
    @Column(name = "URR_DELETEDATE_D")
    private Date deleteDate;
    
    public String getId() {
        return id;
    }
    
    public UserRegistrationRequest setId(String id) {
        this.id = id;
        return this;
    }
    
    public String getName() {
        return name;
    }
    
    public UserRegistrationRequest setName(String name) {
        this.name = name;
        return this;
    }
    
    public String getEmail() {
        return email;
    }
    
    public UserRegistrationRequest setEmail(String email) {
        this.email = email;
        return this;
    }
    
    public String getStatus() {
        return status;
    }
    
    public UserRegistrationRequest setStatus(String status) {
        this.status = status;
        return this;
    }
    
    public Date getCreateDate() {
        return createDate;
    }
    
    public UserRegistrationRequest setCreateDate(Date createDate) {
        this.createDate = createDate;
        return this;
    }
    
    public Date getUpdateDate() {
        return updateDate;
    }
    
    public UserRegistrationRequest setUpdateDate(Date updateDate) {
        this.updateDate = updateDate;
        return this;
    }
    
    @Override
    public Date getDeleteDate() {
        return deleteDate;
    }
    
    public UserRegistrationRequest setDeleteDate(Date deleteDate) {
        this.deleteDate = deleteDate;
        return this;
    }
    
    @Override
    public String toMessage() {
        return "Registration request from " + name + " (" + email + ") with status " + status;
    }
    
    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("name", name)
                .add("email", email)
                .add("status", status)
                .toString();
    }
}