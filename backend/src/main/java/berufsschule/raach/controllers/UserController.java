package berufsschule.raach.controllers;

import berufsschule.raach.data.LoginData;
import berufsschule.raach.data.RegisterData;
import berufsschule.raach.data.TokenData;
import berufsschule.raach.exeptions.EncryptionException;
import berufsschule.raach.services.DeletionService;
import berufsschule.raach.services.LoginService;
import berufsschule.raach.services.RegistrationService;
import berufsschule.raach.services.Util;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.io.OutputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import static berufsschule.raach.services.Util.*;

/**
 * The user controller class.
 * Handles all incoming requests for user-related operations.
 * User login, logout, registration, and deletion.
 */
public class UserController {

    public static final Logger logger = Logger.getLogger(UserController.class.getName());
    private static final String API_PREFIX = "/api/";

    private static final ArrayList<String> mapping = new ArrayList<>(Arrays.asList(API_PREFIX + "login", API_PREFIX + "checkBackend",
            API_PREFIX + "register", API_PREFIX + "delete", API_PREFIX + "logout"));

    static Optional<Boolean> checkMapping(final String path, final String method, final HttpExchange exchange) throws IllegalArgumentException, IOException {

        final String checkedPath = checkPath(path, exchange);

        if (mapping.contains(checkedPath)) {
            logger.log(Level.INFO, "Mapping found for {0}", path);
            final int mappedPath = mapping.indexOf(path);
            switch (mappedPath) {
                case 0:
                    return Optional.of(login(method, exchange));
                case 1:
                    return Optional.of(checkBackend(method, exchange));
                case 2:
                    return Optional.of(register(method, exchange));
                case 3:
                    return Optional.of(delete(method, exchange));
                case 4:
                    return Optional.of(logout(method, exchange));
            }
        }
        throw new IllegalArgumentException("Invalid path");
    }

    private static boolean logout(String method, HttpExchange exchange) {
        try {
            if (method.equals(POST)) {
                final TokenData loginToken = readJSON(exchange, TokenData.class);
                final LoginService loginService = LoginService.getInstance();
                final boolean result = loginService.logout(loginToken);
                if (result) {
                    exchange.sendResponseHeaders(200, -1);
                    return true;
                } else {
                    sendErrorResponse(exchange, 500, "Error processing logout request");
                }
            }
        } catch (EncryptionException | IOException e) {
            logger.log(Level.WARNING, "Error processing logout request", e);
            sendErrorResponse(exchange, 500, "Error processing logout request");
        } finally {
            exchange.close();
        }

        return false;
    }

    private static Boolean delete(final String method, final HttpExchange exchange) {

        if (method.equals(DELETE)) {
            try {
                final DeletionService deletionService = DeletionService.getInstance();
                final boolean didDelete = deletionService.deleteUser(exchange);
                exchange.sendResponseHeaders(204, -1);
                return didDelete;
            } catch (IOException e) {
                sendErrorResponse(exchange, 500, "Error deleting user");

            } finally {
                exchange.close();
            }
        }
        return false;
    }

    private static Boolean register(final String method, final HttpExchange exchange) throws IllegalArgumentException {
        if (method.equals(POST) && exchange.getRequestHeaders().get(CONTENT_TYPE).contains(CONTENT_TYPE_JSON)) {
            if (exchange.getRequestBody() != null) {
                try {
                    final RegisterData registerData = readJSON(exchange, RegisterData.class);
                    final RegistrationService registrationService = RegistrationService.getInstance();

                    if (registrationService.register(registerData)) {
                        exchange.sendResponseHeaders(204, -1);
                        return true;
                    }

                } catch (IllegalArgumentException e) {
                    logger.log(Level.WARNING, "illegal argument", e);
                    sendErrorResponse(exchange, 400, e.getMessage());

                } catch (JsonSyntaxException e) {
                    logger.log(Level.WARNING, "JsonSyntaxException", e);
                    sendErrorResponse(exchange, 400, "Invalid JSON format");

                } catch (Exception e) {
                    logger.log(Level.WARNING, "Exception", e);
                    sendErrorResponse(exchange, 500, "Internal server error");
                } finally {
                    exchange.close();
                }
            } else {
                sendErrorResponse(exchange, 400, "Request body is required");
            }
        }
        logger.log(Level.WARNING, "no exception, and no success either");
        return false;
    }

    private static boolean login(final String method, final HttpExchange exchange) {

        if (!validateInputs(method, exchange)) {
            return false;
        }

        try {

            // Authenticate user
            final LoginService loginService = LoginService.getInstance();
            TokenData token;

            final LoginData loginData = loginService.getMailAndPassword(exchange);

            try {
                token = loginService.checkLoginData(loginData.mail(), loginData.password());
            } catch (NoSuchPaddingException | IllegalBlockSizeException |
                     NoSuchAlgorithmException | BadPaddingException |
                     InvalidKeyException e) {
                logger.log(Level.WARNING, "Encryption error during login", e);
                sendErrorResponse(exchange, 500, "Authentication service error");
                return false;
            }

            if (token == null) {
                sendErrorResponse(exchange, 401, "Invalid email or password");
                return false;
            }

            // Success - send token
            final byte[] responseBytes = Util.createByteArray(token);

            exchange.getResponseHeaders().set(CONTENT_TYPE, "application/json");
            exchange.sendResponseHeaders(200, responseBytes.length);

            try (OutputStream os = exchange.getResponseBody()) {
                os.write(responseBytes);
            }

            logger.log(Level.INFO, "Login successful for: {0}", loginData.mail());
            return true;

        } catch (JsonSyntaxException e) {
            logger.log(Level.WARNING, "Invalid JSON in login request", e);
            sendErrorResponse(exchange, 400, "Invalid JSON format");
            return false;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Unexpected error during login", e);
            sendErrorResponse(exchange, 500, "Internal server error");
            return false;
        } finally {
            exchange.close();
        }
    }

    private static Boolean checkBackend(final String method, final HttpExchange exchange) throws IllegalArgumentException {

        if (method.equals(GET)) {
            try {
                exchange.sendResponseHeaders(200, -1);
                exchange.close();
                return true;
            } catch (IOException e) {
                logger.log(Level.WARNING, "invalid request", e);
            }

        } else {
            throw new IllegalArgumentException("Invalid method");
        }
        return false;
    }
}

