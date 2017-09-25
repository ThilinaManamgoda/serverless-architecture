package api.user;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import transport.http.server.RestLogic;
import user.UserCallImplement;
import user.request.JsonHelper;
import user.request.models.CreateFunctionModal;


public class CreateFunction extends RestLogic {
    @Override
    public FullHttpResponse process(FullHttpRequest fullHttpRequest){
        boolean status = false;

        ByteBuf buf = fullHttpRequest.content();
        String content = buf.toString(CharsetUtil.UTF_8);
        CreateFunctionModal modal = new JsonHelper<CreateFunctionModal>(CreateFunctionModal.class).jsonToObj(content);

        /**
         *  call create function in UserCall interface
         *  @param 1 - domain name should pass in json object in content
         *  @param 2 - file path in json
         *  @param 3 - language in json
         *  @param 4 - user name in headers
         */

        UserCallImplement call = new UserCallImplement();
        status = call.createFunction(
                modal.getDomainName(),
                modal.getFilePath(),
                modal.getLang(),
                fullHttpRequest.headers().get("user")
        );


        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
                status? HttpResponseStatus.OK:HttpResponseStatus.EXPECTATION_FAILED, null);
        response.headers().set(HttpHeaders.Names.CONTENT_TYPE, "application/json");
        response.headers().set("user", fullHttpRequest.headers().get("user"));
        response.headers().set("domain",modal.getDomainName());


        return response;
    }
}
