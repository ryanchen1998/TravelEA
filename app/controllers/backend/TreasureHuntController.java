package controllers.backend;

import actions.ActionState;
import actions.Authenticator;
import actions.roles.Everyone;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import javax.inject.Inject;
import models.TreasureHunt;
import models.User;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Http.MimeTypes;
import play.mvc.Result;
import play.mvc.Results;
import play.mvc.With;
import play.routing.JavaScriptReverseRouter;
import repository.TreasureHuntRepository;
import util.validation.ErrorResponse;
import util.validation.TreasureHuntValidator;

/**
 * Manages the database for Treasure Hunt calls
 */
public class TreasureHuntController extends TEABackController {

    private final TreasureHuntRepository treasureHuntRepository;

    @Inject
    public TreasureHuntController(TreasureHuntRepository treasureHuntRepository) {
        this.treasureHuntRepository = treasureHuntRepository;
    }

    /**
     * Ensures treasure hunt data is valid and calls method to insert into database
     *
     * @param request HTTP request containing authentication and request body
     * @return On success, ok with ID of new treasure hunt, on error returns unauthorized, forbidden
     * or bad request
     */
    @With({Everyone.class, Authenticator.class})
    public CompletableFuture<Result> insertTreasureHunt(Http.Request request) {
        // Get the data input by the user as a JSON object
        JsonNode data = request.body().asJson();
        User user = request.attrs().get(ActionState.USER);

        // Sends the received data to the validator for checking
        ErrorResponse validatorResult = new TreasureHuntValidator(data).validateTreasureHunt(false);

        // Checks if the validator found any errors in the data
        if (validatorResult.error()) {
            return CompletableFuture.supplyAsync(() -> badRequest(validatorResult.toJson()));
        }

        // Assemble TreasureHunt
        TreasureHunt treasureHunt = Json.fromJson(data, TreasureHunt.class);

        // Checks if user logged in is not allowed to create treasure hunt for userId
        if (!user.admin && !user.id.equals(treasureHunt.user.id)) {
            return CompletableFuture.supplyAsync(() -> forbidden(Json.toJson(
                "You do not have permission to create a treasure hunt for someone else")));
        }

        return treasureHuntRepository.addTreasureHunt(treasureHunt).thenApplyAsync(treasureHuntId ->
            ok(Json.toJson(treasureHuntId))
        );
    }

    /**
     * Updates a treasure hunt in the database
     *
     * @param request the HTTP request containing the user cookie
     * @param id ID of the treasure hunt to update
     * @return 200 on successful update, 404 if no treasure hunt found, 403 if unauthorized
     */
    @With({Everyone.class, Authenticator.class})
    public CompletableFuture<Result> updateTreasureHunt(Http.Request request, Long id) {
        JsonNode data = request.body().asJson();
        User user = request.attrs().get(ActionState.USER);

        // Sends the received data to the validator for checking
        ErrorResponse validatorResult = new TreasureHuntValidator(data).validateTreasureHunt(true);

        // Checks if the validator found any errors in the data
        if (validatorResult.error()) {
            return CompletableFuture.supplyAsync(() -> badRequest(validatorResult.toJson()));
        }

        return treasureHuntRepository.getTreasureHuntById(id).thenComposeAsync(treasureHunt -> {
            // Check if treasure hunt with given ID exists
            if (treasureHunt == null) {
                return CompletableFuture.supplyAsync(() -> notFound("This treasure hunt does not exist"));
            }

            // Check if user is authorized to update treasure hunt
            if (!(user.admin || treasureHunt.user.id.equals(user.id))) {
                return CompletableFuture.supplyAsync(() -> forbidden("You do not have permission to update this treasure hunt"));
            }

            // Assemble TreasureHunt
            TreasureHunt updatedTreasureHunt = Json.fromJson(data, TreasureHunt.class);
            updatedTreasureHunt.id = id;

            // Update the treasure hunt and return an ok message
            return treasureHuntRepository.updateTreasureHunt(updatedTreasureHunt)
                .thenApplyAsync(rows -> {
                    try {
                        return ok(sanitizeJson(Json.toJson(treasureHunt)));
                    } catch (IOException e) {
                        return internalServerError(Json.toJson(SANITIZATION_ERROR));
                    }
                });
        });
    }

