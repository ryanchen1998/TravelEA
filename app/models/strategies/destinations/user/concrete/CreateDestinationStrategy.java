package models.strategies.destinations.user.concrete;

import java.util.concurrent.CompletableFuture;
import java.util.List;
import models.NewsFeedResponseItem;
import models.strategies.destinations.user.UserDestinationStrategy;
import repository.DestinationRepository;
import repository.ProfileRepository;

public class CreateDestinationStrategy extends UserDestinationStrategy {

    /**
     * Constructor to instantiate strategy for creating a new destination
     *
     * @param destId ID of destination referenced in event
     * @param destinationRepository Reference to destination repository
     * @param userId ID of user performing the event
     * @param profileRepository Reference to profile repository
     */
    public CreateDestinationStrategy(Long destId,
        DestinationRepository destinationRepository, Long userId,
        ProfileRepository profileRepository, List<Long> eventIds) {
        super(destId, destinationRepository, userId, profileRepository, eventIds);
    }

    /**
     * Generates a response item useful for the front end, with a user-friendly message and any needed data
     *
     * @return NewsFeedResponseItem to be sent to front end
     */
    @Override
    public CompletableFuture<NewsFeedResponseItem> execute() {
        return getUserProfileAsync().thenComposeAsync(profile ->
            getReferencedDestinationAsync().thenApplyAsync(destination ->
                new NewsFeedResponseItem(
                    "has created a new destination",
                    profile.firstName + " " + profile.lastName,
                    profile.profilePhoto,
                    profile.userId,
                    destination,
                    eventIds
                )
            )
        );
    }
}
