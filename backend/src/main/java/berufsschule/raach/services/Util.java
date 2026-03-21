package berufsschule.raach.services;


import berufsschule.raach.data.TokenData;
import berufsschule.raach.data.imageData.ImageSummaryData;
import berufsschule.raach.data.imageData.ImageWithIDData;
import berufsschule.raach.exeptions.UserNotFoundException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.sun.net.httpserver.HttpExchange;
import org.bson.Document;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class Util {

    public static final Logger logger = Logger.getLogger(Util.class.getName());

    public static final String POST = "POST";
    public static final String GET = "GET";
    public static final String DELETE = "DELETE";

    public static final String CONTENT_TYPE = "Content-Type";
    public static final String CONTENT_TYPE_JSON = "application/json";
    public static final String CHARSET = "charset=utf-8";
    public static final String CONTENT_TYPE_JSON_AND_CHARSET = CONTENT_TYPE_JSON + ";" + CHARSET;

    public static String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            logger.log(Level.SEVERE, "Error while encrypting password", e);
        }
        return null;
    }

    public static boolean validateInputs(String method, HttpExchange exchange) {
        // Validate request method first
        if (!POST.equals(method)) {
            sendErrorResponse(exchange, 405, "Method not allowed. Use POST.");
            return false;
        }

        // Validate Content-Type header
        List<String> contentTypes = exchange.getRequestHeaders().get(CONTENT_TYPE);
        if (contentTypes == null || !contentTypes.contains(CONTENT_TYPE_JSON)) {
            sendErrorResponse(exchange, 415, CONTENT_TYPE + " must be " + CONTENT_TYPE_JSON);
            return false;
        }

        // Validate request body exists
        if (exchange.getRequestBody() == null) {
            sendErrorResponse(exchange, 400, "Request body is required");
            return false;
        }
        return true;
    }

    public static void sendErrorResponse(final HttpExchange exchange,
                                         final int statusCode,
                                         final String errorMsg) {
        try (exchange) {

            if (exchange.getResponseCode() != -1) {
                // headers already sent → exit method
                logger.log(Level.WARNING, "Headers already sent");
                return;
            }

            // Create structured JSON error response following RFC 7807
            String jsonResponse = String.format(
                    "{\"type\": \"about:blank\", \"title\": \"Error\", \"status\": %d, \"detail\": \"%s\"}",
                    statusCode, errorMsg.replace("\"", "\\\""));

            byte[] responseBytes = jsonResponse.getBytes(StandardCharsets.UTF_8);

            // Set proper headers, including CORS so browsers accept the response, see Main, do not need to set them again here
            // or new cors as the original header will not be removed
            var headers = exchange.getResponseHeaders();
            if (headers.containsKey(CONTENT_TYPE)) {
                headers.get(CONTENT_TYPE).remove(CONTENT_TYPE_JSON);
            }
            headers.set(CONTENT_TYPE, "application/problem+json");
            exchange.sendResponseHeaders(statusCode, responseBytes.length);

            // Use try-with-resources to prevent leaks
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(responseBytes);
            }

            logger.log(Level.WARNING, "Sent error response: {0} - {1}",
                    new Object[]{statusCode, errorMsg});

        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to send error response", e);
        }
        finally {
            exchange.close();
        }
    }

    public static String checkLoginToken(HttpExchange exchange, MongoCollection<Document> userCollection) {
        //check timestamp and LoginToken, if null is returned an error happened
        final String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            logger.log(Level.WARNING, "Missing or invalid Authorization header");
            sendErrorResponse(exchange, 401, "Missing or invalid Authorization header");
            return null;
        }

        final String tokenJson = authHeader.substring("Bearer ".length()).trim();
        logger.log(Level.INFO, "Checking login token: {0}", tokenJson);

        TokenData tokenData;
        try {
            logger.log(Level.INFO, "Token in Backend check:  " + tokenJson);
            final JsonReader reader = new JsonReader(new StringReader(tokenJson));
            final Gson gson = new GsonBuilder().create();
            final Type tokenType = new TypeToken<TokenData>() {
            }.getType();

            tokenData = gson.fromJson(reader, tokenType);

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Exception", e);
            sendErrorResponse(exchange, 400, "Malformed token");
            return null;
        }

        String decryptedMail;
        String decryptedTimestamp;
        try {
            decryptedMail = TokenEncrypter.decrypt(tokenData.encryptedMail());
            decryptedTimestamp = TokenEncrypter.decrypt(tokenData.timestamp());
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Exception", e);
            sendErrorResponse(exchange, 400, "Invalid token encryption");
            return null;
        }

        // check timestamp and UUID
        try {
            final UUID version = UUID.fromString(tokenData.version());

            Optional<Document> userDocument = Optional.ofNullable(userCollection.find(Filters.eq("mail", decryptedMail)).first());
            if (userDocument.isPresent()) {
                Document loginToken = userDocument.get().get("LoginToken", Document.class);
                if (loginToken != null) {
                    final UUID savedVersion = UUID.fromString(loginToken.getString("version")); // default 0 if missing
                    if (savedVersion.equals(version)) {
                        Instant tokenTime = Instant.parse(decryptedTimestamp);
                        if (Duration.between(tokenTime, Instant.now()).toHours() > 4) {
                            sendErrorResponse(exchange, 401, "Token expired");
                            return null;
                        }
                    } else {
                        sendErrorResponse(exchange, 401, "wrong UUID");
                    }
                }
            } else  {
                throw new UserNotFoundException("User not found");
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Exception", e);
            sendErrorResponse(exchange, 400, "Invalid timestamp or LoginToken format");
            return null;
        }

        return decryptedMail;
    }

    public static <T> T readJSON(final HttpExchange exchange, Class<T> clazz) {
        final JsonReader reader = createJsonReader(exchange);
        final Gson gson = new GsonBuilder().create();
        return gson.fromJson(reader, clazz);
    }

    public static String checkPathImage(final String path, final HttpExchange exchange) {

        long slashCount = path.chars().filter(ch -> ch == '/').count();

        if (slashCount >= 3 && path.contains("image")) {
            return path.endsWith("/") ? path.substring(0, path.length() - 1) : path;
        } else {
            logger.log(Level.SEVERE, "Issue pathing: {0}", path);
            sendErrorResponse(exchange, 400, "Invalid JSON format");
            return null;
        }
    }

    public static String checkPath(final String path, final HttpExchange exchange) {

        long slashCount = path.chars().filter(ch -> ch == '/').count();

        if (slashCount == 3) {
            // Remove everything from the last slash onwards
            int lastSlash = path.lastIndexOf('/');
            return path.substring(0, lastSlash);
        } else if (slashCount == 2) {
            return path;
        } else if (slashCount == 1) {
            return path;
        } else {
            logger.log(Level.SEVERE, "Issue pathing: {0}", path);
            sendErrorResponse(exchange, 400, "Invalid JSON format");
            return null;
        }
    }

    public static JsonReader createJsonReader(HttpExchange exchange) {
        final String body = new BufferedReader(new InputStreamReader(exchange.getRequestBody()))
                .lines()
                .collect(Collectors.joining("\n"));

        logger.log(Level.INFO, "got RequestBody: {0}", logPartOfRequestBody(body));

        return new JsonReader(new StringReader(body));
    }

    private static String logPartOfRequestBody(String body) {
    //prevent the whole image string from being logged as it is waaay to big
        String keyword = "base64";
        int idx = body.indexOf(keyword);

        if (idx != -1) {
            return body.substring(0, idx + keyword.length());
        } else {
            return body;
        }
    }


    public static Map<String, String> getQueryToMap(String query) {
        Map<String, String> result = new HashMap<>();
        if (query == null) return result;

        for (String param : query.split("&")) {
            String[] pair = param.split("=");
            if (pair.length > 1) {
                result.put(pair[0], java.net.URLDecoder.decode(pair[1], StandardCharsets.UTF_8));
            } else {
                result.put(pair[0], "");
            }
        }
        return result;
    }

    public static  byte[] createByteArray(TokenData token){
        final Gson gson = new GsonBuilder().create();
        final String jsonResponse = gson.toJson(token);
        return jsonResponse.getBytes(StandardCharsets.UTF_8);
    }

    public static byte[] createByteArray(List<ImageSummaryData> images) {
        final Gson gson = new GsonBuilder().create();
        final String json = gson.toJson(images);
        return json.getBytes(StandardCharsets.UTF_8);
    }

    public static byte[] createByteArray(ImageWithIDData image) {
        final Gson gson = new GsonBuilder().create();
        final String json = gson.toJson(image);
        return json.getBytes(StandardCharsets.UTF_8);
    }
}
