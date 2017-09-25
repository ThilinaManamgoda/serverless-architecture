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

package launch;

import lambda.netty.loadbalancer.core.ConfigConstants;
import lambda.netty.loadbalancer.core.etcd.EtcdUtil;
import org.apache.commons.configuration2.XMLConfiguration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.log4j.Logger;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Launcher {

    private final static String CONFIG_PROPERTIES_FILE = "config.xml";

    private static final Logger logger = Logger.getLogger(Launcher.class);


    private static XMLConfiguration xmlConfiguration;

    static {
        Configurations configs = new Configurations();
        try {
            xmlConfiguration = configs.xml(CONFIG_PROPERTIES_FILE);
        } catch (ConfigurationException e) {
            logger.error("Cannot read configurations !", e);
        }
    }

    public static final String TMP_FILE_LOCATION = getString(ConfigConstantKeys.TRANSPORT_TMP_FILE_LOCATION);
    static String ETCD_CLUSTER = getString(ConfigConstants.CONFIG_ETCD_CLUSTER_CONNECTIONS_URL);

    private static final ExecutorService service = Executors.newFixedThreadPool(getInt(ConfigConstantKeys.LAUNCHER_THREADS));

    public static String getString(String tag) {
        return xmlConfiguration.getString(tag);
    }

    public static int getInt(String tag) {
        return xmlConfiguration.getInt(tag);
    }


    public static void main(String[] args) {
        EtcdUtil.setUp(ETCD_CLUSTER);
        logger.info("Starting HTTP Transport service");
        service.submit(new HttpServerLauncher());
    }
}
