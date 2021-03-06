package controllers.backend;

import actions.ActionState;
import actions.Authenticator;
import actions.roles.Everyone;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import javax.imageio.ImageIO;
import models.NewsFeedEvent;
import models.Photo;
import models.Tag;
import models.User;
import models.enums.NewsFeedEventType;
import play.libs.Files;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Results;
import play.mvc.With;
import play.routing.JavaScriptReverseRouter;
import repository.DestinationRepository;
import repository.NewsFeedEventRepository;
import repository.PhotoRepository;
import repository.ProfileRepository;
import repository.TagRepository;
import util.objects.PagingResponse;
import util.objects.Pair;
import util.validation.ErrorResponse;


@SuppressWarnings("SpellCheckingInspection")
public class PhotoController extends TEABackController {

    // Constant fields defining the directories of regular photos and test photos
    private static final String PHOTO_DIRECTORY = "/storage/photos/";
    private static final String TEST_PHOTO_DIRECTORY = "/storage/photos/test/";

    // Constant fields defining the directory of publicly available files
    private static final String PUBLIC_DIRECTORY = "/public";

    // Default dimensions of thumbnail images
    private static final int THUMB_WIDTH = 400;
    private static final int THUMB_HEIGHT = 266;
    private static final int PROFILE_THUMB_HEIGHT = 150;
    private static final int PROFILE_THUMB_WIDTH = 150;
    private final String savePath;

    // Caption and tag field name constants
    private static final String CAPTION = "caption";
    private static final String TAGS = "tags";

    // Repositories to handle DB transactions
    private PhotoRepository photoRepository;
    private ProfileRepository profileRepository;
    private DestinationRepository destinationRepository;
    private TagRepository tagRepository;
    private NewsFeedEventRepository newsFeedEventRepository;

    @Inject
    public PhotoController(DestinationRepository destinationRepository,
        PhotoRepository photoRepository,
        ProfileRepository profileRepository,
        TagRepository tagRepository,
        NewsFeedEventRepository newsFeedEventRepository) {

        this.destinationRepository = destinationRepository;
        this.photoRepository = photoRepository;
        this.profileRepository = profileRepository;
        this.tagRepository = tagRepository;
        this.newsFeedEventRepository = newsFeedEventRepository;

        // Create photo directories if none exist
        String directoryName = System.getProperty("user.dir");
        File directory = new File(directoryName + "/public/storage/photos/test/thumbnails");
        if (!directory.exists()) {
            directory.mkdirs();
        }
        File directory2 = new File(directoryName + "/public/storage/photos/thumbnails");
        if (!directory2.exists()) {
            directory2.mkdirs();
        }

        savePath = directoryName + PUBLIC_DIRECTORY;
    }

    /**
     * Takes a path to a file and returns the file object.
     *
     * @param filePath path to file to read
     */
    public Result getPhotoFromPath(String filePath) {
        File file = new File(filePath);
        return ok(file, true);
    }

