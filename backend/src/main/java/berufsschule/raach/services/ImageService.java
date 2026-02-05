package berufsschule.raach.services;

import berufsschule.raach.data.ImageTag;
import berufsschule.raach.data.ImageWithMetaData;
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

    public boolean saveImage(ImageWithMetaData imageWithMetaData) {

        GridFSUploadOptions options = new GridFSUploadOptions().metadata(imageWithMetaData.metadata());

        // Convert bytes to InputStream
        try (InputStream is = new java.io.ByteArrayInputStream(imageWithMetaData.byteArray())) {
            ObjectId id = BUCKET.uploadFromStream(imageWithMetaData.filename(), is, options);


            USER_DB.getCollection("users").updateOne(
                    Filters.eq("mail", imageWithMetaData.metadata().get("mail")),
                    Updates.push("imageIds", id)
            );

            return true;

        } catch (IOException e) {
            logger.log(Level.WARNING, "Error while saving image " + imageWithMetaData.filename(), e);
            throw new DBSaveException("Error while saving image " + imageWithMetaData.filename(), e);
        }
    }

    //needs to be checked before call if Object Id belongs to user
    public Optional<ImageWithMetaData> findImageWithIdAndTag(ObjectId id, ImageTag tag) throws DbSearchException {

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

    public List<ImageWithMetaData> findAllImages() {
        GridFSFindIterable listAllImages = getAllImages();

        return getImageWithDataList(listAllImages);
    }

    public Optional<List<ImageWithMetaData>> getAllImagesForUser(List<ObjectId> imageIDs) {

        final List<ImageWithMetaData> result = getImageWithDataList(BUCKET.find(Filters.in("_id", imageIDs)));
        return Optional.of(result);
    }

    private List<ImageWithMetaData> getImageWithDataList(GridFSFindIterable list) {
        final List<ImageWithMetaData> result = new ArrayList<>();

        for (GridFSFile file : list) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            BUCKET.downloadToStream(file.getObjectId(), out);

            ImageWithMetaData entry = new ImageWithMetaData(
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

    private ImageWithMetaData buildImageWithData(GridFSFile file) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        BUCKET.downloadToStream(file.getObjectId(), out);

        return new ImageWithMetaData(
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
        final String decryptedMail = checkLoginToken(exchange, userCollection);
        final Document userDocument = userCollection.find(Filters.eq("mail", decryptedMail)).first();

        if (userDocument != null && userDocument.containsKey("image_ids")) {
            List<ObjectId> idList = userDocument.getList("imageIds", ObjectId.class);
            return idList.contains(id);
        }
        return false;
    }
}
