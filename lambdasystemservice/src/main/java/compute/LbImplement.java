package compute;

import availability.UserPing;
import com.coreos.jetcd.kv.GetResponse;
import connections.OpenstackAdminConnection;
import lambda.netty.loadbalancer.core.etcd.EtcdClientException;
import lambda.netty.loadbalancer.core.loadbalance.StateImplJsonHelp;
import lambda.netty.loadbalancer.core.loadbalance.statemodels.InstanceStates;
import lambda.netty.loadbalancer.core.loadbalance.statemodels.OSVInstance;
import lambda.netty.loadbalancer.core.loadbalance.statemodels.State;
import org.openstack4j.api.OSClient;
import org.openstack4j.model.compute.Server;
import lambda.netty.loadbalancer.core.etcd.EtcdUtil;
import com.coreos.jetcd.kv.PutResponse;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * Created by deshan on 9/5/17.
 */
public class LbImplement implements LoadBalancerInteract {
    OSClient.OSClientV2 os;
    ServerLaunchImplement serverLaunch;
    UserPing ping;


    public LbImplement(){
        this.os = OpenstackAdminConnection.getOpenstackAdminConnection().getOSclient();
        this.serverLaunch = new ServerLaunchImplement(this.os);
        this.ping = new UserPing(this.os,this.os);


    }

    @Override
    public String startFunction(String domain,String instanceID) {
        String ipaddress = null;
        boolean test = false;
        int count=0;
        this.serverLaunch.startOSVinstance(instanceID);
        Server server = this.os.compute().servers().get(instanceID);
        ipaddress = server.getAccessIPv4();

        while (!test) {
            test = ping.pingHostByCommand(ipaddress);
            count++;
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if(count>1000) break;
        }

        if(test){
            //write to etcd
            try {
                this.updateEtcd(domain,InstanceStates.RUNNING,ipaddress);
            } catch (EtcdClientException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //notify lb
        }
        return ipaddress;

    }


    private void updateEtcd(String domainName, InstanceStates state, String ipaddress) throws EtcdClientException,ExecutionException,InterruptedException {
        CompletableFuture<GetResponse> res = EtcdUtil.getValue(domainName);
        GetResponse resjson = res.get();
        String val = String.valueOf(resjson.getKvs().get(0).getValue().toString(StandardCharsets.UTF_8));
        State stateImpl = StateImplJsonHelp.getObject(val);
        OSVInstance remoteHost = stateImpl.getOSVInstance().remove();

        // add data
        remoteHost.setIpaddress(ipaddress);
        stateImpl.pushOSVInstance(remoteHost);
        stateImpl.setState(state);

        //write back to etcd
        EtcdUtil.putValue(domainName,StateImplJsonHelp.toString(stateImpl));

    }
}
