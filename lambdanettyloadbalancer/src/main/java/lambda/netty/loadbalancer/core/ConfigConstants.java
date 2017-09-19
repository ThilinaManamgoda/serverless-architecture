package lambda.netty.loadbalancer.core;

public class ConfigConstants {

    //transport
    public static final String TRANSPORT_SERVER_PORT = "transport.server.port";
    public static final String TRANSPORT_SERVER_BOSS_GROUP_THREAD_COUNT = "transport.server.bossGroupThreadCount";
    public static final String TRANSPORT_SSL_CONFIG_ENABLED = "transport.ssl-config.enabled";
    public static final String TRANSPORT_SERVER_HTTPOBJECTAGGREGATOR = "transport.server.httpobjectaggregator";
    //ssl
    public static final String TRANSPORT_SSL_CONFIG_PROTOCOL = "transport.ssl-config.protocol";
    public static final String TRANSPORT_SSL_CONFIG_KEYSTORE_FILE = "transport.ssl-config.keystore.file";
    public static final String TRANSPORT_SSL_CONFIG_KEYSTORE_TYPE = "transport.ssl-config.keystore.type";
    public static final String TRANSPORT_SSL_CONFIG_KEYSTORE_PASSWORD = "transport.ssl-config.keystore.password";
    public static final String TRANSPORT_SSL_CONFIG_CERT_PASSWORD = "transport.ssl-config.cert.password";

    //sys-service
    public static final String SYS_SERVICE_CONNECTIONS_CONNECTION_HOST = "sys-service.connections.connection.host";
    public static final String SYS_SERVICE_CONNECTIONS_CONNECTION_PORT = "sys-service.connections.connection.port";
    public static final String SYS_SERVICE_CONNECTIONS_PATH = "sys-service.connections.path";
    public static final String SYS_SERVICE_CONNECTIONS_PROTOCOL ="sys-service.connections.protocol" ;

    //etcd
    public static final String ETCD_CLUSTER_CONNECTIONS_URL = "etcd-cluster.connections.url";


    //scalability
    public static final String SCALABILITY_THRESHOLD = "scalability.response-time-threshold";
    public static final String SCALABILITY_MAP_SIZE = "scalability.map-size";
    public static final String SCALABILITY_BLOCKING_QUEUE_SIZE = "scalability.blocking_queue_size";
    public static final String SCALABILITY_THREAD_COUNT = "scalability.thread-count";
    public static final String SCALABILITY_QUERY_TIME = "scalability.query_time";
    public static final String LAUNCHER_THREADS = "launcher.threads";
    public static final String CONFIG_SCALABILITY_ENABLED = "scalability.enabled";
}
