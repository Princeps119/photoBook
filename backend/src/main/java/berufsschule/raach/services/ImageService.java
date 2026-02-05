package berufsschule.raach.services;

import berufsschule.raach.data.ImageTag;
import berufsschule.raach.data.ImageWithData;
import berufsschule.raach.exeptions.DbSearchException;
import berufsschule.raach.repo.MongoRepo;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;
import com.mongodb.client.gridfs.GridFSFindIterable;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.mongodb.client.gridfs.model.GridFSUploadOptions;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

public class ImageService {

    private static final Logger logger = Logger.getLogger(ImageService.class.getName());
    private static final MongoDatabase USER_DB = MongoRepo.getInstance().getUserDB();
    private static final MongoDatabase IMAGE_DB = MongoRepo.getInstance().getImageDB();
    private static final GridFSBucket BUCKET = GridFSBuckets.create(IMAGE_DB, "userImages");

    private static ImageService instance;

    private ImageService() {
    }

    public static synchronized ImageService getInstance() {
        if (instance == null) {
            instance = new ImageService();
        }
        return instance;
    }

    public ObjectId saveImage(InputStream imageStream, String filename, String userMail) {

        final GridFSUploadOptions options = new GridFSUploadOptions()
                .metadata(new Document("name", filename));

        final ObjectId id = BUCKET.uploadFromStream(filename, imageStream, options);


        USER_DB.getCollection("users").updateOne(
                Filters.eq("mail", userMail),
                Updates.push("imageIds", id)
        );

        return id;
    }

    //needs to be checked before call if Object Id belongs to user
    public Optional<ImageWithData> findImageWithIdAndTag(ObjectId id, ImageTag tag) throws DbSearchException {

        final Optional<GridFSFile> imageFileOpt = getImageWithIdAndTag(id, tag);
        if (imageFileOpt.isPresent()) {
            if (id == null || tag == null) {
                return Optional.empty();
            }

            Optional<GridFSFile> fileOpt = getImageWithIdAndTag(id, tag);
            if (fileOpt.isEmpty()) return Optional.empty();

            GridFSFile file = fileOpt.get();

            return Optional.of(buildImageWithData(file));

        } else throw new DbSearchException("could not find an Image");
    }

    public List<ImageWithData> findAllImages() {
        GridFSFindIterable listAllImages = getAllImages();

        return getImageWithDataList(listAllImages);
    }

    private ImageWithData buildImageWithData(GridFSFile file) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        BUCKET.downloadToStream(file.getObjectId(), out);

       return new ImageWithData(
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

    public Optional<List<ImageWithData>> getAllImagesForUser(List<ObjectId> imageIDs) {

        final List<ImageWithData> result = getImageWithDataList(BUCKET.find(Filters.in("_id", imageIDs)));
        return Optional.of(result);
    }

    private List<ImageWithData> getImageWithDataList(GridFSFindIterable list)  {
        final List<ImageWithData> result = new ArrayList<>();

        for (GridFSFile file : list) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            BUCKET.downloadToStream(file.getObjectId(), out);

            ImageWithData entry = new ImageWithData(
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
            imageFile = BUCKET.find().filter(Filters.eq("imageId", id)).first();

        }

        if (imageFile == null) {
            return Optional.empty();
        }
        return Optional.of(imageFile);
    }
}
