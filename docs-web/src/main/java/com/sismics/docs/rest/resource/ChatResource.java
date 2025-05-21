/** AI-genereated-content
 *  Tool: Github Copilot
 *  Version: Claude 3.7 Sonnet
 *  Usage: [Prompt:]
 *      With reference to the given context, implement necessary API for sending messages,
 *      listing conversations for a user, retrieving messages between 2 users, mark message as read,
 *      and other necessary things if needed, for a simple one-to-one chat system between users in the same group.
 *      [ChatMessage.java] [ChatMessageDao.java] [ChatMessageDto.java] [ChatMessageCriteria.java]
 *      [UserRegistrationRequestResource.java]
 */

package com.sismics.docs.rest.resource;

import com.sismics.docs.core.constant.PermType;
import com.sismics.docs.core.dao.ChatMessageDao;
import com.sismics.docs.core.dao.UserDao;
import com.sismics.docs.core.dao.criteria.ChatMessageCriteria;
import com.sismics.docs.core.dao.criteria.UserCriteria;
import com.sismics.docs.core.dao.dto.ChatMessageDto;
import com.sismics.docs.core.dao.dto.UserDto;
import com.sismics.docs.core.model.jpa.ChatMessage;
import com.sismics.docs.core.model.jpa.User;
import com.sismics.docs.core.util.jpa.SortCriteria;
import com.sismics.rest.exception.ClientException;
import com.sismics.rest.exception.ForbiddenClientException;
import com.sismics.rest.util.ValidationUtil;

import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObjectBuilder;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Chat REST resources.
 * 
 * @author pruri
 */
@Path("/chat")
public class ChatResource extends BaseResource {
    
    /**
     * Sends a new message.
     *
     * @api {put} /chat Send a new message
     * @apiName PutChat
     * @apiGroup Chat
     * @apiParam {String} receiverId Receiver user ID
     * @apiParam {String{1..4000}} content Message content
     * @apiSuccess {String} status Status OK
     * @apiSuccess {String} id ID of the created message
     * @apiError (client) ForbiddenError Access denied
     * @apiError (client) ValidationError Validation error
     * @apiError (client) NotFound Receiver not found
     * @apiPermission user
     * @apiVersion 1.7.0
     *
     * @param receiverId Receiver user ID
     * @param content Message content
     * @return Response
     */
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    public Response sendMessage(
            @FormParam("receiverId") String receiverId,
            @FormParam("content") String content) {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        
        // Validate input
        receiverId = ValidationUtil.validateLength(receiverId, "receiverId", 1, 50);
        content = ValidationUtil.validateLength(content, "content", 1, 4000);
        
        // Check if receiver exists and is not the same as sender
        UserDao userDao = new UserDao();
        User receiver = userDao.getById(receiverId);
        
        if (receiver == null) {
            throw new ClientException("NotFound", "Receiver not found");
        }
        
        if (receiverId.equals(principal.getId())) {
            throw new ClientException("InvalidReceiver", "Cannot send message to yourself");
        }
        
        // Create the message
        ChatMessageDao chatMessageDao = new ChatMessageDao();
        ChatMessage chatMessage = new ChatMessage()
                .setSenderId(principal.getId())
                .setReceiverId(receiverId)
                .setContent(content);
        
        String id = chatMessageDao.create(chatMessage, principal.getId());
        
        // Return the message ID
        JsonObjectBuilder response = Json.createObjectBuilder()
                .add("status", "ok")
                .add("id", id);
        
        return Response.ok().entity(response.build()).build();
    }
    
