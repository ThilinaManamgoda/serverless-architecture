package api.user;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import transport.http.server.RestLogic;
import user.UserCallImplement;
import user.request.JsonHelper;
import user.request.models.InvokeFunctionModal;

/**
 * Created by deshan on 9/24/17.
 */
public class InvokeFunction extends RestLogic{
    @Override
    public FullHttpResponse process(FullHttpRequest fullHttpRequest) {
        /**
         * check whether function invokes
         */
        boolean status = true;

        ByteBuf buf = fullHttpRequest.content();
        String content = buf.toString(CharsetUtil.UTF_8);
        InvokeFunctionModal modal = new JsonHelper<InvokeFunctionModal>(InvokeFunctionModal.class).jsonToObj(content);


        UserCallImplement call = new UserCallImplement();
        /**
         * invoke function in UserCall interface
         * @param 1 - domain name in json
         * @param 2 - blocking state in json
         * @param 3 - arguments in json
         */

        call.invokeFunction(
                modal.getDomainName(),
                modal.isBlocking(),
                modal.getArgs().toArray(new String[0])
        );


        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
                status ? HttpResponseStatus.OK : HttpResponseStatus.EXPECTATION_FAILED, null);
        response.headers().set(HttpHeaders.Names.CONTENT_TYPE, "application/json");
        response.headers().set("user", fullHttpRequest.headers().get("user"));
        response.headers().set("domain", modal.getDomainName());


        return response;
    }
}