    /**
     * Updates the caption and tags associated with a photo
     *
     * @param request HTTP request containing authentication information and request body
     * @param photoId ID of photo being updated
     * @return Response status, on ok sends old photo data for use with undo/redo
     */
    @With({Everyone.class, Authenticator.class})
    public CompletableFuture<Result> updatePhotoDetails(Http.Request request, Long photoId) {
        User user = request.attrs().get(ActionState.USER);
        JsonNode data = request.body().asJson();

        // Check if caption or tags field is missing, if so return badRequest
        if (!data.has(TAGS) || !data.has(CAPTION)) {
            return CompletableFuture.supplyAsync(Results::badRequest);
        }

        // Retrieve new photo details from request body, return badRequest if fails
        Photo newPhotoDetails;
        try {
            newPhotoDetails = Json.fromJson(data, Photo.class);
        } catch (Exception e) {
            return CompletableFuture.supplyAsync(Results::badRequest);
        }

        // Get the photo to have caption updated
        return photoRepository.getPhotoById(photoId).thenComposeAsync(photo -> {
            // Check if photo exists and then if user is authorized to perform this action
            if (photo == null) {
                return CompletableFuture
                    .supplyAsync(() -> notFound("Could not find photo to update"));
            } else if (!photo.userId.equals(user.id) && !user.admin) {
                return CompletableFuture.supplyAsync(
                    () -> forbidden("You do not have permission to update this photo"));
            }

            // Adds new tags and retrieves existing tags
            return tagRepository.addTags(newPhotoDetails.tags).thenComposeAsync(tags -> {
                // Stores the old caption and tag data
                ObjectMapper mapper = new ObjectMapper();
                ObjectNode oldData = mapper.createObjectNode();
                oldData.set(CAPTION, Json.toJson(photo.caption));
                oldData.set(TAGS, Json.toJson(photo.tags));

                // Sets fields with new photo data and updates
                photo.caption = newPhotoDetails.caption;
                photo.tags = tags;

                return photoRepository.updatePhoto(photo).thenApplyAsync(guid -> {
                    try {
                        return ok(sanitizeJson(oldData));
                    } catch (IOException ex) {
                        return internalServerError(Json.toJson(SANITIZATION_ERROR));
                    }
                });
            });
        });
    }

    /**
     * Sets a users photo as their profile photo
     *
     * @param request Http request containing authentication and ID of new profile photo
     * @param userId ID of user updating the photo
     * @return Http response, on ok returns ID of old profile photo
     */
    @With({Everyone.class, Authenticator.class})
    public CompletableFuture<Result> makePhotoProfile(Http.Request request, Long userId) {
        User loggedInUser = request.attrs().get(ActionState.USER);

        return profileRepository.findID(userId).thenComposeAsync(profile -> {
            if (profile == null) {
                return CompletableFuture.supplyAsync(() -> notFound("User does not exist"));
            }

            // Get photoId from request body
            Long photoId = Json.fromJson(request.body().asJson(), Long.class);

            return photoRepository.getPhotoById(photoId).thenComposeAsync(photo -> {
                if (photo == null) {
                    return CompletableFuture.supplyAsync(() -> notFound("Photo does not exist"));
                } else if (!photo.userId.equals(loggedInUser.id) && !loggedInUser.admin) {
                    return CompletableFuture.supplyAsync(() -> forbidden(
                        "You do not have permission to make this photo a profile picture"));
                }

                // Store old profile photo ID to send in response
                Photo oldProfilePhoto = profile.profilePhoto;

                // Update profile photo and return previous profile photo ID
                profile.profilePhoto = photo;

                return profileRepository.updateProfile(profile).thenComposeAsync(profileId -> {
                    // Create news feed event for profile update
                    NewsFeedEvent newsFeedEvent = new NewsFeedEvent();
                    newsFeedEvent.userId = userId;
                    newsFeedEvent.refId = photoId;
                    newsFeedEvent.eventType = NewsFeedEventType.NEW_PROFILE_PHOTO.name();

                    return newsFeedEventRepository.addNewsFeedEvent(newsFeedEvent)
                        .thenApplyAsync(id -> {
                            if (oldProfilePhoto == null) {
                                return ok(Json.newObject().nullNode());
                            }

                            try {
                                return ok(sanitizeJson(Json.toJson(oldProfilePhoto.guid)));
                            } catch (IOException ex) {
                                return internalServerError(Json.toJson(SANITIZATION_ERROR));
                            }
                        });
                });
            });
        });
    }

