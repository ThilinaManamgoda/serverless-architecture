package api.user;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import transport.http.server.RestLogic;
import user.UserCallImplement;
import user.request.JsonHelper;
import user.request.models.UpdateFunctionModal;

/**
 * Created by deshan on 9/24/17.
 */
public class UpdateFunction extends RestLogic{
    @Override
    public FullHttpResponse process(FullHttpRequest fullHttpRequest) {
        boolean status = false;

        ByteBuf buf = fullHttpRequest.content();
        String content = buf.toString(CharsetUtil.UTF_8);
        UpdateFunctionModal modal = new JsonHelper<UpdateFunctionModal>(UpdateFunctionModal.class).jsonToObj(content);

        /**
         * update an existing function
         * @param 1 - domain name in json
         * @param 2 - file location in json
         */
        UserCallImplement call = new UserCallImplement();
        status = call.updateFunction(
                modal.getDomainName(),
                modal.getFileName()
        );


        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
                status ? HttpResponseStatus.OK : HttpResponseStatus.EXPECTATION_FAILED, null);
        response.headers().set(HttpHeaders.Names.CONTENT_TYPE, "application/json");
        response.headers().set("user", fullHttpRequest.headers().get("user"));
        response.headers().set("domain", modal.getDomainName());


        return response;
    }
}
