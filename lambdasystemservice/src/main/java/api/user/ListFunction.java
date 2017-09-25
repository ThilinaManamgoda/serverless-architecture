package api.user;

import com.google.gson.Gson;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import transport.http.server.RestLogic;
import user.UserCallImplement;

import java.util.ArrayList;

/**
 * Created by deshan on 9/23/17.
 */
public class ListFunction extends RestLogic{
    @Override
    public FullHttpResponse process(FullHttpRequest fullHttpRequest){
        UserCallImplement userFunctionData = new UserCallImplement();

        /**
         * list function by the user - call UserCall
         * @param 1 - user name in headers
         *
         */

        ArrayList<String> list = userFunctionData.listFunction(fullHttpRequest.headers().get("user"));
        //create json object from list
        Gson gson = new Gson();
        String json = gson.toJson(list);


        ByteBuf content = Unpooled.copiedBuffer(json, CharsetUtil.UTF_8);
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
                list!=null? HttpResponseStatus.OK:HttpResponseStatus.SERVICE_UNAVAILABLE, content);
        response.headers().set(HttpHeaders.Names.CONTENT_TYPE, "application/json");
        response.headers().set("domain", fullHttpRequest.headers().get("domain"));
        response.headers().set(HttpHeaders.Names.CONTENT_LENGTH, content.readableBytes());
        return response;
    }

}
