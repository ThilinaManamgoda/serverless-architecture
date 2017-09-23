package lambda.netty.loadbalancer.core.loadbalance.statemodels;

import java.util.UUID;

public class OSVInstance {
    UUID uuid;
    String ipaddress;


    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public String getIpaddress() { return ipaddress; }

    public void setIpaddress(String ipaddress) { this.ipaddress = ipaddress;}
}
