package berufsschule.raach.services;

import berufsschule.raach.repo.MongoRepo;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;

public class ImageService {

    private static final Logger logger = Logger.getLogger(ImageService.class.getName());
    private static final String USER_COLLECTION_NAME = "users";
    private static final String IMAGES_COLLECTION_NAME = "images";
    private static final MongoDatabase IMAGE_DB = MongoRepo.getInstance().getImageDB();
    private static final MongoRepo DB = MongoRepo.getInstance();


    private static ImageService instance;
    private final MongoCollection<Document> userCollection;
    private final MongoCollection<Document> imagesCollection;

    private ImageService() {
        this.userCollection = DB.getUserCollection(USER_COLLECTION_NAME);
        this.imagesCollection = DB.getImagesCollection(IMAGES_COLLECTION_NAME);
    }

    public static synchronized ImageService getInstance() {
        if (instance == null) {
            instance = new ImageService();
        }
        return instance;
    }


    public ObjectId saveImage(InputStream imageStream, String filename, String userMail) {

        MongoDatabase imageDB = DB.getImageDB();
        MongoDatabase userDB = DB.getUserDB();
        GridFSBucket bucket = GridFSBuckets.create(imageDB, "userImages");

        ObjectId id =  bucket.uploadFromStream(filename, imageStream);


        userDB.getCollection("users").updateOne(
                Filters.eq("usermail", userMail),
                Updates.push("imageIds", id)
        );


        return id;
    }
}
