package lambda.netty.loadbalancer.core.launch;

import lambda.netty.loadbalancer.core.ConfigConstants;
import org.apache.log4j.Logger;

import java.lang.reflect.Field;

public class ConfigLogger {
    private static final Logger logger = Logger.getLogger(ConfigLogger.class);

    final private static String LAUNCHER = "<========== Launcher Configurations ========> ";
    final private static String TRANSPORT = "<========== Transport Configurations ========> ";
    final private static String SYS_SERVICE = "<========== Sys-Service configurations ========> ";
    final private static String ETCD = "<========== ETCD configurations ========>";
    final private static String SCALABILITY = "<========== Scalability configurations ========> ";

    final private static String L_REGEX = "CONFIGLAUNCHER";
    final private static String T_REGEX = "CONFIGTRANSPORT";
    final private static String SY_REGEX = "CONFIGSYS";
    final private static String E_REGEX = "CONFIGETCD";
    final private static String SC_REGEX = "CONFIGSCALABILITY";

    public static void printFields() throws Exception {
        ConfigConstants configConstants = new ConfigConstants();
        Class<?> objClass = configConstants.getClass();


        boolean lFirst = true;
        boolean tFirst = true;
        boolean syFirst = true;
        boolean eFirst = true;
        boolean scFirst = true;
        Field[] fields = objClass.getFields();

        for (Field field : fields) {
            String name = field.getName();
            String[] content = name.split("_");
            String regex = content[0] + content[1];
            Object value = field.get(configConstants);
            switch (regex) {
                case L_REGEX: {
                    if (lFirst) {
                        logger.info(LAUNCHER);
                        lFirst = false;
                    }
                    printConfig(value);
                    break;
                }
                case T_REGEX: {
                    if (tFirst) {
                        logger.info(TRANSPORT);
                        tFirst = false;
                    }
                    printConfig(value);
                    break;
                }
                case SY_REGEX: {
                    if (syFirst) {
                        logger.info(SYS_SERVICE);
                        syFirst = false;
                    }
                    printConfig(value);
                    break;
                }
                case E_REGEX: {
                    if (eFirst) {
                        logger.info(ETCD);
                        eFirst = false;
                    }
                    printConfig(value);
                    break;
                }
                case SC_REGEX: {
                    if (scFirst) {
                        logger.info(ETCD);
                        scFirst = false;
                    }
                    printConfig(value);
                    break;
                }
            }

        }
    }

    private static void printConfig(Object value) {
        logger.info(value + ": " + Launcher.getString((String) value));
    }
}
