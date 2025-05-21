/** AI-genereated-content
 *  Tool: Github Copilot
 *  Version: Claude 3.7 Sonnet
 *  Usage: [Prompt:]
 *      Create requestRegistration, getRequests, approve, reject api endpoints
 *      for user registration requests based on provided context.
 *      [dbupdate-032-0.sql] [UserRegistrationRequest.java] [UserRegistrationRequestDao.java]
 *      [UserRegistrationRequestDto.java] [UserResource.java]
 */

package com.sismics.docs.rest.resource;

import com.google.common.base.Strings;
import com.sismics.docs.core.constant.ConfigType;
import com.sismics.docs.core.dao.UserDao;
import com.sismics.docs.core.dao.UserRegistrationRequestDao;
import com.sismics.docs.core.dao.dto.UserRegistrationRequestDto;
import com.sismics.docs.core.event.RegistrationRequestCreatedEvent;
import com.sismics.docs.core.model.context.AppContext;
import com.sismics.docs.core.model.jpa.User;
import com.sismics.docs.core.model.jpa.UserRegistrationRequest;
import com.sismics.docs.core.util.ConfigUtil;
import com.sismics.docs.core.util.jpa.SortCriteria;
import com.sismics.docs.rest.constant.BaseFunction;
import com.sismics.rest.exception.ClientException;
import com.sismics.rest.exception.ForbiddenClientException;
import com.sismics.rest.exception.ServerException;
import com.sismics.rest.util.ValidationUtil;
import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObjectBuilder;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

/**
 * User registration request REST resources.
 * 
 * @author pruri
 */
@Path("/user/registration")
public class UserRegistrationRequestResource extends BaseResource {
    
    /**
     * Creates a new registration request.
     *
     * @api {put} /user/registration Request a new user registration
     * @apiName PutUserRegistration
     * @apiGroup User
     * @apiParam {String{1..100}} name User's full name
     * @apiParam {String{1..100}} email E-mail
     * @apiSuccess {String} status Status OK
     * @apiSuccess {String} id ID of the registration request
     * @apiError (client) ValidationError Validation error
     * @apiError (client) RegistrationDisabled User registration is disabled
     * @apiError (server) UnknownError Unknown server error
     * @apiPermission none
     * @apiVersion 1.7.0
     *
     * @param name User's name
     * @param email E-Mail
     * @return Response
     */
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    public Response requestRegistration(
            @FormParam("name") String name,
            @FormParam("email") String email) {
        
        // Check if registration is enabled
        if (!ConfigUtil.getConfigBooleanValue(ConfigType.USER_REGISTRATION_ENABLED)) {
            throw new ClientException("RegistrationDisabled", "User registration is disabled");
        }
        
        // Validate the input data
        name = ValidationUtil.validateLength(name, "name", 1, 100, false);
        email = ValidationUtil.validateLength(email, "email", 1, 100, false);
        ValidationUtil.validateEmail(email, "email");
        
        // Check if a request with this email already exists
        UserRegistrationRequestDao userRegistrationRequestDao = new UserRegistrationRequestDao();
        List<UserRegistrationRequest> existingRequests = userRegistrationRequestDao.getByEmail(email);
        for (UserRegistrationRequest existingRequest : existingRequests) {
            if ("PENDING".equals(existingRequest.getStatus())) {
                throw new ClientException("AlreadyRequested", "A registration request with this email is already pending");
            }
        }
        
        // Check if a user with this email already exists
        UserDao userDao = new UserDao();
        User user = userDao.getActiveByEmail(email);
        if (user != null) {
            throw new ClientException("EmailExists", "A user with this email already exists");
        }
        
        // Create the registration request
        UserRegistrationRequest userRegistrationRequest = new UserRegistrationRequest()
                .setName(name)
                .setEmail(email)
                .setStatus("PENDING");
        
        try {
            String id = userRegistrationRequestDao.create(userRegistrationRequest);
            
            // Send notification to administrators
            RegistrationRequestCreatedEvent event = new RegistrationRequestCreatedEvent();
            event.setRequest(userRegistrationRequest);
            AppContext.getInstance().getMailEventBus().post(event);
            
            // Return the registration request ID
            JsonObjectBuilder response = Json.createObjectBuilder()
                    .add("status", "ok")
                    .add("id", id);
            return Response.ok().entity(response.build()).build();
        } catch (Exception e) {
            throw new ServerException("UnknownError", "Error creating registration request", e);
        }
    }