    /**
     * Returns all conversations for the current user.
     *
     * @api {get} /chat/conversation Get conversations
     * @apiName GetChatConversation
     * @apiGroup Chat
     * @apiSuccess {Object[]} conversations List of conversations
     * @apiSuccess {String} conversations.userId Other user's ID
     * @apiSuccess {String} conversations.username Other user's username
     * @apiSuccess {Number} conversations.unreadCount Number of unread messages
     * @apiSuccess {Object} conversations.lastMessage Last message info
     * @apiSuccess {String} conversations.lastMessage.content Message content
     * @apiSuccess {Number} conversations.lastMessage.createDate Create date (timestamp)
     * @apiSuccess {Boolean} conversations.lastMessage.isFromMe True if the current user is the sender
     * @apiError (client) ForbiddenError Access denied
     * @apiPermission user
     * @apiVersion 1.7.0
     *
     * @return Response
     */
    @GET
    @Path("conversation")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getConversations() {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        
        String userId = principal.getId();
        
        // Get all messages involving the current user
        ChatMessageDao chatMessageDao = new ChatMessageDao();
        ChatMessageCriteria criteria = new ChatMessageCriteria()
                .setUserId(userId);
        List<ChatMessageDto> messages = chatMessageDao.findByCriteria(criteria, new SortCriteria(6, false));
        
        // Group messages by conversation (each user the current user has chatted with)
        Map<String, List<ChatMessageDto>> conversationsMap = new HashMap<>();
        Map<String, String> usernamesMap = new HashMap<>();
        
        for (ChatMessageDto message : messages) {
            String otherUserId;
            if (userId.equals(message.getSenderId())) {
                otherUserId = message.getReceiverId();
                usernamesMap.put(otherUserId, message.getReceiverUsername());
            } else {
                otherUserId = message.getSenderId();
                usernamesMap.put(otherUserId, message.getSenderUsername());
            }
            
            conversationsMap.computeIfAbsent(otherUserId, k -> new ArrayList<>()).add(message);
        }
        
        // Build response
        JsonArrayBuilder conversations = Json.createArrayBuilder();
        for (Map.Entry<String, List<ChatMessageDto>> entry : conversationsMap.entrySet()) {
            String otherUserId = entry.getKey();
            List<ChatMessageDto> conversationMessages = entry.getValue();
            
            // Sort messages by create date descending to get the latest message
            conversationMessages.sort(Comparator.comparing(ChatMessageDto::getCreateTimestamp).reversed());
            ChatMessageDto lastMessage = conversationMessages.get(0);
            
            // Count unread messages
            long unreadCount = conversationMessages.stream()
                    .filter(m -> !m.isRead() && m.getReceiverId().equals(userId))
                    .count();
            
            JsonObjectBuilder conversation = Json.createObjectBuilder()
                    .add("userId", otherUserId)
                    .add("username", usernamesMap.get(otherUserId))
                    .add("unreadCount", unreadCount)
                    .add("lastMessage", Json.createObjectBuilder()
                            .add("content", lastMessage.getContent())
                            .add("createDate", lastMessage.getCreateTimestamp())
                            .add("isFromMe", userId.equals(lastMessage.getSenderId())));
            
            conversations.add(conversation);
        }
        
        JsonObjectBuilder response = Json.createObjectBuilder()
                .add("conversations", conversations);
        
        return Response.ok().entity(response.build()).build();
    }
    