    /**
     * Deletes a treasure hunt from the database
     *
     * @param request Http request containing user cookie
     * @param id ID of the treasure hunt to delete
     * @return 200 on successful delete, 404 if no treasure hunt found, 403 if unauthorized
     */
    @With({Everyone.class, Authenticator.class})
    public CompletableFuture<Result> deleteTreasureHunt(Http.Request request, Long id) {
        User user = request.attrs().get(ActionState.USER);

        return treasureHuntRepository.getDeletedTreasureHunt(id).thenComposeAsync(treasureHunt -> {
            // Check 404 condition where given ID does not exist
            if (treasureHunt == null) {
                return CompletableFuture.supplyAsync(() -> notFound(Json.toJson(
                    "No treasure hunt with given ID found")));
            }

            // Check user owns treasure hunt (or is an admin), otherwise return 403 FORBIDDEN
            if (!(user.admin || treasureHunt.user.id.equals(user.id))) {
                return CompletableFuture.supplyAsync(() -> forbidden(Json.toJson(
                    "You do not have permission to delete that treasure hunt")));
            }

            // Update treasure hunt deleted status and update database
            treasureHunt.deleted = !treasureHunt.deleted;

            return treasureHuntRepository.updateTreasureHunt(treasureHunt).thenApplyAsync(rows ->
                ok(Json.toJson(treasureHunt.id))
            );
        });
    }

    /**
     * Gets a treasure hunt with a given id. Returns a json with treasure hunt object.
     *
     * @param request Http request containing user cookie
     * @param id ID of wanted treasure hunt
     * @return OK with a treasure hunt, notFound if treasure hunt does not exist
     */
    @With({Everyone.class, Authenticator.class})
    public CompletableFuture<Result> getTreasureHuntById(Http.Request request, Long id) {
        User user = request.attrs().get(ActionState.USER);
        return treasureHuntRepository.getTreasureHuntById(id).thenApplyAsync(treasureHunt -> {
            if (treasureHunt == null) {
                return notFound("No treasure hunt found with matching ID");
            } else if (!(user.admin || user.id.equals(treasureHunt.user.id))) {
                return forbidden("You do not have permission to retrieve this treasure hunt");
            } else {
                try {
                    return ok(sanitizeJson(Json.toJson(treasureHunt)));
                } catch (IOException e) {
                    return internalServerError(Json.toJson(SANITIZATION_ERROR));
                }
            }
        });
    }

    /**
     * Gets all treasure hunts
     *
     * @param request the HTTP request
     * @return JSON object with list of treasure hunts
     */
    @With({Everyone.class, Authenticator.class})
    public CompletableFuture<Result> getAllTreasureHunts(Http.Request request) {
        return treasureHuntRepository.getAllTreasureHunts().thenApplyAsync(hunts -> {
            try {
                return ok(sanitizeJson(Json.toJson(hunts)));
            } catch (IOException e) {
                return internalServerError(Json.toJson(SANITIZATION_ERROR));
            }
        });
    }

    /**
     * Lists routes to put in JS router for use from frontend.
     *
     * @return JSRouter Play result
     */
    public Result treasureHuntRoutes(Http.Request request) {
        return ok(
            JavaScriptReverseRouter.create("treasureHuntRouter", "jQuery.ajax", request.host(),
                controllers.backend.routes.javascript.TreasureHuntController.insertTreasureHunt(),
                controllers.backend.routes.javascript.TreasureHuntController.getTreasureHuntById(),
                controllers.backend.routes.javascript.TreasureHuntController.getAllTreasureHunts(),
                controllers.backend.routes.javascript.TreasureHuntController.updateTreasureHunt(),
                controllers.backend.routes.javascript.TreasureHuntController.deleteTreasureHunt()

            )
        ).as(MimeTypes.JAVASCRIPT);
    }
}
