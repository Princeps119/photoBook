package berufsschule.raach.controllers;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.util.Optional;
import java.util.logging.Level;

import static berufsschule.raach.controllers.ImageController.handleImageRequest;
import static berufsschule.raach.controllers.UserController.checkMapping;
import static berufsschule.raach.services.Util.logger;

/**
 * The main controller class.
 * Handles all incoming requests.
 * Decides which controller to use.
 */
public class MainController {

    public static Optional<Boolean> processRequest(final HttpExchange exchange) {
        try {

            final String method = exchange.getRequestMethod();

            final String path = exchange.getRequestURI().getPath();

            logger.log(Level.INFO, "Request Method: {0}, Path: {1}, Query: {2}",
                    new Object[]{method, path, exchange.getRequestURI().getQuery()});

            if (path.contains("image")) {
                return handleImageRequest(exchange);
            }
            return checkMapping(path, method, exchange);

        } catch (IllegalArgumentException | IOException e) {
            //invalid mappings
            logger.log(Level.WARNING, "Error processing request", e);
            throw new IllegalArgumentException("Error processing request", e);
        }
    }
}