    /**
     * Sets the cover photo for a user with a given user id.
     *
     * @param request the HTTP request
     * @param id the id of the user who is having their cover photo changed
     * @return the if of the original cover photo before it was changed (possibly null)
     */
    @With({Everyone.class, Authenticator.class})
    public CompletableFuture<Result> setCoverPhoto(Http.Request request, Long id) {
        User user = request.attrs().get(ActionState.USER);
        Long currentUserId = user.id;

        // Check if user is authorized to perform this action
        if (!id.equals(currentUserId) && !user.admin) {
            return CompletableFuture.supplyAsync(Results::forbidden);
        }

        // Get json parameters
        Long newPhotoId = Json.fromJson(request.body().asJson(), Long.class);

        // Update cover photo, and get the prior photo id
        try {
            return profileRepository.updateCoverPhotoAndReturnExistingId(id, newPhotoId)
                .thenComposeAsync(returnedId -> {
                        // Create news feed event for profile update
                        NewsFeedEvent newsFeedEvent = new NewsFeedEvent();
                        newsFeedEvent.userId = id;
                        newsFeedEvent.refId = newPhotoId;
                        newsFeedEvent.eventType = NewsFeedEventType.NEW_PROFILE_COVER_PHOTO.name();

                        return newsFeedEventRepository.addNewsFeedEvent(newsFeedEvent)
                            .thenApplyAsync(eventId ->
                                returnedId != null ? ok(Json.toJson(returnedId))
                                    : ok(Json.newObject().nullNode())
                            );
                    }
                );
        } catch (NullPointerException e) {
            return CompletableFuture
                .supplyAsync(() -> badRequest(Json.toJson("No such profile found")));
        }
    }

    /**
     * Returns all photos belonging to a user. Only returns public photos if it is not the owner of
     * the photos getting them
     *
     * @param request Request to read cookie data from
     * @param userId ID of user to get photos of
     * @param pageNum Page number of photos to retrieve
     * @param pageSize Number of photos to retrieve
     * @param requestOrder Request count identifier the users most recent request
     * @return A paged list of the users photos
     */
    @With({Everyone.class, Authenticator.class})
    public CompletableFuture<Result> getAllUserPhotos(Http.Request request, Long userId,
        Integer pageNum, Integer pageSize, Integer requestOrder) {
        Long currentUserId = request.attrs().get(ActionState.USER).id;
        // Checks if the photos belong to the user getting them
        if (currentUserId.equals(userId)) {
            // get public and private photos
            return photoRepository.getPagedUserPhotos(userId, pageNum, pageSize)
                .thenApplyAsync(photos -> {
                    try {
                        return ok(sanitizeJson(Json.toJson(
                            new PagingResponse<>(photos.getList(), requestOrder,
                                photos.getTotalPageCount()))));
                    } catch (IOException ex) {
                        return internalServerError(SANITIZATION_ERROR);
                    }
                });
        } else {
            // only get public photos
            return photoRepository.getPagedPublicUserPhotos(userId, pageNum, pageSize)
                .thenApplyAsync(photos -> {
                    try {
                        return ok(sanitizeJson(Json.toJson(
                            new PagingResponse<>(photos.getList(), requestOrder,
                                photos.getTotalPageCount()))));
                    } catch (IOException ex) {
                        return internalServerError(SANITIZATION_ERROR);
                    }
                });
        }
    }

