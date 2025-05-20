package com.sismics.docs.core.event;

import com.sismics.docs.core.model.jpa.UserRegistrationRequest;

/**
 * Event raised when a new user registration request is created.
 *
 * @author pruri
 */
public class RegistrationRequestCreatedEvent {
    /**
     * Registration request.
     */
    private UserRegistrationRequest request;

    /**
     * Getter of request.
     *
     * @return the request
     */
    public UserRegistrationRequest getRequest() {
        return request;
    }

    /**
     * Setter of request.
     *
     * @param request the request to set
     */
    public RegistrationRequestCreatedEvent setRequest(UserRegistrationRequest request) {
        this.request = request;
        return this;
    }
}