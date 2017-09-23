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

package availability;

import org.openstack4j.api.OSClient;
import org.openstack4j.model.compute.Server;

import java.util.List;

/**
 * Created by deshan on 8/30/17.
 */
public class UserPing extends Thread {
    OSClient.OSClientV2 user;
    OSClient.OSClientV2 adminuser;

    public UserPing(OSClient.OSClientV2 user, OSClient.OSClientV2 adminuser) {
        this.user = user;
        this.adminuser = adminuser;
    }

    //ping to a host
    public boolean pingHostByCommand(String host) {
        try {
            String strCommand = "";
//            System.out.println("My OS :" + System.getProperty("os.name"));
            if (System.getProperty("os.name").startsWith("Windows")) {
                // construct command for Windows Operating system
                strCommand = "ping -n 1 " + host;
            } else {
                // construct command for Linux and OSX
                strCommand = "ping -c 1 " + host;
            }
            // Execute the command constructed
            Process myProcess = Runtime.getRuntime().exec(strCommand);
            myProcess.waitFor();
            if (myProcess.exitValue() == 0) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    // list servers per user
    public List<? extends Server> getServers(OSClient.OSClientV2 osUser) {
        //
        // get service of the user fro user;
        //
        try {
            List<? extends Server> servers = osUser.compute().servers().list();
            return servers;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void run() {
        List<? extends Server> list = this.getServers(this.user);
        String ipAddress = null;
        boolean pingStatus = false;
        int count;
        int lastCallFalse1;
        int lastCallFalse2;

        for (Server singleServer : list) {
            ipAddress = singleServer.getAccessIPv4();
            count = 0;
            lastCallFalse1 = 0;
            lastCallFalse2 = 0;
            for (int i = 0; i < 4; i++) {
                pingStatus = pingHostByCommand(ipAddress);
                if (pingStatus) {
                    count++;
                } else {
                    lastCallFalse1 = lastCallFalse2;
                    lastCallFalse2 = i;
                }
            }

            if (count >= 2) {
                // update etcd
            } else {
                //update etcd
                if (lastCallFalse1 == 2 && lastCallFalse2 == 3) ;
            }
        }

    }
}
