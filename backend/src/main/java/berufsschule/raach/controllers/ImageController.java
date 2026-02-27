package berufsschule.raach.controllers;

import berufsschule.raach.data.imageData.ImageSummaryData;
import berufsschule.raach.data.imageData.ImageTag;
import berufsschule.raach.data.imageData.ImageUploadData;
import berufsschule.raach.data.imageData.ImageWithIDData;
import berufsschule.raach.exeptions.DbSearchException;
import berufsschule.raach.exeptions.UserNotFoundException;
import berufsschule.raach.repo.MongoRepo;
import berufsschule.raach.services.ImageService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;
import org.bson.types.ObjectId;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static berufsschule.raach.controllers.MainController.*;
import static berufsschule.raach.services.Util.*;

public class ImageController {

    public static final Logger logger = Logger.getLogger(ImageController.class.getName());

    private static final String API_PREFIX = "/api/image/";
    private static final String API_ENDPOINT_SAVE = "save";
    private static final String API_ENDPOINT_FIND_ID = "findid";
    private static final String API_ENDPOINT_ALL_FOR_USER = "allforuser";
    private static final String API_ENDPOINT_DELETE = "delete";
    private static final String API_ENDPOINT_ALL_PUBLIC = "allpublic";

    private static final ArrayList<String> mapping = new ArrayList<>(Arrays.asList(API_PREFIX + API_ENDPOINT_SAVE, API_PREFIX + API_ENDPOINT_FIND_ID, API_PREFIX + API_ENDPOINT_ALL_FOR_USER,
            API_PREFIX + API_ENDPOINT_DELETE, API_PREFIX + API_ENDPOINT_ALL_PUBLIC));

    private static final String USER_COLLECTION_NAME = "users";
    private static final MongoRepo userDB = MongoRepo.getInstance();

    public static Optional<Boolean> handleImageRequest(final HttpExchange exchange) {

        final String method = exchange.getRequestMethod();

        final String path = exchange.getRequestURI().getPath();

        logger.log(Level.INFO, "Request Method: {0}, Path: {1}, Query: {2}",
                new Object[]{method, path, exchange.getRequestURI().getQuery()});

        return Optional.of(checkImageMapping(path, method, exchange));
    }

    private static boolean checkImageMapping(String path, String method, HttpExchange exchange) {
        final String checkedPath = checkPathImage(path, exchange);


        String deletePathWithoutId = null;
        final String token = API_ENDPOINT_DELETE;

        if (checkedPath != null) {
            int pos = checkedPath.indexOf(token);
            if (pos >= 0) {
                deletePathWithoutId = checkedPath.substring(0, pos + token.length());
            }
        }

        // Pick either deletePathWithoutId OR checkedPath
        final String key = (deletePathWithoutId != null) ? deletePathWithoutId : checkedPath;

        // Check mapping only once
        int mappedPath = mapping.indexOf(key);

        if (mappedPath >= 0) {
            logger.log(Level.INFO, "Mapping found for {0}", path);

            switch (mappedPath) {
                case 0:
                    return save(method, exchange);
                case 1:
                    String timestamp = new Date().toString();
                    logger.log(Level.INFO, "Finding image by id time: " + timestamp);
                    return findImageById(method, exchange);
                case 2:
                    return findAllImagesForUser(method, exchange);
                case 3:
                    return delete(method, exchange);
                case 4:
                    return findAllImagesForPublic(exchange);
            }
        }
        throw new IllegalArgumentException("Invalid path");
    }

    private static boolean findAllImagesForPublic(HttpExchange exchange) {
        final ImageService imageService = ImageService.getInstance();
        List<ImageSummaryData> images = imageService.findAllImages();

        try {
            final Gson gson = new GsonBuilder().create();
            final String json = gson.toJson(images);
            byte[] bytes = json.getBytes(StandardCharsets.UTF_8);

            exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
            exchange.sendResponseHeaders(200, bytes.length);

            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        } catch (IOException e) {
            logger.log(Level.WARNING, "Error writing response", e);
            sendErrorResponse(exchange, 500, "Internal server error");
        } finally {
            exchange.close();
        }
        return true;
    }

