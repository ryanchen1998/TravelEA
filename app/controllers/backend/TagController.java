package controllers.backend;

import actions.ActionState;
import actions.Authenticator;
import actions.roles.Everyone;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import javax.inject.Inject;

import models.Photo;
import models.Tag;
import models.User;
import models.enums.TagType;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.With;
import play.routing.JavaScriptReverseRouter;
import repository.PhotoRepository;
import repository.DestinationRepository;
import repository.TagRepository;
import util.objects.PagePair;

/**
 * Manages tags in the database
 */
public class TagController extends TEABackController {

    private final TagRepository tagRepository;
    private final PhotoRepository photoRepository;
    private DestinationRepository destinationRepository;
    private PhotoController photoController;


    @Inject
    public TagController(TagRepository tagRepository, PhotoRepository photoRepository,
        DestinationRepository destinationRepository, PhotoController photoController) {
        this.tagRepository = tagRepository;
        this.photoRepository = photoRepository;
        this.destinationRepository = destinationRepository;
        this.photoController = photoController;
    }

    /**
     * Controller method to get all tags of a certain type, optional name parameter to search by name.
     * Name search is case insensitive
     *
     * @param request HTTP request, needs to contain the tag type under tagType in  a JSON
     * @param name Optional sting by which to search for a name
     * @return Ok with a paginated list of tags as well as the total page count or BadRequest
     */
    public CompletableFuture<Result> getTags(Http.Request request, String name, Integer pageNum, Integer pageSize) {

        JsonNode data = request.body().asJson();
        ObjectMapper objectMapper = new ObjectMapper();
        TagType tagType = objectMapper.convertValue(data.get("tagType"), TagType.class);

        if (name != null) {
            return tagRepository.searchTags(tagType, name, pageNum, pageSize).thenApplyAsync(tags ->
            {
                try {
                    return ok(new ObjectMapper().writeValueAsString(new PagePair<Collection<?>, Integer>(tags.getList(), tags.getTotalPageCount())));
                } catch (JsonProcessingException e) {
                    return badRequest();
                }
            });
        } else {
            return tagRepository.searchTags(tagType, pageNum, pageSize)
                .thenApplyAsync(tags ->
                {
                    try {
                        return ok(new ObjectMapper().writeValueAsString(
                            new PagePair<Collection<?>, Integer>(tags.getList(),
                                tags.getTotalPageCount())));
                    } catch (JsonProcessingException e) {
                        return badRequest();
                    }
                });
        }
    }

    /**
     * Controller method to get all tags a user has used. Paginated and sorted by timeUsed
     *
     * @param request Contains the HTTP request info
     * @param userId ID of the user for whom to receive the tags
     * @param pageNum page being requested
     * @param pageSize number of tags per page
     * @return OK with the data as the tags and totalPageCount or badRequest
     */
    @With({Everyone.class, Authenticator.class})
    public CompletableFuture<Result> getUserTags(Http.Request request, Long userId, Integer pageNum, Integer pageSize) {
        return tagRepository.getRecentUserTags(userId, pageNum, pageSize).thenApplyAsync(tags ->
        {
            try {
                return ok(new ObjectMapper().writeValueAsString(new PagePair<Collection<?>, Integer>(tags.getList(), tags.getTotalPageCount())));
            } catch (JsonProcessingException e) {
                return badRequest();
            }
        });

    }

    /**
     * Retrieves all tags associated with a given user's photos. If the user is viewing their own profile or an admin
     * then retrieve all their private and public photo tags; else, only retrieve their public photo tags
     *
     * @param request Request to read cookie data from
     * @param id ID of the user to retrieve data from
     */
    @With({Everyone.class, Authenticator.class})
    public CompletableFuture<Result> getAllUserPhotoTags(Http.Request request, Long id) {
        User user = request.attrs().get(ActionState.USER);

        // Admins cannot see the users private photos
        if (user.id.equals(id)) {
            return photoRepository.getAllUserPhotos(id).thenApplyAsync(photos -> {
                Set<Tag> tags = getTagsFromPhotos(photos);
                try {
                    return ok(sanitizeJson(Json.toJson(tags)));
                } catch (IOException ex) {
                    return internalServerError(Json.toJson(SANITIZATION_ERROR));
                }
            });
        } else {
            return photoRepository.getAllPublicUserPhotos(id).thenApplyAsync(photos -> {
                Set<Tag> tags = getTagsFromPhotos(photos);
                try {
                    return ok(sanitizeJson(Json.toJson(tags)));
                } catch (IOException ex) {
                    return internalServerError(Json.toJson(SANITIZATION_ERROR));
                }
            });
        }
    }

    /**
     * Retrieves all tags from a list of user photos and returns a set of tags. Iterates through a list of photos and
     * adds each tag set for each photo to the tag set to be returned
     *
     * @param photos Array list of photo objects
     * @return a set of tags
     */
    private Set<Tag> getTagsFromPhotos(List<Photo> photos) {
        Set<Tag> tagSet = new HashSet<>();
        for (Photo photo : photos) {
            tagSet.addAll(photo.tags);
        }
        return tagSet;
    }

    @With({Everyone.class, Authenticator.class})
    public CompletableFuture<Result> getAllDestinationPhotoTags(Http.Request request, Long destinationId) {
        User user = request.attrs().get(ActionState.USER);

        return destinationRepository.getDestination(destinationId).thenApplyAsync(destination -> {
            if (destination == null) {
                return notFound(Json.toJson(destinationId));
            } else if (!destination.isPublic && !destination.user.id.equals(user.id)
                && !user.admin) {
                return forbidden();
            } else {
                List<Photo> photos = photoController.filterPhotos(destination.destinationPhotos, user.id);
                return ok(Json.toJson(getTagsFromPhotos(photos)));
            }
        });
    }

    /**
     * Lists routes to put in JS router for use from frontend.
     *
     * @return JSRouter Play result
     */
    public Result tagRoutes (Http.Request request) {
        return ok(
            JavaScriptReverseRouter.create("tagRouter", "jQuery.ajax", request.host(),
                controllers.backend.routes.javascript.TagController.getAllUserPhotoTags(),
                controllers.backend.routes.javascript.TagController.getAllDestinationPhotoTags()
            )
        ).as(Http.MimeTypes.JAVASCRIPT);
    }
}
