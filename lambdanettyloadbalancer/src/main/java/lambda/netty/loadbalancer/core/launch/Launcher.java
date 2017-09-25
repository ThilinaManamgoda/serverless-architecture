/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package lambda.netty.loadbalancer.core.launch;

import lambda.netty.loadbalancer.core.ConfigConstants;
import lambda.netty.loadbalancer.core.Server;
import lambda.netty.loadbalancer.core.etcd.EtcdClientException;
import lambda.netty.loadbalancer.core.etcd.EtcdUtil;
import lambda.netty.loadbalancer.core.loadbalance.StateImplJsonHelp;
import lambda.netty.loadbalancer.core.loadbalance.statemodels.InstanceStates;
import lambda.netty.loadbalancer.core.loadbalance.statemodels.OSVInstance;
import lambda.netty.loadbalancer.core.loadbalance.statemodels.State;
import lambda.netty.loadbalancer.core.loadbalance.statemodels.StateImpl;
import lambda.netty.loadbalancer.core.scalability.ScalabilityManager;
import org.apache.commons.configuration2.XMLConfiguration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.log4j.Logger;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
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
    private static ExecutorService service = Executors.newFixedThreadPool(Launcher.getIntValue(ConfigConstants.CONFIG_LAUNCHER_THREADS));
    public final static boolean SCALABILITY_ENABLED = Launcher.getBoolean(ConfigConstants.CONFIG_SCALABILITY_ENABLED);
    public static String ETCD_CLUSTER = Launcher.getString(ConfigConstants.CONFIG_ETCD_CLUSTER_CONNECTIONS_URL);

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
            tmp.forEach(x -> tmp_return.add(Integer.parseInt((String) x)));

            return tmp_return;
        }
        return null;
    }

    public static boolean getBoolean(String key) {
        String val = getString(key);

        return val.equals("true") ? true : false;
    }

    public static long getLong(String s) {

        return xmlConfiguration.getLong(s);
    }

    public static void main(String[] args) throws InterruptedException, EtcdClientException {

//        State state = new StateImpl();
//        OSVInstance osvInstance = new OSVInstance();
//        osvInstance.setHost("127.0.0.1:8082");
//        osvInstance.setUuid(new UUID(1,1));
//        state.pushOSVInstance(osvInstance);
//        state.setState(InstanceStates.RUNNING);
//        state.setDomain("localhost");
//
//        System.out.println(StateImplJsonHelp.toString(state));
//        try {
//            EtcdUtil.putValue("localhost",StateImplJsonHelp.toString(state)).get();
//            System.out.println(EtcdUtil.getValue("localhost").get().getKvs().get(0).getValue().toString(StandardCharsets.UTF_8));
//        } catch (EtcdClientException e) {
//            e.printStackTrace();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        } catch (ExecutionException e) {
//            e.printStackTrace();
//        }
        EtcdUtil.setUp(ETCD_CLUSTER);
        try {
            ConfigLogger.printFields();
        } catch (Exception e) {
            logger.error("Cannot print Configurations !", e);
        }


        if (SCALABILITY_ENABLED) {
            service.submit(new ScalabilityManager());
        } else {
            logger.info("Scalability is not enabled !");
        }
        service.submit(new Server());

    }

}