    /**
     * Returns all registration requests.
     *
     * @api {get} /user/registration Get registration requests
     * @apiName GetUserRegistrationRequests
     * @apiGroup User
     * @apiParam {Number} sort_column Column index to sort on
     * @apiParam {Boolean} asc If true, sort in ascending order
     * @apiSuccess {Object[]} requests List of registration requests
     * @apiSuccess {String} requests.id ID
     * @apiSuccess {String} requests.name User's name
     * @apiSuccess {String} requests.email E-mail
     * @apiSuccess {String} requests.status Status (PENDING, APPROVED, REJECTED)
     * @apiSuccess {Number} requests.create_date Create date (timestamp)
     * @apiSuccess {Number} requests.update_date Update date (timestamp)
     * @apiError (client) ForbiddenError Access denied
     * @apiPermission admin
     * @apiVersion 1.7.0
     *
     * @param sortColumn Sort index
     * @param asc If true, ascending sorting, else descending
     * @return Response
     */
    @GET
    @Path("list")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getRequests(
            @QueryParam("sort_column") Integer sortColumn,
            @QueryParam("asc") Boolean asc) {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        checkBaseFunction(BaseFunction.ADMIN);
        
        JsonArrayBuilder requests = Json.createArrayBuilder();
        SortCriteria sortCriteria = new SortCriteria(sortColumn, asc);
        
        UserRegistrationRequestDao userRegistrationRequestDao = new UserRegistrationRequestDao();
        List<UserRegistrationRequestDto> requestDtoList = userRegistrationRequestDao.findAll(sortCriteria);
        
        for (UserRegistrationRequestDto requestDto : requestDtoList) {
            JsonObjectBuilder request = Json.createObjectBuilder()
                    .add("id", requestDto.getId())
                    .add("name", requestDto.getName() != null ? requestDto.getName() : "")
                    .add("email", requestDto.getEmail())
                    .add("status", requestDto.getStatus())
                    .add("create_date", requestDto.getCreateTimestamp());
            
            if (requestDto.getUpdateTimestamp() != null) {
                request.add("update_date", requestDto.getUpdateTimestamp());
            }
            
            requests.add(request);
        }
        
        JsonObjectBuilder response = Json.createObjectBuilder()
                .add("requests", requests);
        return Response.ok().entity(response.build()).build();
    }

