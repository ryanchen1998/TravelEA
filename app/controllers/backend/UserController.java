package controllers.backend;

import com.typesafe.config.Config;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.*;
import play.data.FormFactory;
import play.libs.Json;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import repository.*;
import util.CryptoManager;
import util.validation.UserValidator;
import util.validation.ErrorResponse;
import actions.*;
import actions.roles.Everyone;
import play.mvc.With;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.*;

import static java.lang.Math.max;

/**
 * Manage a database of users
 */
public class UserController extends Controller {

    private final UserRepository userRepository;
    private final FormFactory formFactory;
    private final HttpExecutionContext httpExecutionContext;
    private final Config config;

    @Inject
    public UserController(FormFactory formFactory,
                          UserRepository userRepository,
                          HttpExecutionContext httpExecutionContext,
                          Config config) {
        this.userRepository = userRepository;
        this.formFactory = formFactory;
        this.httpExecutionContext = httpExecutionContext;
        this.config = config;
    }

    /**
     * Display the paginated list of users.
     *
     * @param page   Current page number (starts from 0)
     * @param order  Sort order (either asc or desc)
     * @param filter Filter applied on user names
     * @return Returns a CompletionStage ok type for successful query
     */
    public CompletionStage<Result> pagedUsers(int page, String order, String filter) {
        // Run a db operation in another thread (using DatabaseExecutionContext)
        return userRepository.page(page, 10, order, filter).thenApplyAsync(users ->
                ok(Json.toJson(users.getList())), httpExecutionContext.current());
    }

    /**
     * Delete a user with given uid
     *
     * @param userId ID of user to delete
     * @return Ok if user successfully deleted, badrequest if no such user found
     */
    public CompletionStage<Result> deleteUser(Long userId) {
        return userRepository.deleteUser(userId).thenApplyAsync(rowsDeleted ->
                        (rowsDeleted > 0) ? ok("Successfully deleted user with uid: " + userId) : badRequest("No user with such uid found"),
                httpExecutionContext.current());
    }

    /**
     * Logs userout
     * @param request The HTTP request sent, the body of this request should be a JSON User object
     * @return Ok
     */
    @With({Everyone.class, Authenticator.class})
    public Result logout(Http.Request request) {
        User user = request.attrs().get(ActionState.USER);
        // removeToken(user);
        return ok();
    }


    /**
     * Method to handle adding a new user to the database. The username provided must be unique,
     * and the password field must be non-empty
     *
     * @param request The HTTP request sent, the body of this request should be a JSON User object
     * @return OK if user is successfully added to DB, badRequest otherwise
     */
    public CompletableFuture<Result> addNewUser(Http.Request request) {
        //Get the data from the request as a JSON object
        JsonNode data = request.body().asJson();
        //Sends the received data to the validator for checking
        ErrorResponse validatorResult = new UserValidator(data).login();

        //Checks if the validator found any errors in the data
        if (validatorResult.error()) {
            return CompletableFuture.supplyAsync(() -> badRequest(validatorResult.toJson()));
        } else {
            //Else, no errors found, continue with adding to the database
            //Create a new user from the request data, basing off the User class
            User newUser = Json.fromJson(data, User.class);
            //Generate a new salt for the new user
            newUser.salt = CryptoManager.generateNewSalt();
            //Generate the salted password
            newUser.password = CryptoManager.hashPassword(newUser.password, Base64.getDecoder().decode(newUser.salt));

            //This block ensures that the username (email) is not taken already, and returns a CompletableFuture<Result>
            return userRepository.findUserName(newUser.username)                //Check whether the username is already in the database
                    .thenComposeAsync(user -> {                                 //Pass that result (a User object) into the new function using thenCompose
                        if (user != null) {
                            return null;                          //If a user is found pass null into the next function
                        } else {
                            return userRepository.insertUser(newUser);         //If a user is not found pass the result of insertUser (a Long) ito the next function
                        }
                    })
                    .thenApplyAsync(user -> {   //Num should be a uid of a new user or null, the return of this lambda is the overall return of the whole method
                        if (user == null) {
                            //Create the error to be sent to client
                            validatorResult.map("Email already in use", "other");
                            return badRequest(validatorResult.toJson());    //If the uid is null, return a badRequest message...
                        } else {
                            System.out.println(user);
                            String authToken = createToken(user);
                            return ok(Json.toJson(authToken));                 //If the uid is not null, return an ok message with the uid contained within
                        }
                    });
        }
    }

    /**
     * Handles login attempts. A username and password must be provided as a JSON object,
     * by default this JSON object deserializes to a User object which is then compared against
     * the database to check for correct password and username etc.
     *
     * @param request HTTP request with username and password in body
     * @return OK with User JSON data on successful login, otherwise badRequest with specific error message
     */
    public CompletionStage<Result> login(Http.Request request) {
        // Get json parameters
        JsonNode json = request.body().asJson();

        // Run the Validator
        ErrorResponse errorResponse = new UserValidator(json).login();

        if (errorResponse.error()) {
            return CompletableFuture.supplyAsync(() -> badRequest(errorResponse.toJson()));
        }

        // Find user with username on database
        return userRepository.findUserName(json.get("username").asText("")).thenApplyAsync(foundUser -> {
            // If no such user was found with that username, return bad request
           if(foundUser == null) {
               errorResponse.map("Unauthorised", "other");
               return status(401,errorResponse.toJson());
           }
           // Otherwise if a user was found, check if correct password
           else {
               // Check if password given matches hashed and salted password on db
               if(CryptoManager.checkPasswordMatch(json.get("password").asText(""), foundUser.salt, foundUser.password)) {
                    String authToken = createToken(foundUser);
                    return ok(Json.toJson(authToken));
               }
               // If password was incorrect, return bad request
               else {
                    errorResponse.map("Unauthorised", "other");
                    return status(401,errorResponse.toJson());
               }
           }
        });
    }

    /**
     * Create Token and update user with it
     * @param user User object to be updated
     * @return authToken
     */
    public String createToken(User user) {
        String authToken = CryptoManager.createToken(user.id, config.getString("play.http.secret.key"));
        //user.authToken = authToken;
        //userRepository.updateUser(user);
        return authToken;
    }
    
    // /**
    //  * Remove user cookie
    //  * @param user User object to be updated
    //  * @return authToken 
    //  */
    // public void removeToken(User user) {
    //     user.authToken = "";
    //     userRepository.updateUser(user);
    // }
}