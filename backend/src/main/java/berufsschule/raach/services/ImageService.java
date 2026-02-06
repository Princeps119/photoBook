package berufsschule.raach.services;

import berufsschule.raach.data.ImageSummaryData;
import berufsschule.raach.data.ImageTag;
import berufsschule.raach.data.ImageUploadData;
import berufsschule.raach.data.ImageWithIDData;
import berufsschule.raach.exeptions.DBSaveException;
import berufsschule.raach.exeptions.DbSearchException;
import berufsschule.raach.repo.MongoRepo;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;
import com.mongodb.client.gridfs.GridFSFindIterable;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.mongodb.client.gridfs.model.GridFSUploadOptions;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.sun.net.httpserver.HttpExchange;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import static berufsschule.raach.services.Util.checkLoginToken;

public class ImageService {

    private static final Logger logger = Logger.getLogger(ImageService.class.getName());
    private static final MongoDatabase USER_DB = MongoRepo.getInstance().getUserDB();
    private static final MongoDatabase IMAGE_DB = MongoRepo.getInstance().getImageDB();
    private static final GridFSBucket BUCKET = GridFSBuckets.create(IMAGE_DB, "userImages");

    private static ImageService instance;
    private static final String USER_COLLECTION_NAME = "users";
    private static final MongoRepo userDB = MongoRepo.getInstance();

    private final MongoCollection<Document> userCollection;

    private ImageService() {
        this.userCollection = userDB.getUserCollection(USER_COLLECTION_NAME);
    }

    public static synchronized ImageService getInstance() {
        if (instance == null) {
            instance = new ImageService();
        }
        return instance;
    }

    public boolean saveImage(ImageUploadData uploadData, final String decryptedMail) {

        GridFSUploadOptions options = new GridFSUploadOptions().metadata(uploadData.metadata());

        // Convert bytes to InputStream
        try (InputStream is = new java.io.ByteArrayInputStream(uploadData.byteArray())) {
            ObjectId id = BUCKET.uploadFromStream(uploadData.filename(), is, options);

            USER_DB.getCollection("users").updateOne(
                    Filters.eq("mail", decryptedMail),
                    Updates.push("imageIds", id)
            );

            return true;

        } catch (IOException e) {
            logger.log(Level.WARNING, "Error while saving image " + uploadData.filename(), e);
            throw new DBSaveException("Error while saving image " + uploadData.filename(), e);
        }
    }

    //needs to be checked before call if Object Id belongs to user
    public Optional<ImageWithIDData> findImageWithIdAndTag(ObjectId id, ImageTag tag) throws DbSearchException {

        final Optional<GridFSFile> imageFileOpt = getImageWithIdAndTag(id, tag);
        if (imageFileOpt.isPresent()) {
            if (id == null || tag == null) {
                return Optional.empty();
            }

            final GridFSFile file = imageFileOpt.get();

            return Optional.of(buildImageWithData(file));

        } else throw new DbSearchException("could not find an Image");
    }

    public boolean deleteById(HttpExchange exchange) throws DbSearchException {
        final String path = exchange.getRequestURI().getPath();

        final String[] segments = path.split("/");
        final ObjectId id = new ObjectId(segments[segments.length - 1]);

        if (!checkUserAuthForImageId(exchange, id)) {
            logger.log(Level.WARNING, "User is not authorized for that id");
            return false;
        }

        final Optional<GridFSFile> imageFileOpt = getImageWithId(id);
        if (imageFileOpt.isPresent()) {
            BUCKET.delete(id);
            return true;
        }
        return false;
    }

    public List<ImageWithIDData> findAllImages() {
        GridFSFindIterable listAllImages = getAllImages();

        return getImageWithDataList(listAllImages);
    }

    public Optional<List<ImageSummaryData>> getAllImageSummariesForUser(HttpExchange exc) {
        final List<ObjectId> imageIDs = checkUserAuthAndGetImageIds(exc);
        if (imageIDs == null || imageIDs.isEmpty()) return Optional.empty();

        final GridFSFindIterable files = BUCKET.find(Filters.in("_id", imageIDs));
        List<ImageSummaryData> result = new ArrayList<>();
        for (GridFSFile file : files) {
            result.add(new ImageSummaryData(
                    file.getObjectId().toHexString(),
                    file.getFilename(),
                    file.getMetadata()
            ));
        }
        return Optional.of(result);
    }

    private List<ImageWithIDData> getImageWithDataList(GridFSFindIterable list) {
        final List<ImageWithIDData> result = new ArrayList<>();

        for (GridFSFile file : list) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            BUCKET.downloadToStream(file.getObjectId(), out);

            ImageWithIDData entry = new ImageWithIDData(
                    file.getObjectId().toHexString(),
                    file.getFilename(),
                    file.getMetadata(),
                    out.toByteArray());

            result.add(entry);
        }
        return result;
    }

    private Optional<GridFSFile> getImageWithIdAndTag(ObjectId id, ImageTag tag) {
        if (id == null || tag == null) {
            return Optional.empty();
        }

        GridFSFile imageFile = null;

        if (tag.equals(ImageTag.Private)) {
            imageFile = BUCKET.find().filter(Filters.eq("imageId", id)).filter(Filters.eq("tag", tag)).first();

        } else if (tag.equals(ImageTag.Public)) {
            imageFile = BUCKET.find().filter(Filters.eq("imageId", id)).filter(Filters.eq("tag", tag)).first();

        }

        if (imageFile == null) {
            return Optional.empty();
        }
        return Optional.of(imageFile);
    }

    private Optional<GridFSFile> getImageWithId(ObjectId id) {
        return Optional.ofNullable(BUCKET.find().filter(Filters.eq("imageId", id)).first());
    }

    private ImageWithIDData buildImageWithData(GridFSFile file) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        BUCKET.downloadToStream(file.getObjectId(), out);

        return new ImageWithIDData(
                file.getObjectId().toHexString(),
                file.getFilename(),
                file.getMetadata(), // Document
                out.toByteArray()
        );
    }

    //needs to be checked before call if Object Id belongs to user
    private GridFSFindIterable getAllImages() {
        return BUCKET.find();
    }

    private boolean checkUserAuthForImageId(HttpExchange exchange, ObjectId id) {
        final Document userDocument = getUser(exchange);

        if (userDocument != null && userDocument.containsKey("imageIds")) {
            List<ObjectId> idList = userDocument.getList("imageIds", ObjectId.class);
            return idList != null && idList.contains(id);
        }
        return false;
    }

    private Document  getUser(HttpExchange exchange) {
        final String decryptedMail = checkLoginToken(exchange, userCollection);
        return userCollection.find(Filters.eq("mail", decryptedMail)).first();
    }
    
    private List<ObjectId> checkUserAuthAndGetImageIds(HttpExchange exchange) {
        final Document userDocument = getUser(exchange);

        if (userDocument != null && userDocument.containsKey("imageIds")) {
            return userDocument.getList("imageIds", ObjectId.class);
        }
        return null;
    }
}
