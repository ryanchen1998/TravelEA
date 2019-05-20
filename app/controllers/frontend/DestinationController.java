package controllers.frontend;

import static play.libs.Scala.asScala;

import actions.ActionState;
import actions.Authenticator;
import actions.roles.Everyone;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import javax.inject.Inject;
import javax.inject.Singleton;
import models.Destination;
import models.User;
import play.libs.concurrent.HttpExecutionContext;
import play.libs.ws.WSBodyReadables;
import play.libs.ws.WSClient;
import play.libs.ws.WSResponse;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.With;
import views.html.destinations;

/**
 * This controller contains an action to handle HTTP requests to the application's destinations
 * page.
 */
@Singleton
public class DestinationController extends TEAFrontController {

    private WSClient ws;

    @Inject
    public DestinationController(WSClient ws, HttpExecutionContext httpExecutionContext) {
        super(httpExecutionContext);
        this.ws = ws;
    }

    /**
     * Displays the destinations page. Called with the /destinations URL and uses a GET request.
     * Checks that a user is logged in. Takes them to the destinations page if they are, otherwise
     * they are taken to the start page.
     *
     * @param request Http request containing authentication information
     * @return displays the destinations or start page.
     */
    @With({Everyone.class, Authenticator.class})
    public CompletableFuture<Result> index(Http.Request request) {
        User user = request.attrs().get(ActionState.USER);

        return this.getDestinations(request, user.id).thenApplyAsync(
                destList -> ok(destinations.render(user, asScala(destList))), httpExecutionContext.current());
    }

    /**
     * Gets Destinations from api endpoint via get request.
     *
     * @param request Http request containing authentication information
     * @param userId ID of user to retrieve destinations for
     * @return List of destinations
     */
    CompletableFuture<List<Destination>> getDestinations(Http.Request request, Long userId) {
        String url = "http://" + request.host() + controllers.backend.routes.DestinationController.getAllDestinations(userId);
        CompletableFuture<WSResponse> res = ws
                .url(url)
                .addHeader("Cookie", String.format("JWT-Auth=%s;", Authenticator.getTokenFromCookie(request)))
                .get()
                .toCompletableFuture();
        return res.thenApply(r -> {
            try {
                JsonNode json = r.getBody(WSBodyReadables.instance.json());
                return new ObjectMapper().readValue(new ObjectMapper().treeAsTokens(json),
                    new TypeReference<List<Destination>>() {
                    });
            } catch (Exception e) {
                return new ArrayList<>();
            }
        });
    }
}
