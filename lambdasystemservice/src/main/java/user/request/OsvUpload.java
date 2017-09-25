package user.request;

import core.utilities.HttpUploadClient;
import object_storage.ObjectStorageImpl;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

/**
 * Created by deshan on 9/26/17.
 */



public class OsvUpload extends HttpUploadClient {
    String FILE_STORAGE = "~/data";
    String user;
    static Logger log = Logger.getLogger(OsvUpload.class.getName());
    public OsvUpload(String baseUrl, String user) {
        super(baseUrl);
        this.user = user;
    }

    public boolean uploadFile(String filename, List<Map.Entry<String, String>> attributes){


        /**
         * get file from object storage and upload
         *
         */
        String fileStored = this.getFile(user,filename);
        File file = new File(fileStored);





        boolean result = false;
        if (!file.isFile()) {
            log.error("The file path or name is incorrect");
            return false;
        }

        try {
            result = uploadFileAsMultiPart(file,attributes);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;

    }

    /**
     *
     * @param bucket - the bucket is named by the user
     * @param file - the path of the file + name in object storage
     * @return the path of the file where stored
     */
    private String getFile(String bucket, String file){
        ObjectStorageImpl objImpl = ObjectStorageImpl.getInstance();
        InputStream obj = objImpl.getObj(bucket,file);
        File filepath = new File(FILE_STORAGE+"file");
        this.copyInputStreamToFile(obj,filepath);

        return FILE_STORAGE+"file";
    }

    private void copyInputStreamToFile( InputStream in, File file ) {
        try {
            OutputStream out = new FileOutputStream(file);
            byte[] buf = new byte[1024];
            int len;
            while((len=in.read(buf))>0){
                out.write(buf,0,len);
            }
            out.close();
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
