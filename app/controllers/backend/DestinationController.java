package controllers.backend;

import actions.ActionState;
import actions.Authenticator;
import actions.roles.Everyone;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import javax.inject.Inject;
import models.Destination;
import models.User;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.With;
import play.routing.JavaScriptReverseRouter;
import repository.CountryDefinitionRepository;
import repository.DestinationRepository;
import repository.TravellerTypeDefinitionRepository;
import util.validation.DestinationValidator;
import util.validation.ErrorResponse;

/**
 * Manages destinations in the database.
 */
public class DestinationController extends TEABackController {

    private final DestinationRepository destinationRepository;
    private final CountryDefinitionRepository countryDefinitionRepository;
    private final TravellerTypeDefinitionRepository travellerTypeDefinitionRepository;

    @Inject
    public DestinationController(DestinationRepository destinationRepository,
        CountryDefinitionRepository countryDefinitionRepository,
        TravellerTypeDefinitionRepository travellerTypeDefinitionRepository) {
        this.destinationRepository = destinationRepository;
        this.countryDefinitionRepository = countryDefinitionRepository;
        this.travellerTypeDefinitionRepository = travellerTypeDefinitionRepository;
    }

    /**
     * Adds a new destination to the database. Destination object to be added must be a json object
     * in the request of the body
     *
     * @param request Request containing destination json object as body
     * @return Ok with id of destination on success, badRequest otherwise
     */
    @With({Everyone.class, Authenticator.class})
    public CompletableFuture<Result> addNewDestination(Http.Request request) {
        JsonNode data = request.body().asJson();
        User user = request.attrs().get(ActionState.USER);

        // Sends the received data to the validator for checking, if error returns bad request
        ErrorResponse validatorResult = new DestinationValidator(data).addNewDestination();
        if (validatorResult.error()) {
            return CompletableFuture.supplyAsync(() -> badRequest(validatorResult.toJson()));
        }

        // Add destination owner to be whichever user uploaded it
        Destination newDestination = Json.fromJson(data, Destination.class);

        // Checks if user logged in is not allowed to create dest for userId
        if (!user.admin && !user.id.equals(newDestination.user.id)) {
            return CompletableFuture.supplyAsync(() -> forbidden(Json.toJson(
                "You do not have permission to create a destination for someone else")));
        }
        //check if destination already exists
        List<Destination> destinations = destinationRepository
            .getSimilarDestinations(newDestination);
        for (Destination destination : destinations) {
            if (destination.user.id.equals(user.id) || destination.isPublic) {
                return CompletableFuture
                    .supplyAsync(() -> badRequest(Json.toJson("Duplicate destination")));
            }
        }
        return destinationRepository.addDestination(newDestination)
            .thenApplyAsync(id -> {
                try {
                    return ok(sanitizeJson(Json.toJson(id)));
                } catch (IOException e) {
                    return internalServerError(Json.toJson(SANITIZATION_ERROR));
                }
            });
    }

    /**
     * Allows a user to mark one of their destinations as public. This will cause it to become
     * immediately visible to all other users, as well as merging with any sufficiently similar
     * destinations that are currently marked as private in the database
     *
     * @param request Request containing authentication header
     * @param id ID of destination to mark as public
     * @return 200 if successful, 400 if already public, 401 unauthorized, 403 forbidden, 404 no
     * such destination
     */
    @With({Everyone.class, Authenticator.class})
    public CompletableFuture<Result> makeDestinationPublic(Http.Request request, Long id) {
        User user = request.attrs().get(ActionState.USER);
        return destinationRepository.getDestination(id).thenApplyAsync(destination -> {
            // Check for 404, i.e the destination to make public doesn't exist
            if (destination == null) {
                return notFound(Json.toJson("No such destination exists"));
            }
            // Check if user owns the destination (or is an admin)
            if (!destination.user.id.equals(user.id) && !user.admin) {
                return forbidden(Json.toJson("You are not allowed to perform this action"));
            }
            // Check if destination was already public
            if (destination.isPublic) {
                return badRequest(Json.toJson("Destination was already public"));
            }

            destinationRepository.setDestinationToPublicInDatabase(destination.id);

            // Find all similar destinations that need to be merged and collect only their IDs
            List<Destination> destinations = destinationRepository
                .getSimilarDestinations(destination);
            List<Long> similarIds = destinations.stream().map(x -> x.id)
                .collect(Collectors.toList());

            // Re-reference each instance of the old destinations to the new one, keeping track of how many rows were changed
            // TripData
            int rowsChanged = destinationRepository
                .mergeDestinationsTripData(similarIds, destination.id);
            // Photos
            rowsChanged += destinationRepository
                .mergeDestinationsPhotos(similarIds, destination.id);

            // If any rows were changed when re-referencing, the destination
            // has been used by another user and must be transferred to admin ownership
            if (rowsChanged > 0) {
                destinationRepository.changeDestinationOwner(destination.id, MASTER_ADMIN_ID);
            }

            // Once all old usages have been re-referenced, delete the found similar destinations
            for (Long simId : similarIds) {
                destinationRepository.deleteDestination(simId);
            }

            return ok("Successfully made destination public, and re-referenced " + rowsChanged
                + " to new public destination");
        });
    }