    /**
     * Uploads any number of photos from a multipart/form-data request.
     *
     * @param request Request where body is a multipart form-data
     * @return Result of query
     */
    @With({Everyone.class, Authenticator.class})
    public CompletableFuture<Result> upload(Http.Request request) {
        User loggedInUser = request.attrs().get(ActionState.USER);

        // Get the request body, and turn it into a
        // multipart form data collection of temporary files
        Http.MultipartFormData<Files.TemporaryFile> body = request.body().asMultipartFormData();

        // Get all basic string keys in multipart form
        Map<String, String[]> formKeys = body.asFormUrlEncoded();

        // Store in a boolean whether or not this is a test file
        boolean isTest = Boolean
            .parseBoolean(formKeys.getOrDefault("isTest", new String[]{"false"})[0]);

        // Id of user that photo is being uploaded for, only applicable when admin is uploading
        Long userIdForUpload;
        String userUploadVal = formKeys.getOrDefault("userUploadId", new String[]{""})[0];
        if (!userUploadVal.equals("")) {
            try {
                userIdForUpload = Long.parseLong(userUploadVal);
                // If user is not an admin and uploading for someone else, throw forbidden
                if (!loggedInUser.admin && !userIdForUpload.equals(loggedInUser.id)) {
                    return CompletableFuture.supplyAsync(() -> forbidden(
                        Json.toJson("Non-admin users can only upload photos for themselves.")));
                }
            } catch (Exception e) {
                return CompletableFuture
                    .supplyAsync(() -> badRequest(Json.toJson("Could not parse userUploadId")));
            }
        } else { // If no userId specified, upload for themselves
            userIdForUpload = loggedInUser.id;
        }

        // Keep track of which file should be uploaded as a profile
        String profilePhotoFilename = formKeys
            .getOrDefault("profilePhotoName", new String[]{null})[0];

        // Keep track of which photos are marked as public
        HashSet<String> publicPhotoFileNames = new HashSet<>(Arrays.asList(
            formKeys.getOrDefault("publicPhotoFileNames", new String[]{""})[0].split(",")));

        String[] photoCaptions =
            (formKeys.get(CAPTION) == null) ? new String[]{""} : formKeys.get(CAPTION);

        // Store photos in a list to allow them all to
        // be uploaded at the end if all are read successfully
        ArrayList<Pair<Photo, Http.MultipartFormData.FilePart<Files.TemporaryFile>>>
            photos = new ArrayList<>();

        Set<Tag> photoTags;
        try {
            final String tagString = formKeys.getOrDefault(TAGS, new String[]{"[]"})[0];
            photoTags = new HashSet<>(Arrays.asList(
                Json.fromJson(new ObjectMapper().readTree(tagString), Tag[].class)));
        } catch (IOException e) {
            return CompletableFuture
                .supplyAsync(() -> internalServerError());
        }
        return tagRepository.addTags(photoTags).thenComposeAsync(tags -> {
            // Iterate through all files in the request
            int position = 0;
            for (Http.MultipartFormData.FilePart<Files.TemporaryFile> file : body.getFiles()) {
                if (file != null) {
                    try {
                        String caption =
                            (position >= photoCaptions.length) ? "" : photoCaptions[position];
                        position += 1;
                        // Make file public if admin is uploading for somone else
                        if (loggedInUser.admin && !userIdForUpload.equals(loggedInUser.id)) {
                            publicPhotoFileNames.add(file.getFilename());
                        }
                        // Store file with photo in list to be added later
                        photos.add(new Pair<>(
                            readFileToPhoto(file, publicPhotoFileNames,
                                userIdForUpload, isTest, caption, tags), file));
                    } catch (IOException e) {
                        // If an invalid file type given, return bad request
                        // with error message generated in exception
                        return CompletableFuture
                            .supplyAsync(() -> badRequest(Json.toJson(e.getMessage())));
                    }
                } else {
                    // If any uploads fail, return bad request immediately
                    return CompletableFuture
                        .supplyAsync(() -> badRequest(Json.toJson("Missing file")));
                }
            }

            // If no photos were actually found, and no other error has been thrown, throw it now
            if (photos.isEmpty()) {
                return CompletableFuture
                    .supplyAsync(() -> badRequest(Json.toJson("No files given")));
            } else {
                try {
                    return saveMultiplePhotos(photos, loggedInUser, profilePhotoFilename != null);
                } catch (IOException e) {
                    return CompletableFuture.supplyAsync(() -> internalServerError(
                        Json.toJson("Unknown number of photos failed to save")));
                }
            }
        });
    }

