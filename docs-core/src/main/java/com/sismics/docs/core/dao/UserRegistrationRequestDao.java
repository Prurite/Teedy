/** AI-genereated-content
 *  Tool: Github Copilot
 *  Version: Claude 3.7 Sonnet
 *  Usage: [Prompt:]
 *      Complete the file UserRegistrationRequest based on given references.
 *      [dbupdate-032-0.sql] [User.java]
 *      Now modify create the Dao and Dto accordingly.
 *      [UserDao.java]
 */

package com.sismics.docs.core.dao;

import com.google.common.base.Joiner;
import com.sismics.docs.core.constant.AuditLogType;
import com.sismics.docs.core.dao.dto.UserRegistrationRequestDto;
import com.sismics.docs.core.model.jpa.UserRegistrationRequest;
import com.sismics.docs.core.util.AuditLogUtil;
import com.sismics.docs.core.util.jpa.QueryParam;
import com.sismics.docs.core.util.jpa.QueryUtil;
import com.sismics.docs.core.util.jpa.SortCriteria;
import com.sismics.util.context.ThreadLocalContext;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Query;
import java.sql.Timestamp;
import java.util.*;

/**
 * User Registration Request DAO.
 * 
 * @author pruri
 */
public class UserRegistrationRequestDao {
    
    /**
     * Creates a new user registration request.
     * 
     * @param userRegistrationRequest User registration request to create
     * @return User registration request ID
     */
    public String create(UserRegistrationRequest userRegistrationRequest) {
        // Create the request UUID
        userRegistrationRequest.setId(UUID.randomUUID().toString());
        
        // Set status to PENDING if not set
        if (userRegistrationRequest.getStatus() == null) {
            userRegistrationRequest.setStatus("PENDING");
        }
        
        // Create the request
        userRegistrationRequest.setCreateDate(new Date());
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        em.persist(userRegistrationRequest);
        
        return userRegistrationRequest.getId();
    }
    
    /**
     * Updates a user registration request.
     * 
     * @param userRegistrationRequest User registration request to update
     * @param userId User ID who is updating the request
     * @return Updated user registration request
     */
    public UserRegistrationRequest update(UserRegistrationRequest userRegistrationRequest, String userId) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        
        // Get the request
        Query q = em.createQuery("select r from UserRegistrationRequest r where r.id = :id");
        q.setParameter("id", userRegistrationRequest.getId());
        UserRegistrationRequest requestDb = (UserRegistrationRequest) q.getSingleResult();

        // Update the request
        requestDb.setStatus(userRegistrationRequest.getStatus());
        requestDb.setUpdateDate(new Date());
        
        // Create audit log
        AuditLogUtil.create(requestDb, AuditLogType.UPDATE, userId);
        
        return requestDb;
    }
    
    /**
     * Gets a user registration request by its ID.
     * 
     * @param id User registration request ID
     * @return User registration request
     */
    public UserRegistrationRequest getById(String id) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        try {
            return em.find(UserRegistrationRequest.class, id);
        } catch (NoResultException e) {
            return null;
        }
    }
    
    /**
     * Gets user registration requests by email.
     * 
     * @param email Email to search for
     * @return User registration request
     */
    public List<UserRegistrationRequest> getByEmail(String email) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        Query q = em.createQuery("select r from UserRegistrationRequest r where r.email = :email");
        q.setParameter("email", email);
        try {
            @SuppressWarnings("unchecked")
            List<UserRegistrationRequest> requests = q.getResultList();
            return requests;
        } catch (NoResultException e) {
            return Collections.emptyList();
        }
    }
    
    /**
     * Gets pending user registration requests.
     * 
     * @return List of pending user registration requests
     */
    public List<UserRegistrationRequest> getPendingRequests() {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        Query q = em.createQuery("select r from UserRegistrationRequest r where r.status = 'PENDING'");
        try {
            @SuppressWarnings("unchecked")
            List<UserRegistrationRequest> requests = q.getResultList();
            return requests;
        } catch (NoResultException e) {
            return Collections.emptyList();
        }
    }
    
    /**
     * Deletes a user registration request.
     * 
     * @param id User registration request ID
     * @param userId User ID who is deleting
     */
    public void delete(String id, String userId) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
            
        // Get the request
        UserRegistrationRequest requestDb = getById(id);
        if (requestDb == null) {
            return;
        }
        
        // Delete the request
        em.remove(requestDb);
        
        // Create audit log
        AuditLogUtil.create(requestDb, AuditLogType.DELETE, userId);
    }
    
    /**
     * Returns the list of user registration requests.
     * 
     * @param sortCriteria Sort criteria
     * @return List of user registration requests
     */
    public List<UserRegistrationRequestDto> findAll(SortCriteria sortCriteria) {
        Map<String, Object> parameterMap = new HashMap<>();
        
        StringBuilder sb = new StringBuilder("select r.URR_ID_C as c0, r.URR_NAME_C as c1, r.URR_EMAIL_C as c2, r.URR_STATUS_C as c3, r.URR_CREATEDATE_D as c4, r.URR_UPDATEDATE_D as c5");
        sb.append(" from T_USER_REGISTRATION_REQUEST r");
        
        // Perform the search
        QueryParam queryParam = QueryUtil.getSortedQueryParam(new QueryParam(sb.toString(), parameterMap), sortCriteria);
        @SuppressWarnings("unchecked")
        List<Object[]> l = QueryUtil.getNativeQuery(queryParam).getResultList();
        
        // Assemble results
        List<UserRegistrationRequestDto> requestDtoList = new ArrayList<>();
        for (Object[] o : l) {
            int i = 0;
            UserRegistrationRequestDto requestDto = new UserRegistrationRequestDto();
            requestDto.setId((String) o[i++]);
            requestDto.setName((String) o[i++]);
            requestDto.setEmail((String) o[i++]);
            requestDto.setStatus((String) o[i++]);
            requestDto.setCreateTimestamp(((Timestamp) o[i++]).getTime());
            if (o[i] != null) {
                requestDto.setUpdateTimestamp(((Timestamp) o[i]).getTime());
            }
            requestDtoList.add(requestDto);
        }
        return requestDtoList;
    }
    
    /**
     * Returns the count of pending registration requests.
     *
     * @return Number of pending requests
     */
    public long getPendingRequestCount() {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        Query query = em.createNativeQuery("select count(r.URR_ID_C) from T_USER_REGISTRATION_REQUEST r where r.URR_STATUS_C = 'PENDING'");
        return ((Number) query.getSingleResult()).longValue();
    }
}