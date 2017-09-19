package lambda.netty.loadbalancer.core.launch;

import lambda.netty.loadbalancer.core.ConfigConstants;
import lambda.netty.loadbalancer.core.Server;
import lambda.netty.loadbalancer.core.scalability.ScalabilityManager;
import org.apache.commons.configuration2.XMLConfiguration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Launcher {
    private static final Logger logger = Logger.getLogger(Launcher.class);
    private final static String CONFIG_PROPERTIES_FILE = "config.xml";
    static {
        Configurations configs = new Configurations();
        try {
            xmlConfiguration = configs.xml(CONFIG_PROPERTIES_FILE);
        } catch (ConfigurationException e) {
            logger.error("Cannot read configurations !", e);
        }
    }


    // start implementing after the static block. it's loading the configuration
    private  static ExecutorService service = Executors.newFixedThreadPool(Launcher.getIntValue(ConfigConstants.LAUNCHER_THREADS));
    public final static boolean SCALABILITY_ENABLED=Launcher.getBoolean(ConfigConstants.CONFIG_SCALABILITY_ENABLED);

    private static XMLConfiguration xmlConfiguration;



    public static String getString(String tag) {
        return xmlConfiguration.getString(tag);
    }

    public static int getIntValue(String tag) {

        return xmlConfiguration.getInt(tag);
    }

    public static List<String> getStringList(String key) {
        Object obj = xmlConfiguration.getProperty(key);
        if (obj instanceof List) {
            return (List) obj;
        }
        return null;
    }

    public static List<Integer> getIntList(String key) {
        Object obj = xmlConfiguration.getProperty(key);

        if (obj instanceof List) {
            List tmp = (List) obj;
            List<Integer> tmp_return = new ArrayList<>(tmp.size());
            tmp.forEach(x->tmp_return.add(Integer.parseInt((String) x)));

            return tmp_return;
        }
        return null;
    }

    public static boolean getBoolean(String key){
        String val = getString(key);

        return val.equals("true")? true:false;
    }
    public static long getLong(String s) {

        return xmlConfiguration.getLong(s);
    }
    public static void main(String[] args) throws InterruptedException {

//        State  state = new StateImpl();
//        state.pushHost("127.0.0.1:8082");
//        state.setState(InstanceStates.DOWN);
//        state.setDomain("maanadev.org");
//
//        System.out.println(StateImplJsonHelp.toString(state));
//        try {
////            EtcdUtil.putValue("localhost",StateImplJsonHelp.toString(state)).get();
//            System.out.println(EtcdUtil.getValue("localhost").get().getKvs().get(0).getValue().toString(StandardCharsets.UTF_8));
//        } catch (EtcdClientException e) {
//            e.printStackTrace();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        } catch (ExecutionException e) {
//            e.printStackTrace();
//        }
        if(SCALABILITY_ENABLED){
            service.submit(new ScalabilityManager());
        }else {
            logger.info("Scalability is not enabled !");
        }
        service.submit(new Server());

    }


}