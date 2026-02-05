package berufsschule.raach.controllers;

import berufsschule.raach.data.ImageTag;
import berufsschule.raach.data.ImageWithMetaData;
import berufsschule.raach.exeptions.DbSearchException;
import berufsschule.raach.exeptions.UserNotFoundException;
import berufsschule.raach.services.ImageService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;
import org.bson.types.ObjectId;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import static berufsschule.raach.controllers.MainController.*;
import static berufsschule.raach.services.Util.*;

public class ImageController {

    public static final Logger logger = Logger.getLogger(ImageController.class.getName());

    private static final String API_PREFIX = "/api/image/";
    private static final ArrayList<String> mapping = new ArrayList<>(Arrays.asList(API_PREFIX + "save", API_PREFIX + "findid", API_PREFIX + "allforuser",
            API_PREFIX + "delete"));

    public static Optional<Boolean> handleImageRequest(final HttpExchange exchange) {

        final String method = exchange.getRequestMethod();

        final String path = exchange.getRequestURI().getPath();

        logger.log(Level.INFO, "Request Method: {0}, Path: {1}",
                new Object[]{method, path});

        return Optional.of(checkImageMapping(path, method, exchange));

    }

    private static Boolean checkImageMapping(String path, String method, HttpExchange exchange) {
        final String checkedPath = checkPath(path, exchange);

        if (mapping.contains(checkedPath)) {
            logger.log(Level.INFO, "Mapping found for {0}", path);
            final int mappedPath = mapping.indexOf(path);
            switch (mappedPath) {
                case 0:
                    return save(method, exchange);
                case 1:
                    return findImageById(method, exchange);
                case 2:
                    return findAllImagesForUser(method, exchange);
                case 3:
                    return delete(method, exchange);
            }
        }
        throw new IllegalArgumentException("Invalid path");
    }

    private static Boolean save(String method, HttpExchange exchange) {
        if (exchange.getRequestBody() != null && method.equals(POST) && exchange.getRequestHeaders().get(CONTENT_TYPE).contains(CONTENT_TYPE_JSON)) {
            try {

                final ImageService imageService = ImageService.getInstance();
                final ImageWithMetaData imageData = readJSON(exchange, ImageWithMetaData.class);

                if (imageData == null || imageData.byteArray() == null || imageData.filename() == null || imageData.metadata().containsKey("mail")) {
                    sendErrorResponse(exchange, 400, "Invalid payload: filename, byteArray and metadata.mail are required");
                    return false;
                }

                final boolean result = imageService.saveImage(imageData);

                if (result) {
                    exchange.sendResponseHeaders(200, -1);
                    exchange.close();
                    return true;
                } else {
                    sendErrorResponse(exchange, 500, "Error processing save request");
                }

            } catch (UserNotFoundException e) {
                sendErrorResponse(exchange, 400, "User not found");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return false;
    }


    private static Boolean findImageById(String method, HttpExchange exchange) {
        if (method.equals(GET)) {
            try {
                final ImageService imageService = ImageService.getInstance();

                final Map<String, String> queryMap = getQueryToMap(exchange.getRequestURI().getQuery());

                if (!queryMap.containsKey("id") && !queryMap.containsKey("tag")) {
                    sendErrorResponse(exchange, 400, "Invalid payload: id or tag are required");
                }

                final ObjectId id = new ObjectId(queryMap.get("id"));
                final Optional<ImageWithMetaData> foundImageOpt = imageService.findImageWithIdAndTag(id, ImageTag.valueOf(queryMap.get("tag")));

                if (foundImageOpt.isPresent()) {
                    final Gson gson = new GsonBuilder().create();
                    final String json = gson.toJson(foundImageOpt.get());
                    byte[] bytes = json.getBytes(StandardCharsets.UTF_8);

                    exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
                    exchange.sendResponseHeaders(200, bytes.length);

                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(bytes);
                    }
                    return true;
                }
            } catch (DbSearchException e) {
                sendErrorResponse(exchange, 500, "Image not found");
            } catch (IllegalArgumentException e) {
                sendErrorResponse(exchange, 400, "Invalid username or password");
            } catch (IOException e) {
                sendErrorResponse(exchange, 500, "Internal server error");
            }
        }
        return false;
    }

    private static Boolean delete(String method, HttpExchange exchange) {
        if (method.equals(DELETE)) {
            try {
                final ImageService imageService = ImageService.getInstance();
                final boolean didDelete = imageService.deleteById(exchange);
                exchange.sendResponseHeaders(204, -1);
                exchange.close();
                return didDelete;
            } catch (IOException e) {
                sendErrorResponse(exchange, 500, "Error deleting user");

            }
        }
        return false;

    }


}
