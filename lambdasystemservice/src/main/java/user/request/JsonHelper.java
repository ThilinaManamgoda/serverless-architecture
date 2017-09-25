package user.request;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.log4j.Logger;

import java.io.IOException;


/**
 * Created by deshan on 9/24/17.
 */
public class JsonHelper<T> {
    final static Logger logger = Logger.getLogger(lambda.netty.loadbalancer.core.loadbalance.JsonHelper.class);
    private static ObjectMapper objectMapper = new ObjectMapper();
    private Class<T> t;

    public JsonHelper(Class<T> t) {

        this.t = t;
    }

    public String objToJson(Object obj) {
        String tmp = null;
        try {
            tmp = objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            logger.error("Cannot process the Object !", e);
        }
        return tmp;
    }

    public T jsonToObj(String str) {
        T tmp = null;
        try {
            tmp = objectMapper.readValue(str, t);
        } catch (IOException e) {
            logger.error("Cannot parse the String !", e);
        }
        return tmp;
    }


}
