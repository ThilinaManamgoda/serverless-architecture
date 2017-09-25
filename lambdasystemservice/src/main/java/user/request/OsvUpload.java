package user.request;

import core.utilities.HttpUploadClient;
import db.modal.FunctionType;
import org.apache.log4j.Logger;
import user.UserCallImplement;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * Created by deshan on 9/26/17.
 */



public class OsvUpload extends HttpUploadClient {

    static Logger log = Logger.getLogger(OsvUpload.class.getName());
    public OsvUpload(String baseUrl) {
        super(baseUrl);
    }

    public boolean uploadFile(String filename, List<Map.Entry<String, String>> attributes){
        File file = new File(filename);
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
}