    private static boolean findAllImagesForUser(String method, HttpExchange exchange) {
        if (method.equals(GET)) {
            try {
                final ImageService imageService = ImageService.getInstance();
                Optional<List<ImageSummaryData>> imagesOp = imageService.getAllImageSummariesForUser(exchange);

                imagesOp.ifPresent(images -> {
                    try {
                        logger.log(Level.INFO, "Found {0} images for user", images.size());

                        final Gson gson = new GsonBuilder().create();
                        final String json = gson.toJson(images);
                        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);

                        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
                        exchange.sendResponseHeaders(200, bytes.length);

                        try (OutputStream os = exchange.getResponseBody()) {
                            os.write(bytes);
                        }
                    } catch (IOException e) {
                        logger.log(Level.WARNING, "Error writing response", e);
                        sendErrorResponse(exchange, 500, "Internal server error");
                    }
                });
                if (imagesOp.isEmpty()) {
                    try {
                        exchange.sendResponseHeaders(200, -1);
                    } catch (IOException e) {
                        logger.log(Level.WARNING, "Error sending headers", e);
                    }
                }
                return true;
            } catch (DbSearchException e) {
                sendErrorResponse(exchange, 500, "Image not found");
            } catch (IllegalArgumentException e) {
                sendErrorResponse(exchange, 400, "Invalid username or password");
            } finally {
                exchange.close();
            }
        }
        return false;
    }

    private static Boolean save(String method, HttpExchange exchange) {
        if (exchange.getRequestBody() != null && method.equals(POST) && exchange.getRequestHeaders().get(CONTENT_TYPE).contains(CONTENT_TYPE_JSON)) {
            try {

                final String decryptedMail = checkLoginToken(exchange, userDB.getUserCollection(USER_COLLECTION_NAME));
                final ImageService imageService = ImageService.getInstance();
                final ImageUploadData imageData = readJSON(exchange, ImageUploadData.class);

                if (imageData == null || imageData.filename() == null || imageData.metadata().containsKey("mail")) {
                    sendErrorResponse(exchange, 400, "Invalid payload: filename, byteArray and metadata.mail are required");
                    return false;
                }

                final boolean result = imageService.saveImage(imageData, decryptedMail);

                if (result) {
                    exchange.sendResponseHeaders(200, -1);
                    return true;
                } else {
                    sendErrorResponse(exchange, 500, "Error processing save request");
                }
            } catch (UserNotFoundException e) {
                sendErrorResponse(exchange, 400, "User not found");
            } catch (IOException e) {
                logger.log(Level.WARNING, "IOException in save", e);
                sendErrorResponse(exchange, 500, "Internal server error");
            } finally {
                exchange.close();
            }
        }
        return false;
    }

    private static Boolean findImageById(String method, HttpExchange exchange) {
        if (method.equals(GET)) {
            try {
                if (checkLoginToken(exchange, userDB.getUserCollection(USER_COLLECTION_NAME)) == null) {
                    return false;
                }
                final ImageService imageService = ImageService.getInstance();

                final Map<String, String> queryMap = getQueryToMap(exchange.getRequestURI().getQuery());

                if (!queryMap.containsKey("id") || !queryMap.containsKey("tag")) {
                    sendErrorResponse(exchange, 400, "Invalid payload: id and tag are required");
                    return false;
                }

                final ObjectId id = new ObjectId(queryMap.get("id"));
                final Optional<ImageWithIDData> foundImageOpt = imageService.findImageWithIdAndTag(id, ImageTag.valueOf(queryMap.get("tag")));

                if (foundImageOpt.isPresent()) {
                    final Gson gson = new GsonBuilder().create();
                    final String json = gson.toJson(foundImageOpt.get());
                    byte[] bytes = json.getBytes(StandardCharsets.UTF_8);

                    exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
                    exchange.sendResponseHeaders(200, bytes.length);

                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(bytes);
                    }
                    String timestamp = new Date().toString();
                    logger.log(Level.INFO, "sent image timestamp: " + timestamp);
                    return true;
                } else {
                    sendErrorResponse(exchange, 404, "Image not found");
                    return false;
                }
            } catch (DbSearchException e) {
                sendErrorResponse(exchange, 500, "Image not found");
            } catch (IllegalArgumentException e) {
                sendErrorResponse(exchange, 400, "Invalid username or password");
            } catch (IOException e) {
                sendErrorResponse(exchange, 500, "Internal server error");
            } finally {
                exchange.close();
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
                return didDelete;
            } catch (IOException e) {
                sendErrorResponse(exchange, 500, "Error deleting image");
            } finally {
                exchange.close();
            }
        }
        return false;
    }
}
