package api.user;

import com.coreos.jetcd.kv.GetResponse;
import io.netty.handler.codec.http.*;
import lambda.netty.loadbalancer.core.etcd.EtcdClientException;
import lambda.netty.loadbalancer.core.etcd.EtcdUtil;
import lambda.netty.loadbalancer.core.loadbalance.StateImplJsonHelp;
import lambda.netty.loadbalancer.core.loadbalance.statemodels.OSVInstance;
import lambda.netty.loadbalancer.core.loadbalance.statemodels.State;
import transport.http.server.RestLogic;
import user.UserCallImplement;


import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * Created by deshan on 9/24/17.
 */
public class DeleteFunction extends RestLogic{
    @Override
    public FullHttpResponse process(FullHttpRequest fullHttpRequest) {
        boolean status = false;


        try {
            // get the uuid of starting instance
            CompletableFuture<GetResponse> res = EtcdUtil.getValue(fullHttpRequest.headers().get("domain"));
            GetResponse resjson = res.get();
            String val = String.valueOf(resjson.getKvs().get(0).getValue().toString(StandardCharsets.UTF_8));
            State stateImpl = StateImplJsonHelp.getObject(val);
            OSVInstance remoteHost = stateImpl.getOSVInstance().peek();

            UserCallImplement call = new UserCallImplement();


            /**
             * delete the function
             * @param 1 - domain name should pass in header
             * @param 2 - user name should pass in header
             */
            status = call.deleteFunction(remoteHost.getUuid().toString(), fullHttpRequest.headers().get("user"));

        } catch (EtcdClientException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }


        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
                status ? HttpResponseStatus.OK : HttpResponseStatus.EXPECTATION_FAILED, null);
        response.headers().set(HttpHeaders.Names.CONTENT_TYPE, "application/json");
        response.headers().set("user", fullHttpRequest.headers().get("user"));


        return response;
    }
}