    /**
     * Saves multiple photo files to storage folder, and inserts reference to them to database.
     *
     * @param photos Collection of pairs of Photo and HTTP multipart form data file parts
     */
    private CompletableFuture<Result> saveMultiplePhotos(
        Collection<Pair<Photo, Http.MultipartFormData.FilePart<Files.TemporaryFile>>> photos,
        User user, Boolean useProfileThumbnailSize)
        throws IOException {
        // Add all the photos we found to the database
        int thumbWidth = THUMB_WIDTH;
        int thumbHeight = THUMB_HEIGHT;
        for (Pair<Photo, Http.MultipartFormData.FilePart<Files.TemporaryFile>> pair : photos) {
            // if photo to add is marked as new profile pic, clear any existing profile pic first
            if (useProfileThumbnailSize) {
                // Profile picture small thumbnail dimensions
                thumbWidth = PROFILE_THUMB_WIDTH;
                thumbHeight = PROFILE_THUMB_HEIGHT;
            }

            // Buffer the image and use same file creation process as
            BufferedImage fullImage = ImageIO.read(pair.getValue().getRef().path().toFile());
            // Create and save main image
            createPhotoFromFile(pair.getValue().getRef(), fullImage.getWidth(),
                fullImage.getHeight()).copyTo(Paths.get(pair.getKey().filename));
            // Create and save thumbnail image
            createPhotoFromFile(pair.getValue().getRef(), thumbWidth, thumbHeight)
                .copyTo(Paths.get(pair.getKey().thumbnailFilename));

        }
        // Collect all keys from the list to upload
        List<Photo> photosToAdd = photos.stream().map(Pair::getKey)
            .collect(Collectors.toList());

        // If this photo is going to be added as profile picture, return the name of it
        if (useProfileThumbnailSize) {
            photosToAdd.get(0).usedForProfile = true;
            return photoRepository.addPhotos(photosToAdd, user).thenApplyAsync(addedPhotos -> {
                // Return filename of photo that was just added
                addedPhotos.get(0).thumbnailFilename =
                    "../user_content/" + addedPhotos.get(0).thumbnailFilename;
                addedPhotos.get(0).filename = "../user_content/" + addedPhotos.get(0).filename;
                return created(Json.toJson(addedPhotos.get(0)));
            });
        } else {
            return photoRepository.addPhotos(photosToAdd, user).thenApplyAsync(
                addedPhotos -> created(Json.toJson("File(s) uploaded successfully")));
        }
    }

    /**
     * Reads a file part from the multipart form and returns a Photo object to add to the database.
     *
     * @param file File part from form
     * @param publicPhotoFileNames Names (if any) of photos to be set to public, defaults to private
     * if referenced here
     * @param userId ID of user who is uploading the files
     * @param isTest Whether or not these photos should be added to test folder of storage
     * @param caption Caption for photo
     * @return Photo object to be added to database
     * @throws IOException Thrown when an unsupported file type added (i.e not image/jpeg or
     * image/png)
     */
    private Photo readFileToPhoto(Http.MultipartFormData.FilePart<Files.TemporaryFile> file,
        HashSet<String> publicPhotoFileNames, long userId,
        boolean isTest, String caption, Set<Tag> tags) throws IOException {
        // Get the filename, file size and content-type of the file
        int randomNumber = (int) (Math.random() * 496148154 + 1);
        String[] filenameParts = file.getFilename().split("\\.");
        String fileName = System.currentTimeMillis() + "_" + randomNumber + "." + filenameParts[
            filenameParts.length - 1];

        String contentType = file.getContentType();
        if (!contentType.equals("image/jpeg") && !contentType.equals("image/png")) {
            // If any file is found that is not a png or jpeg reject all files with error message
            throw new IOException("Invalid file type given: " + contentType);
        }

        // Create a photo object
        Photo photo = new Photo();
        photo.filename = (savePath + ((isTest) ? TEST_PHOTO_DIRECTORY : PHOTO_DIRECTORY)
            + fileName);
        photo.isPublic = publicPhotoFileNames.contains(file.getFilename());
        photo.thumbnailFilename = (savePath + ((isTest) ? TEST_PHOTO_DIRECTORY : PHOTO_DIRECTORY)
            + "thumbnails/" + fileName);
        photo.uploaded = LocalDateTime.now();
        photo.userId = userId;
        photo.usedForProfile = false;
        photo.caption = caption;
        photo.tags = tags;
        return photo;
    }

