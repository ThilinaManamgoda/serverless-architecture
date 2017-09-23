package compute;


import lambda.netty.loadbalancer.core.etcd.EtcdClientException;

/**
 * Created by deshan on 9/5/17.
 */
public interface LoadBalancerInteract {
    //return IP address
    String startFunction(String domain, String instanceID)throws EtcdClientException;
}