    /**
     * Deletes a destination with given id. Return a result with a json int which represents the
     * number of rows that were deleted. So if the return value is 0, no destination was found to
     * delete
     *
     * @param id ID of destination to delete
     * @return OK with number of rows deleted, bad request if none deleted
     */
    @With({Everyone.class, Authenticator.class})
    public CompletableFuture<Result> deleteDestination(Http.Request request, Long id) {
        User user = request.attrs().get(ActionState.USER);
        return destinationRepository.getDestination(id).thenComposeAsync(destination -> {
            if (destination == null) {
                return CompletableFuture.supplyAsync(() -> notFound("No such destination found"));
            } else if (destination.user.id.equals(user.id) || user.admin) {
                return destinationRepository.deleteDestination(id).thenApplyAsync(rowsDeleted -> {
                    if (rowsDeleted < 1) {
                        ErrorResponse errorResponse = new ErrorResponse();
                        errorResponse.map("Destination not found", "other");
                        return badRequest(errorResponse.toJson());
                    } else {
                        try {
                            return ok(sanitizeJson(Json.toJson(rowsDeleted)));
                        } catch (IOException e) {
                            return internalServerError(Json.toJson(SANITIZATION_ERROR));
                        }
                    }
                });
            } else {
                return CompletableFuture.supplyAsync(() -> forbidden("Forbidden"));
            }
        });
    }

    /**
     * Edits a destination's details with given id.
     *
     * @param request The request
     * @param id The id of the destination to edit
     * @return 400 is the request is bad, 404 if the destination is not found, 500 if sanitization
     * fails, 403 if the user cannot edit the destination and 200 if successful
     */
    @With({Everyone.class, Authenticator.class})
    public CompletableFuture<Result> editDestination(Http.Request request, Long id) {
        JsonNode data = request.body().asJson();
        User user = request.attrs().get(ActionState.USER);
        ErrorResponse validatorResult = new DestinationValidator(data).addNewDestination();
        return destinationRepository.getDestination(id).thenComposeAsync(destination -> {
            if (destination == null) {
                return CompletableFuture
                    .supplyAsync(() -> notFound("Destination with provided ID not found"));
            } else if (destination.user.id.equals(user.id) || user.admin) {
                if (validatorResult.error()) {
                    return CompletableFuture
                        .supplyAsync(() -> badRequest(validatorResult.toJson()));
                }
                Destination editedDestination = Json.fromJson(data, Destination.class);
                //check if destination already exists
                List<Destination> destinations = destinationRepository
                    .getSimilarDestinations(editedDestination);
                for (Destination dest : destinations) {
                    if (dest.user.id.equals(user.id) || dest.isPublic) {
                        return CompletableFuture
                            .supplyAsync(() -> badRequest(Json.toJson("Duplicate destination")));
                    }
                }
                return destinationRepository.updateDestination(editedDestination)
                    .thenApplyAsync(updatedDestination -> {
                        try {
                            return ok(sanitizeJson(Json.toJson("Successfully added destination")));
                        } catch (IOException e) {
                            return internalServerError(Json.toJson(SANITIZATION_ERROR));
                        }
                    });
            } else {
                return CompletableFuture.supplyAsync(() -> forbidden(
                    "Forbidden, user does not have permission to edit this destination"));
            }
        });
    }

    /**
     * Adds or requests to add a traveller type to a destination depending on a users privileges.
     *
     * @param request The request
     * @param destId The id of the destination to edit
     * @param travelerTypeId The id of the traveler type to add
     * @return 400 is the request is bad, 404 if the destination is not found, 500 if sanitization
     * fails, 403 if the user cannot edit the destination and 200 if successful
     */
    @With({Everyone.class, Authenticator.class})
    public CompletableFuture<Result> addTravellerType(Http.Request request, Long destId,
        Long travelerTypeId) {
        User user = request.attrs().get(ActionState.USER);
        return destinationRepository.getDestination(destId).thenComposeAsync(destination -> {
            if (destination == null) {
                return CompletableFuture
                    .supplyAsync(() -> notFound("Destination with provided ID not found"));
            }
            // Check if the type is already linked
            if (destination.isLinkedTravellerType(travelerTypeId)) {
                return CompletableFuture
                    .supplyAsync(() -> badRequest("Destination already has that traveller type"));
            }
            return travellerTypeDefinitionRepository.getTravellerTypeDefinitionById(travelerTypeId)
                .thenComposeAsync(travellerType -> {
                    if (travellerType == null) {
                        return CompletableFuture
                            .supplyAsync(
                                () -> notFound("Traveller Type with provided ID not found"));
                    }
                    //Create the link and fill in details
                    String message;
                    if (destination.user.id.equals(user.id) || user.admin) {
                        destination.travellerTypes.add(travellerType);
                        if (destination.isLinkedTravellerType(travelerTypeId)) {
                            destination.removePendingTravellerType(travelerTypeId);
                        }
                        message = "added";
                    } else {
                        if (destination.isPendingTravellerType(travelerTypeId)) {
                            return CompletableFuture
                                .supplyAsync(() -> ok(
                                    "Successfully requested traveller type to destination"));
                        }
                        destination.travellerTypesPending.add(travellerType);
                        message = "requested to add";
                    }

                    return destinationRepository.updateDestination(destination)
                        .thenApplyAsync(rows -> ok(Json.toJson(
                            "Successfully " + message + " traveller type to destination")));

                });
        });
    }

