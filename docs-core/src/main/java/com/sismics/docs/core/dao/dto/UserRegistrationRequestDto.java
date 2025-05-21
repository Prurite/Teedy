/** AI-genereated-content
 *  Tool: Github Copilot
 *  Version: Claude 3.7 Sonnet
 *  Usage: [Prompt:]
 *      Complete the file UserRegistrationRequest based on given references.
 *      [dbupdate-032-0.sql] [User.java]
 *      Now modify create the Dao and Dto accordingly.
 *      [UserDao.java]
 */

package com.sismics.docs.core.dao.dto;

/**
 * User registration request DTO.
 *
 * @author pruri
 */
public class UserRegistrationRequestDto {
    /**
     * Request ID.
     */
    private String id;
    
    /**
     * Name.
     */
    private String name;
    
    /**
     * Email.
     */
    private String email;
    
    /**
     * Status.
     */
    private String status;
    
    /**
     * Creation date timestamp.
     */
    private Long createTimestamp;
    
    /**
     * Update date timestamp.
     */
    private Long updateTimestamp;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getCreateTimestamp() {
        return createTimestamp;
    }

    public void setCreateTimestamp(Long createTimestamp) {
        this.createTimestamp = createTimestamp;
    }

    public Long getUpdateTimestamp() {
        return updateTimestamp;
    }

    public void setUpdateTimestamp(Long updateTimestamp) {
        this.updateTimestamp = updateTimestamp;
    }
}