    /**
     * Creates a new image at default thumbnail size of a given image.
     *
     * @param fullImageFile The full image to create a filename for
     * @return Temporary file of thumbnail
     */
    private Files.TemporaryFile createPhotoFromFile(Files.TemporaryFile fullImageFile,
        int thumbWidth, int thumbHeight) throws IOException {
        BufferedImage thumbImage = new BufferedImage(thumbWidth, thumbHeight,
            BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics2D = thumbImage
            .createGraphics(); //create a graphics object to paint to
        graphics2D.setBackground(Color.WHITE);
        graphics2D.setPaint(Color.WHITE);
        graphics2D.fillRect(0, 0, thumbWidth, thumbHeight);
        graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
            RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        // Convert full image file to java.awt image
        BufferedImage fullImage = ImageIO.read(fullImageFile.path().toFile());

        // If image is smaller than thumbnail size then center with bars on each side
        if (fullImage.getWidth() < thumbWidth && fullImage.getHeight() < thumbHeight) {
            graphics2D.drawImage(fullImage, thumbWidth / 2 - fullImage.getWidth() / 2,
                thumbHeight / 2 - fullImage.getHeight() / 2, fullImage.getWidth(),
                fullImage.getHeight(), null);
        } // Otherwise scale image down so biggest side
        // is set to max of thumbnail and rest is scaled proportionally
        else {
            // Determine which side is proportionally bigger
            boolean fitWidth =
                fullImage.getWidth() / thumbWidth > fullImage.getHeight() / thumbHeight;
            double scaleFactor =
                (fitWidth) ? (double) thumbWidth / (double) fullImage.getWidth()
                    : (double) thumbHeight / (double) fullImage.getHeight();
            if (fitWidth) {
                int newHeight = (int) Math.floor(fullImage.getHeight() * scaleFactor);
                graphics2D
                    .drawImage(fullImage, 0, thumbHeight / 2 - newHeight / 2, thumbWidth,
                        newHeight,
                        null);
            } else {
                int newWidth = (int) Math.floor(fullImage.getWidth() * scaleFactor);
                graphics2D
                    .drawImage(fullImage, thumbWidth / 2 - newWidth / 2, 0, newWidth,
                        thumbHeight,
                        null);
            }
        }

        int randomNumber = (int) (Math.random() * 496148154 + 1);
        String fileName = System.currentTimeMillis() + "_" + randomNumber + ".jpg";

        // Create file to store output of thumbnail write
        File thumbFile = new File(savePath + TEST_PHOTO_DIRECTORY + fileName);

        // Write buffered image to thumbnail file
        ImageIO.write(thumbImage, "jpg", thumbFile);

        // Return temporary file created from the file
        Files.TemporaryFile temporaryFile = (new Files.SingletonTemporaryFileCreator())
            .create(thumbFile.toPath());

        // Delete the file that it was buffered to
        thumbFile.deleteOnExit();

        return temporaryFile;
    }

    /**
     * Deletes a photo with given id. Return a result with a json int which represents the number of
     * rows that were deleted. So if the return value is 0, no photo was found to delete
     *
     * @param id ID of photo to delete
     * @return OK with number of rows deleted, bad request if none deleted
     */
    @With({Everyone.class, Authenticator.class})
    public CompletableFuture<Result> deletePhoto(Long id) {
        return photoRepository.deletePhoto(id).thenComposeAsync(photoDeleted -> {
            if (photoDeleted == null) {
                ErrorResponse errorResponse = new ErrorResponse();
                errorResponse.map("Photo not found", "other");
                return CompletableFuture.supplyAsync(() -> badRequest(errorResponse.toJson()));
            } else {
                // Delete any newsfeed events related to that photo
                return newsFeedEventRepository.cleanUpPhotoEvents(photoDeleted)
                    .thenApplyAsync(rows -> {
                        // Mark the files for deletion
                        File thumbFile = new File(savePath + photoDeleted.thumbnailFilename);
                        File mainFile = new File(savePath + photoDeleted.filename);
                        if (!thumbFile.delete()) {
                            // If file fails to delete immediately,
                            // mark file for deletion when VM shuts down
                            thumbFile.deleteOnExit();
                        }
                        if (!mainFile.delete()) {
                            // If file fails to delete immediately,
                            // mark file for deletion when VM shuts down
                            mainFile.deleteOnExit();
                        }

                        // Return number of photos deleted
                        return ok(Json.toJson(1));
                    });
            }
        });
    }

    /**
     * Gets photo by photo guid
     *
     * @param request Request to read cookie data from
     * @param photoId guid of photo to get
     * @return Photo
     */
    @With({Everyone.class, Authenticator.class})
    public CompletableFuture<Result> getPhotoById(Http.Request request, Long photoId) {
        User user = request.attrs().get(ActionState.USER);
        Long currentUserId = user.id;
        return photoRepository.getPhotoById(photoId).thenApplyAsync(photo -> {
            // Check if photo exists and then if user is authorized to perform this action
            if (photo == null) {
                return notFound();
            } else if (!photo.userId.equals(currentUserId) && !user.admin) {
                return forbidden();
            }
            return ok(Json.toJson(photo));
        });
    }

    /**
     * Toggles the privacy of a photo.
     *
     * @param request Request to read cookie data from
     * @param id id of photo to toggle
     */
    @With({Everyone.class, Authenticator.class})
    public CompletableFuture<Result> togglePhotoPrivacy(Http.Request request, Long id) {
        User user = request.attrs().get(ActionState.USER);
        Long currentUserId = user.id;
        return photoRepository.getPhotoById(id).thenComposeAsync(photo -> {
            final Photo existingPhoto = photo;
            final Boolean oldStatus;
            if (photo != null) {
                photo.isPublic = !photo.isPublic;
                oldStatus = existingPhoto.isPublic;
            } else {
                return CompletableFuture.supplyAsync(Results::notFound);
            }
            if (!oldStatus) {
                // Photo was public, making it prrivate, no newsfeed event
                return newsFeedEventRepository.cleanUpPhotoEvents(photo)
                    .thenComposeAsync(deleted_rows -> {
                        return photoRepository.updatePhoto(photo)
                            .thenApplyAsync(updated_rows -> ok(Json.toJson(existingPhoto)));
                    });
            }
            return photoRepository.updatePhoto(photo)
                .thenComposeAsync(rows -> {
                    // Create news feed event for public photo added
                    NewsFeedEvent newsFeedEvent = new NewsFeedEvent();
                    newsFeedEvent.userId = currentUserId;
                    newsFeedEvent.refId = id;
                    newsFeedEvent.eventType = NewsFeedEventType.UPLOADED_USER_PHOTO.name();

                    return newsFeedEventRepository.addNewsFeedEvent(newsFeedEvent)
                        .thenApplyAsync(eventId ->
                            ok(Json.toJson(existingPhoto))
                        );
                });
        });
    }

    /**
     * Links and deletes links from photo to a destination.
     *
     * @param request Request
     * @param photoId id of photo
     * @param destId id of destination
     * @return OK if successful or not found
     */
    @With({Everyone.class, Authenticator.class})
    public CompletableFuture<Result> deleteLinkPhotoToDest(Http.Request request, Long destId,
        Long photoId) {
        Long userId = request.attrs().get(ActionState.USER).id;
        return photoRepository.getDeletedDestPhoto(photoId, destId)
            .thenComposeAsync(deletedPhoto -> {
                return destinationRepository.getDestination(destId)
                    .thenComposeAsync(destination -> {
                        return photoRepository.getPhotoById(photoId).thenComposeAsync(photo -> {
                            if (deletedPhoto == null) {
                                if (destination != null) {
                                    if (!destination.isPublic && !destination.user.id
                                        .equals(userId)) {
                                        //forbidden if destination is private and user does not own destination
                                        return CompletableFuture
                                            .supplyAsync(Results::forbidden);
                                    }
                                    if (destination.isPublic && !destination.user.id
                                        .equals(userId)) {
                                        //Set destination owner to admin if photo is added from diffrent user
                                        destinationRepository
                                            .changeDestinationOwner(destId, MASTER_ADMIN_ID)
                                            .thenApplyAsync(rows -> rows); //set to master admin
                                    }
                                    if (destination.isLinked(photoId)) {
                                        //if photo is already linked return badrequest
                                        return CompletableFuture
                                            .supplyAsync(Results::badRequest);
                                    }
                                    if (photo == null) {
                                        return CompletableFuture.supplyAsync(Results::notFound);
                                    }
                                    //Link photo to destination
                                    destination.destinationPhotos.add(photo);
                                    return destinationRepository.updateDestination(destination)
                                        .thenApplyAsync(dest -> {
                                            NewsFeedEvent newsFeedEvent = new NewsFeedEvent();
                                            newsFeedEvent.refId = photoId;
                                            newsFeedEvent.destId = destId;
                                            newsFeedEvent.userId = userId;
                                            newsFeedEvent.eventType = NewsFeedEventType.LINK_DESTINATION_PHOTO
                                                .name();

                                            if (photo.isPublic) { newsFeedEventRepository.addNewsFeedEvent(newsFeedEvent); }
                                            return ok(Json.toJson("Succesfully Updated"));
                                        });
                                }
                                return CompletableFuture.supplyAsync(Results::notFound);
                            } else {
                                if (photo == null) {
                                    return CompletableFuture.supplyAsync(Results::notFound);
                                }
                                if (!photo.removeDestination(destId)) {
                                    return CompletableFuture.supplyAsync(Results::notFound);
                                }
                            }
                            deletedPhoto.deleted = !deletedPhoto.deleted;
                            if (deletedPhoto.deleted) {
                                newsFeedEventRepository.cleanUpPhotoEvents(photo);
                            }
                            return photoRepository.updatePhoto(photo)
                                .thenApplyAsync(rows -> ok(Json.toJson(deletedPhoto.guid)));
                        });
                    });
            });
    }

    /**
     * Gets destination photos based on logged in user. Desttination photos not currently paginated
     *
     * @param request Request containing authentication information
     * @param destId id of destination
     * @return A list of photos associated with the destination
     */
    @With({Everyone.class, Authenticator.class})
    public CompletableFuture<Result> getDestinationPhotos(Http.Request request, Long destId) {
        User user = request.attrs().get(ActionState.USER);
        return destinationRepository.getDestination(destId).thenApplyAsync(destination -> {
            if (destination == null) {
                return notFound(Json.toJson(destId));
            } else if (!destination.isPublic && !destination.user.id.equals(user.id)
                && !user.admin) {
                return forbidden();
            } else {
                List<Photo> photos = filterPhotos(destination.destinationPhotos, user.id);
                try {
                    photos = photoRepository.appendAssetsUrlNoPage(photos);
                    return ok(sanitizeJson(Json.toJson(photos)));
                } catch (IOException e) {
                    return internalServerError(Json.toJson(SANITIZATION_ERROR));
                }
            }
        });
    }

    /**
     * Removes photos that shouldnt be seen by given user.
     *
     * @param photos List of photos
     * @param userId Id of authenticated user
     * @return List of filtered photos
     */
    public List<Photo> filterPhotos(List<Photo> photos, Long userId) {
        Iterator<Photo> iter = photos.iterator();
        while (iter.hasNext()) {
            Photo photo = iter.next();
            if (!photo.isPublic && !photo.userId.equals(userId)) {
                iter.remove();
            }
        }
        return photos;
    }

    /**
     * Lists routes to put in JS router for use from frontend.
     *
     * @return JSRouter Play result
     */
    public Result photoRoutes(Http.Request request) {
        return ok(
            JavaScriptReverseRouter.create("photoRouter", "jQuery.ajax", request.host(),
                controllers.backend.routes.javascript.PhotoController.upload(),
                controllers.backend.routes.javascript.PhotoController.togglePhotoPrivacy(),
                controllers.backend.routes.javascript.PhotoController.getAllUserPhotos(),
                controllers.backend.routes.javascript.PhotoController.deleteLinkPhotoToDest(),
                controllers.backend.routes.javascript.PhotoController.getDestinationPhotos(),
                controllers.backend.routes.javascript.PhotoController.makePhotoProfile(),
                controllers.backend.routes.javascript.PhotoController.setCoverPhoto(),
                controllers.backend.routes.javascript.PhotoController.updatePhotoDetails(),
                controllers.backend.routes.javascript.PhotoController.getPhotoById()
            )
        ).as(Http.MimeTypes.JAVASCRIPT);
    }

}