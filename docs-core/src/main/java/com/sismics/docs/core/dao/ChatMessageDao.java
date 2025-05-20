package com.sismics.docs.core.dao;

import com.google.common.base.Joiner;
import com.sismics.docs.core.dao.criteria.ChatMessageCriteria;
import com.sismics.docs.core.dao.dto.ChatMessageDto;
import com.sismics.docs.core.model.jpa.ChatMessage;
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
 * Chat message DAO.
 * 
 * @author pruri
 */
public class ChatMessageDao {
    /**
     * Creates a new chat message.
     * 
     * @param chatMessage Chat message to create
     * @param userId User ID
     * @return Chat message ID
     */
    public String create(ChatMessage chatMessage, String userId) {
        // Create the chat message UUID
        chatMessage.setId(UUID.randomUUID().toString());
        
        // Create the chat message
        chatMessage.setCreateDate(new Date());
        chatMessage.setRead(false);
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        em.persist(chatMessage);
        
        return chatMessage.getId();
    }
    
    /**
     * Gets a chat message by its ID.
     * 
     * @param id Chat message ID
     * @return Chat message
     */
    public ChatMessage getById(String id) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        try {
            return em.find(ChatMessage.class, id);
        } catch (NoResultException e) {
            return null;
        }
    }
    
    /**
     * Marks a chat message as read.
     * 
     * @param id Chat message ID
     * @param userId User ID
     */
    public void markAsRead(String id, String userId) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        
        // Get the chat message
        Query q = em.createQuery("select cm from ChatMessage cm where cm.id = :id and cm.deleteDate is null");
        q.setParameter("id", id);
        ChatMessage chatMessageDb = (ChatMessage) q.getSingleResult();
        
        // Update the chat message
        chatMessageDb.setRead(true);
    }
    
    /**
     * Deletes a chat message.
     * 
     * @param id Chat message ID
     * @param userId User ID
     */
    public void delete(String id, String userId) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
            
        // Get the chat message
        Query q = em.createQuery("select cm from ChatMessage cm where cm.id = :id and cm.deleteDate is null");
        q.setParameter("id", id);
        ChatMessage chatMessageDb = (ChatMessage) q.getSingleResult();
        
        // Delete the chat message
        Date dateNow = new Date();
        chatMessageDb.setDeleteDate(dateNow);
    }
    
    /**
     * Returns the list of chat messages.
     * 
     * @param criteria Search criteria
     * @param sortCriteria Sort criteria
     * @return List of chat messages
     */
    public List<ChatMessageDto> findByCriteria(ChatMessageCriteria criteria, SortCriteria sortCriteria) {
        Map<String, Object> parameterMap = new HashMap<>();
        List<String> criteriaList = new ArrayList<>();
        
        StringBuilder sb = new StringBuilder("select cm.CHM_ID_C as c0, cm.CHM_IDSENDER_C as c1, us.USE_USERNAME_C as c2, cm.CHM_IDRECEIVER_C as c3, ur.USE_USERNAME_C as c4, cm.CHM_CONTENT_C as c5, cm.CHM_CREATEDATE_D as c6, cm.CHM_READ_B as c7");
        sb.append(" from T_CHAT_MESSAGE cm ");
        sb.append(" join T_USER us on us.USE_ID_C = cm.CHM_IDSENDER_C ");
        sb.append(" join T_USER ur on ur.USE_ID_C = cm.CHM_IDRECEIVER_C ");
        
        // Add search criterias
        if (criteria.getUserId() != null) {
            criteriaList.add("(cm.CHM_IDSENDER_C = :userId or cm.CHM_IDRECEIVER_C = :userId)");
            parameterMap.put("userId", criteria.getUserId());
        }
        
        if (criteria.getSenderId() != null) {
            criteriaList.add("cm.CHM_IDSENDER_C = :senderId");
            parameterMap.put("senderId", criteria.getSenderId());
        }
        
        if (criteria.getReceiverId() != null) {
            criteriaList.add("cm.CHM_IDRECEIVER_C = :receiverId");
            parameterMap.put("receiverId", criteria.getReceiverId());
        }
        
        if (criteria.getUnreadOnly() != null && criteria.getUnreadOnly()) {
            criteriaList.add("cm.CHM_READ_B = false");
        }
        
        criteriaList.add("cm.CHM_DELETEDATE_D is null");
        
        if (!criteriaList.isEmpty()) {
            sb.append(" where ");
            sb.append(Joiner.on(" and ").join(criteriaList));
        }
        
        // Perform the search
        QueryParam queryParam = QueryUtil.getSortedQueryParam(new QueryParam(sb.toString(), parameterMap), sortCriteria);
        @SuppressWarnings("unchecked")
        List<Object[]> l = QueryUtil.getNativeQuery(queryParam).getResultList();
        
        // Assemble results
        List<ChatMessageDto> chatMessageDtoList = new ArrayList<>();
        for (Object[] o : l) {
            int i = 0;
            ChatMessageDto chatMessageDto = new ChatMessageDto();
            chatMessageDto.setId((String) o[i++]);
            chatMessageDto.setSenderId((String) o[i++]);
            chatMessageDto.setSenderUsername((String) o[i++]);
            chatMessageDto.setReceiverId((String) o[i++]);
            chatMessageDto.setReceiverUsername((String) o[i++]);
            chatMessageDto.setContent((String) o[i++]);
            chatMessageDto.setCreateTimestamp(((Timestamp) o[i++]).getTime());
            chatMessageDto.setRead((Boolean) o[i]);
            chatMessageDtoList.add(chatMessageDto);
        }
        return chatMessageDtoList;
    }
    
    /**
     * Returns the number of unread messages for a user.
     * 
     * @param userId User ID
     * @return Number of unread messages
     */
    public int getUnreadMessageCount(String userId) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        Query query = em.createNativeQuery("select count(cm.CHM_ID_C) from T_CHAT_MESSAGE cm where cm.CHM_IDRECEIVER_C = :userId and cm.CHM_READ_B = false and cm.CHM_DELETEDATE_D is null");
        query.setParameter("userId", userId);
        return ((Number) query.getSingleResult()).intValue();
    }
}