    /**
     * Returns all messages between the current user and another user.
     *
     * @api {get} /chat/:userId Get messages
     * @apiName GetChatMessages
     * @apiGroup Chat
     * @apiParam {String} userId Other user's ID
     * @apiParam {Number} limit Maximum number of messages to return
     * @apiParam {Number} offset Offset
     * @apiSuccess {Object[]} messages List of messages
     * @apiSuccess {String} messages.id ID
     * @apiSuccess {String} messages.content Content
     * @apiSuccess {Boolean} messages.isFromMe True if the current user is the sender
     * @apiSuccess {Boolean} messages.read Read status
     * @apiSuccess {Number} messages.createDate Create date (timestamp)
     * @apiError (client) ForbiddenError Access denied
     * @apiError (client) NotFound User not found
     * @apiPermission user
     * @apiVersion 1.7.0
     *
     * @param otherUserId Other user's ID
     * @param limitStr Maximum number of messages to return
     * @param offsetStr Offset
     * @return Response
     */
    @GET
    @Path("{userId: [a-z0-9\\-]+}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getMessages(
            @PathParam("userId") String otherUserId,
            @QueryParam("limit") String limitStr,
            @QueryParam("offset") String offsetStr) {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        
        // Validate inputs
        Integer limit = ValidationUtil.validateInteger(limitStr, "limit");
        Integer offset = ValidationUtil.validateInteger(offsetStr, "offset");
        if (limit == null) {
            limit = 50; // Default limit
        }
        if (offset == null) {
            offset = 0; // Default offset
        }
        
        // Check if the other user exists
        UserDao userDao = new UserDao();
        User otherUser = userDao.getById(otherUserId);
        if (otherUser == null) {
            throw new ClientException("NotFound", "User not found");
        }
        
        // Get messages between current user and other user
        ChatMessageDao chatMessageDao = new ChatMessageDao();
        
        // Find messages where current user is sender and other user is receiver
        ChatMessageCriteria sentCriteria = new ChatMessageCriteria()
                .setSenderId(principal.getId())
                .setReceiverId(otherUserId);
        
        // Find messages where current user is receiver and other user is sender
        ChatMessageCriteria receivedCriteria = new ChatMessageCriteria()
                .setSenderId(otherUserId)
                .setReceiverId(principal.getId());
        
        List<ChatMessageDto> sentMessages = chatMessageDao.findByCriteria(sentCriteria, new SortCriteria(6, false));
        List<ChatMessageDto> receivedMessages = chatMessageDao.findByCriteria(receivedCriteria, new SortCriteria(6, false));
        
        // Merge and sort all messages by creation date
        List<ChatMessageDto> allMessages = new ArrayList<>();
        allMessages.addAll(sentMessages);
        allMessages.addAll(receivedMessages);
        allMessages.sort(Comparator.comparing(ChatMessageDto::getCreateTimestamp).reversed());
        
        // Apply pagination
        int endIndex = Math.min(offset + limit, allMessages.size());
        List<ChatMessageDto> paginatedMessages = allMessages.subList(offset, endIndex);
        
        // Mark received messages as read if they aren't already
        for (ChatMessageDto message : receivedMessages) {
            if (!message.isRead()) {
                chatMessageDao.markAsRead(message.getId(), principal.getId());
            }
        }
        
        // Build response
        JsonArrayBuilder messages = Json.createArrayBuilder();
        for (ChatMessageDto message : paginatedMessages) {
            JsonObjectBuilder messageJson = Json.createObjectBuilder()
                    .add("id", message.getId())
                    .add("content", message.getContent())
                    .add("isFromMe", principal.getId().equals(message.getSenderId()))
                    .add("read", message.isRead())
                    .add("createDate", message.getCreateTimestamp());
            
            messages.add(messageJson);
        }
        
        JsonObjectBuilder response = Json.createObjectBuilder()
                .add("messages", messages)
                .add("total", allMessages.size());
        
        return Response.ok().entity(response.build()).build();
    }
    
    /**
     * Mark a message as read.
     *
     * @api {post} /chat/:id/read Mark a message as read
     * @apiName PostChatRead
     * @apiGroup Chat
     * @apiParam {String} id Message ID
     * @apiSuccess {String} status Status OK
     * @apiError (client) ForbiddenError Access denied
     * @apiError (client) NotFound Message not found
     * @apiPermission user
     * @apiVersion 1.7.0
     *
     * @param id Message ID
     * @return Response
     */
    @POST
    @Path("{id: [a-z0-9\\-]+}/read")
    @Produces(MediaType.APPLICATION_JSON)
    public Response markAsRead(@PathParam("id") String id) {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        
        // Get the message
        ChatMessageDao chatMessageDao = new ChatMessageDao();
        ChatMessage message = chatMessageDao.getById(id);
        if (message == null) {
            throw new ClientException("NotFound", "Message not found");
        }
        
        // Check if the current user is the receiver
        if (!message.getReceiverId().equals(principal.getId())) {
            throw new ForbiddenClientException();
        }
        
        // Mark the message as read
        chatMessageDao.markAsRead(id, principal.getId());
        
        // Return OK response
        JsonObjectBuilder response = Json.createObjectBuilder()
                .add("status", "ok");
        
        return Response.ok().entity(response.build()).build();
    }
    
