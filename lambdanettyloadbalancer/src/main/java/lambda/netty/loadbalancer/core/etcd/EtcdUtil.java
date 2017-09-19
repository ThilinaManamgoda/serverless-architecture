package lambda.netty.loadbalancer.core.etcd;

import com.coreos.jetcd.Client;
import com.coreos.jetcd.KV;
import com.coreos.jetcd.Txn;
import com.coreos.jetcd.Watch;
import com.coreos.jetcd.data.ByteSequence;
import com.coreos.jetcd.kv.GetResponse;
import com.coreos.jetcd.kv.PutResponse;
import com.coreos.jetcd.kv.TxnResponse;
import com.coreos.jetcd.op.Cmp;
import com.coreos.jetcd.op.CmpTarget;
import com.coreos.jetcd.op.Op;
import com.coreos.jetcd.options.GetOption;
import com.coreos.jetcd.options.PutOption;
import com.coreos.jetcd.watch.WatchEvent;
import com.coreos.jetcd.watch.WatchResponse;
import lambda.netty.loadbalancer.core.ConfigConstants;
import lambda.netty.loadbalancer.core.launch.Launcher;
import org.apache.log4j.Logger;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class EtcdUtil {
    final static Logger logger = Logger.getLogger(EtcdUtil.class);
    public static String ETCD_CLUSTER = Launcher.getString(ConfigConstants.ETCD_CLUSTER_CONNECTIONS_URL);
    private static KV kvClient = null;
    private static Client client = null;
    private static GetOption getOption = GetOption.newBuilder().withSerializable(true).build();

    static {

        if (kvClient == null && client == null) {
            client = Client.builder().endpoints(ETCD_CLUSTER).build();
            kvClient = client.getKVClient();
        }
    }

    private EtcdUtil() {
    }

    public static CompletableFuture<PutResponse> putValue(String s_key, String s_value) throws EtcdClientException {
        ByteSequence key = ByteSequence.fromString(s_key);
        ByteSequence value = ByteSequence.fromString(s_value);
        CompletableFuture<com.coreos.jetcd.kv.PutResponse> responseCompletableFuture;
        if (kvClient == null) {
            throw new EtcdClientException();
        } else {
            responseCompletableFuture = kvClient.put(key, value);
        }
        return responseCompletableFuture;
    }

    public static CompletableFuture<GetResponse> getValue(String s_key) throws EtcdClientException {
        ByteSequence key = ByteSequence.fromString(s_key);
        CompletableFuture<GetResponse> rangeResponse = null;
        if (kvClient == null) {
            throw new EtcdClientException();
        } else {
            rangeResponse = kvClient.get(key, getOption);
            kvClient.close();
        }


        return rangeResponse;
    }

    public static boolean txnPutValue(String s_key, String s_value, long version) {
        ByteSequence key = ByteSequence.fromString(s_key);
        ByteSequence value = ByteSequence.fromString(s_value);

        Txn txn = kvClient.txn();
        Cmp cmp = new Cmp(key, Cmp.Op.GREATER, CmpTarget.version(version));
        TxnResponse txnResponse = null;
        try {
            txnResponse = txn.If(cmp).Then(Op.put(key, value, PutOption.DEFAULT)).commit().get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return txnResponse.isSucceeded();
    }

    public static void txngetValue(String s_key, long version) {
        ByteSequence key = ByteSequence.fromString(s_key);
        Txn txn = kvClient.txn();
        Cmp cmp = new Cmp(key, Cmp.Op.GREATER, CmpTarget.version(version));
        TxnResponse txnResponse = null;
        try {
            txnResponse = txn.If(cmp).Then(Op.get(key, GetOption.DEFAULT)).commit().get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

//        return txnResponse.getGetResponses().get(0).getKvs().get(0).getValue().toStringUtf8();
        System.out.println(txnResponse.toString());
        System.out.println(txnResponse.getPutResponses());
        System.out.println(txnResponse.getGetResponses());
        System.out.println(txnResponse);
//        System.out.println(txnResponse.getGetResponses());
    }

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        Watch watch = null;
        Watch.Watcher watcher = null;

        try {
            watch = client.getWatchClient();
            watcher = watch.watch(ByteSequence.fromString("aaa"));

            for (int i = 0; i < 5; i++) {
                WatchResponse response = watcher.listen();

                for (WatchEvent event : response.getEvents()) {

                    System.out.println(event.getEventType());
                    System.out.println(Optional.ofNullable(event.getKeyValue().getKey())
                            .map(ByteSequence::toStringUtf8)
                            .orElse(""));
                    System.out.println(Optional.ofNullable(event.getKeyValue().getValue())
                            .map(ByteSequence::toStringUtf8)
                            .orElse(""));

                }
            }
        } catch (Exception e) {
            if (watcher != null) {
                watcher.close();
            }

            if (watch != null) {
                watch.close();
            }

            if (client != null) {
                client.close();
            }
        }

    }


}
