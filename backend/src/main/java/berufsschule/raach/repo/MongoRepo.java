package berufsschule.raach.repo;

import berufsschule.raach.data.TokenData;
import berufsschule.raach.services.TokenEncrypter;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexOptions;
import org.bson.Document;
import org.bson.types.ObjectId;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MongoRepo {

    private static final Path SECRET_PATH = Paths.get("/run/secrets/MONGO_URI_FILE");// von docker compose environment

    private static MongoRepo instance;
    private final MongoDatabase userDB;
    private final MongoDatabase imageDB;

    private MongoRepo() {
        MongoDatabase userDatabase = null;
        MongoDatabase imageDatabase = null;
        MongoClient client;
        try {
            String uri = null;
            if (Files.exists(SECRET_PATH)) {
                uri = Files.readString(SECRET_PATH).trim();
            }

            if (uri == null || uri.isBlank()) {
                System.err.println("MONGO_URI_FILE is not set or empty! Falling back to uri.txt");
                Path localUriPath = Paths.get("backend/src/main/resources/uri.txt");
                if (Files.exists(localUriPath)) {
                    uri = Files.readString(localUriPath).trim();
                }
            }

            if (uri == null || uri.isBlank()) {
                System.err.println("Could not find a valid MongoDB URI.");
            } else {
                client = MongoClients.create(uri);
                userDatabase = client.getDatabase("photobook_users");
                imageDatabase = client.getDatabase("photobook_images");
            }
        } catch (IOException e) {
            System.err.println("Could not read MONGO_URI_FILE: " + e.getMessage());
        }

        if (userDatabase == null) {
            System.err.println("Database not initialized.");
        }
        userDB = userDatabase;
        imageDB = imageDatabase;
    }

    public static synchronized MongoRepo getInstance() {
        if (instance == null) {
            instance = new MongoRepo();
        }
        return instance;
    }

    public MongoDatabase getImageDB() {
        if (imageDB == null) {
            System.err.println("imageDB not initialized.");
            return null;
        }
        boolean exists = imageDB.listCollectionNames()
                .into(new ArrayList<>())
                .contains("images");

        if (!exists) {
            imageDB.createCollection("images");
            createExampleUser();
        }
        return imageDB;
    }

    public MongoDatabase getUserDB() {
        if (userDB == null) {
            System.err.println("userDB not initialized.");
            return null;
        }
        boolean exists = userDB.listCollectionNames()
                .into(new ArrayList<>())
                .contains("users");

        //cannot check for null for that collection as is created lazily and will always return an obj for userDB.getCollection("users")
        if (!exists) {
            userDB.createCollection("users");
            userDB.getCollection("users").createIndex(new Document("mail", 1), new IndexOptions().unique(true));

            createExampleUser();
        }
        if (userDB.getCollection("users").countDocuments() == 0) {
            createExampleUser();
        }
        return userDB;
    }

    private void createExampleUser() {

        try {
            String username = "exampleUser";
            String email = "example@mail.com";
            String password = "mySecretPassword";


            TokenData tokenData = new TokenData(username, TokenEncrypter.encrypt(email), Instant.now().toString(), UUID.randomUUID().toString());
            MongoCollection<Document> users = userDB.getCollection("users");

            // Hash password (SHA-256)
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());
            String hashedPassword = Base64.getEncoder().encodeToString(hash);

            // Create document
            Document userDoc = new Document("username", username)
                    .append("mail", email)
                    .append("hashedPassword", hashedPassword)
                    .append("LoginToken", tokenData)
                    .append("image_ids", new ArrayList<ObjectId>());

            // Insert into collection
            users.insertOne(userDoc);
        } catch (NoSuchAlgorithmException e) {
            Logger.getLogger(MongoRepo.class.getName()).log(Level.SEVERE, "could not create example user", e);
        } catch (NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException | InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }

    public MongoCollection<Document> getUserCollection(final String collectionName) {
        return getUserDB().getCollection(collectionName);
    }
    public MongoCollection<Document> getImagesCollection(final String collectionName) {
        return getImageDB().getCollection(collectionName);
    }

}