    /**
     * Deletes a message.
     *
     * @api {delete} /chat/:id Delete a message
     * @apiName DeleteChat
     * @apiGroup Chat
     * @apiParam {String} id Message ID
     * @apiSuccess {String} status Status OK
     * @apiError (client) ForbiddenError Access denied
     * @apiError (client) NotFound Message not found
     * @apiPermission user
     * @apiVersion 1.7.0
     *
     * @param id Message ID
     * @return Response
     */
    @DELETE
    @Path("{id: [a-z0-9\\-]+}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response delete(@PathParam("id") String id) {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        
        // Get the message
        ChatMessageDao chatMessageDao = new ChatMessageDao();
        ChatMessage message = chatMessageDao.getById(id);
        if (message == null) {
            throw new ClientException("NotFound", "Message not found");
        }
        
        // Check if the current user is the sender or receiver
        if (!message.getSenderId().equals(principal.getId()) && !message.getReceiverId().equals(principal.getId())) {
            throw new ForbiddenClientException();
        }
        
        // Delete the message
        chatMessageDao.delete(id, principal.getId());
        
        // Return OK response
        JsonObjectBuilder response = Json.createObjectBuilder()
                .add("status", "ok");
        
        return Response.ok().entity(response.build()).build();
    }
    
    /**
     * Returns the number of unread messages for the current user.
     *
     * @api {get} /chat/unread Get unread message count
     * @apiName GetChatUnread
     * @apiGroup Chat
     * @apiSuccess {Number} unread Number of unread messages
     * @apiError (client) ForbiddenError Access denied
     * @apiPermission user
     * @apiVersion 1.7.0
     *
     * @return Response
     */
    @GET
    @Path("unread")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUnreadCount() {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        
        // Get unread message count
        ChatMessageDao chatMessageDao = new ChatMessageDao();
        int unreadCount = chatMessageDao.getUnreadMessageCount(principal.getId());
        
        // Return count
        JsonObjectBuilder response = Json.createObjectBuilder()
                .add("unread", unreadCount);
        
        return Response.ok().entity(response.build()).build();
    }
    
    /**
     * Returns the list of users that the current user can chat with.
     *
     * @api {get} /chat/user List users for chat
     * @apiName GetChatUsers
     * @apiGroup Chat
     * @apiSuccess {Object[]} users List of users
     * @apiSuccess {String} users.id User ID
     * @apiSuccess {String} users.username Username
     * @apiError (client) ForbiddenError Access denied
     * @apiPermission user
     * @apiVersion 1.7.0
     *
     * @return Response
     */
    @GET
    @Path("user")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUsers() {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        
        // Get all active users except the current user
        UserDao userDao = new UserDao();
        UserCriteria userCriteria = new UserCriteria();
        List<UserDto> users = userDao.findByCriteria(userCriteria, null);
        users = users.stream()
                .filter(user -> !user.getId().equals(principal.getId()))
                .collect(Collectors.toList());
        
        // Build response
        JsonArrayBuilder usersArray = Json.createArrayBuilder();
        for (UserDto user : users) {
            JsonObjectBuilder userJson = Json.createObjectBuilder()
                    .add("id", user.getId())
                    .add("username", user.getUsername());
            
            usersArray.add(userJson);
        }
        
        JsonObjectBuilder response = Json.createObjectBuilder()
                .add("users", usersArray);
        
        return Response.ok().entity(response.build()).build();
    }
}