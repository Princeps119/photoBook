package berufsschule.raach.controllers;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.util.Optional;
import java.util.logging.Level;

import static berufsschule.raach.controllers.ImageController.handleImageRequest;
import static berufsschule.raach.controllers.UserController.checkMapping;
import static berufsschule.raach.services.Util.logger;

public class MainController {

    public static final String POST = "POST";
    public static final String GET = "GET";
    public static final String PATCH = "PATCH";
    public static final String DELETE = "DELETE";


    public static final String CONTENT_TYPE_JSON = "application/json";
    public static final String CONTENT_TYPE = "Content-Type";

    public static Optional<Boolean> processRequest(final HttpExchange exchange) {
        try {

            final String method = exchange.getRequestMethod();

            final String path = exchange.getRequestURI().getPath();

            logger.log(Level.INFO, "Request Method: {0}, Path: {1}",
                    new Object[]{method, path});

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