    /**
     * Approves a registration request and creates a new user.
     *
     * @api {post} /user/registration/:id/approve Approve a registration request
     * @apiName PostUserRegistrationApprove
     * @apiGroup User
     * @apiParam {String} id Registration request ID
     * @apiParam {String{3..50}} username Username for the new user
     * @apiParam {String{8..50}} password Password for the new user
     * @apiParam {Number} storage_quota Storage quota (in bytes)
     * @apiSuccess {String} status Status OK
     * @apiError (client) ForbiddenError Access denied
     * @apiError (client) ValidationError Validation error
     * @apiError (client) NotFound Registration request not found
     * @apiError (server) UnknownError Unknown server error
     * @apiPermission admin
     * @apiVersion 1.7.0
     *
     * @param id Registration request ID
     * @param username Username for the new user
     * @param password Password for the new user
     * @param storageQuotaStr Storage quota in bytes
     * @return Response
     */
    @POST
    @Path("{id: [a-z0-9\\-]+}/approve")
    @Produces(MediaType.APPLICATION_JSON)
    public Response approve(
            @PathParam("id") String id,
            @FormParam("username") String username,
            @FormParam("password") String password,
            @FormParam("storage_quota") String storageQuotaStr) {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        checkBaseFunction(BaseFunction.ADMIN);
        
        // Validate input data
        username = ValidationUtil.validateLength(username, "username", 3, 50);
        ValidationUtil.validateUsername(username, "username");
        password = ValidationUtil.validateLength(password, "password", 8, 50);
        Long storageQuota = ValidationUtil.validateLong(storageQuotaStr, "storage_quota");
        
        // Get the registration request
        UserRegistrationRequestDao userRegistrationRequestDao = new UserRegistrationRequestDao();
        UserRegistrationRequest request = userRegistrationRequestDao.getById(id);
        if (request == null) {
            throw new ClientException("NotFound", "Registration request not found");
        }
        
        // Check if request is already processed
        if (!"PENDING".equals(request.getStatus())) {
            throw new ClientException("AlreadyProcessed", "This registration request has already been processed");
        }
        
        // Check if username is already taken
        UserDao userDao = new UserDao();
        User existingUser = userDao.getActiveByUsername(username);
        if (existingUser != null) {
            throw new ClientException("AlreadyExistingUsername", "Username already exists");
        }
        
        // Create the new user
        User user = new User();
        user.setRoleId("user");
        user.setUsername(username);
        user.setPassword(password);
        user.setEmail(request.getEmail());
        user.setStorageQuota(storageQuota);
        user.setOnboarding(true);
        
        try {
            // Create the user
            userDao.create(user, principal.getId());
            
            // Update the request status to APPROVED
            request.setStatus("APPROVED");
            userRegistrationRequestDao.update(request, principal.getId());
            
            // Return OK response
            JsonObjectBuilder response = Json.createObjectBuilder()
                    .add("status", "ok");
            return Response.ok().entity(response.build()).build();
        } catch (Exception e) {
            if ("AlreadyExistingUsername".equals(e.getMessage())) {
                throw new ClientException("AlreadyExistingUsername", "Login already used", e);
            } else {
                throw new ServerException("UnknownError", "Unknown server error", e);
            }
        }
    }

    /**
     * Rejects a registration request.
     *
     * @api {post} /user/registration/:id/reject Reject a registration request
     * @apiName PostUserRegistrationReject
     * @apiGroup User
     * @apiParam {String} id Registration request ID
     * @apiSuccess {String} status Status OK
     * @apiError (client) ForbiddenError Access denied
     * @apiError (client) NotFound Registration request not found
     * @apiError (server) UnknownError Unknown server error
     * @apiPermission admin
     * @apiVersion 1.7.0
     *
     * @param id Registration request ID
     * @return Response
     */
    @POST
    @Path("{id: [a-z0-9\\-]+}/reject")
    @Produces(MediaType.APPLICATION_JSON)
    public Response reject(@PathParam("id") String id) {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        checkBaseFunction(BaseFunction.ADMIN);
        
        // Get the registration request
        UserRegistrationRequestDao userRegistrationRequestDao = new UserRegistrationRequestDao();
        UserRegistrationRequest request = userRegistrationRequestDao.getById(id);
        if (request == null) {
            throw new ClientException("NotFound", "Registration request not found");
        }
        
        // Check if request is already processed
        if (!"PENDING".equals(request.getStatus())) {
            throw new ClientException("AlreadyProcessed", "This registration request has already been processed");
        }
        
        try {
            // Update the request status to REJECTED
            request.setStatus("REJECTED");
            userRegistrationRequestDao.update(request, principal.getId());
            
            // Return OK response
            JsonObjectBuilder response = Json.createObjectBuilder()
                    .add("status", "ok");
            return Response.ok().entity(response.build()).build();
        } catch (Exception e) {
            throw new ServerException("UnknownError", "Unknown server error", e);
        }
    }
}