    /**
     * Gets all destinations valid for the requesting user.
     *
     * @param request Http request containing authentication information
     * @param userId ID of user to retrieve destinations for
     * @return OK status code with a list of destinations in the body
     */
    @With({Everyone.class, Authenticator.class})
    public CompletableFuture<Result> getAllDestinations(Http.Request request, Long userId) {
        User user = request.attrs().get(ActionState.USER);

        // If user is admin or requesting for their own destinations
        if (user.admin || user.id.equals(userId)) {
            return destinationRepository.getAllDestinations(userId)
                .thenApplyAsync(allDestinations -> {
                    try {
                        return ok(sanitizeJson(Json.toJson(allDestinations)));
                    } catch (IOException e) {
                        return internalServerError(Json.toJson(SANITIZATION_ERROR));
                    }
                });
        }
        // Else return only public destinations
        else {
            return destinationRepository.getAllPublicDestinations()
                .thenApplyAsync(allDestinations -> {
                    try {
                        return ok(sanitizeJson(Json.toJson(allDestinations)));
                    } catch (IOException e) {
                        return internalServerError(Json.toJson(SANITIZATION_ERROR));
                    }
                });
        }
    }

    /**
     * Gets all countries. Returns a json list of all countries.
     *
     * @return OK with list of countries
     */
    public CompletableFuture<Result> getAllCountries() {
        return countryDefinitionRepository.getAllCountries()
            .thenApplyAsync(allCountries -> {
                try {
                    return ok(sanitizeJson(Json.toJson(allCountries)));
                } catch (IOException e) {
                    return internalServerError(Json.toJson(SANITIZATION_ERROR));
                }
            });
    }

    /**
     * Gets a destination with a given id. Returns a json with destination object.
     *
     * @param getId ID of wanted destination
     * @return OK with a destination, notFound if destination does not exist
     */
    public CompletableFuture<Result> getDestination(long getId) {
        return destinationRepository.getDestination(getId).thenApplyAsync(destination -> {
            if (destination == null) {
                return notFound(Json.toJson(getId));
            } else {
                try {
                    return ok(sanitizeJson(Json.toJson(destination)));
                } catch (IOException e) {
                    return internalServerError(Json.toJson(SANITIZATION_ERROR));
                }
            }
        });
    }

    /**
     * Gets a paged list of destinations conforming to the amount of destinations requested and the
     * provided order and filters.
     *
     * @param page The current page to display
     * @param pageSize The number of destinations per page
     * @param order The column to order by
     * @param filter The sort order (either asc or desc)
     * @return OK with paged list of destinations
     */
    public CompletableFuture<Result> getPagedDestinations(int page, int pageSize, String
        order,
        String filter) {
        // TODO: Destinations should be returned here which are not currently, update API spec
        return destinationRepository.getPagedDestinations(page, pageSize, order, filter)
            .thenApplyAsync(destinations -> ok());
    }

    /**
     * Lists routes to put in JS router for use from frontend.
     *
     * @return JSRouter Play result
     */
    public Result destinationRoutes(Http.Request request) {
        return ok(
            JavaScriptReverseRouter.create("destinationRouter", "jQuery.ajax", request.host(),
                controllers.backend.routes.javascript.DestinationController.getAllCountries(),
                controllers.backend.routes.javascript.DestinationController.getAllDestinations(),
                controllers.backend.routes.javascript.DestinationController.getDestination(),
                controllers.backend.routes.javascript.DestinationController.deleteDestination(),
                controllers.frontend.routes.javascript.DestinationController
                    .detailedDestinationIndex(),
                controllers.backend.routes.javascript.DestinationController.editDestination(),
                controllers.backend.routes.javascript.DestinationController.makeDestinationPublic()
            )
        ).as(Http.MimeTypes.JAVASCRIPT);
    }
}
