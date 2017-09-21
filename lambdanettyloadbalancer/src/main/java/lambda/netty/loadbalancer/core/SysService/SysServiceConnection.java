package lambda.netty.loadbalancer.core.SysService;

public class SysServiceConnection {

    private String host;
    private int port;

    public SysServiceConnection(String host, int port){

        this.host = host;
        this.port = port;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
