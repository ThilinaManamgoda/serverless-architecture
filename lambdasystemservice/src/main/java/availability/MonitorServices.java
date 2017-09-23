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

import connections.OpenstackAdminConnection;
import org.openstack4j.api.OSClient;
import org.openstack4j.model.identity.v2.User;

import java.util.List;

/**
 * Created by deshan on 8/30/17.
 */
public class MonitorServices {
    List<? extends User> currentUsers;
    OSClient.OSClientV2 os;

    public MonitorServices() {
        this.os = OpenstackAdminConnection.getOpenstackAdminConnection().getOSclient();
    }

    public static void main(String[] args) {
        MonitorServices monitor = new MonitorServices();
        List<? extends User> osUser = monitor.getCurrentUsers();
        for (User user : osUser) {
            new UserPing(monitor.os, monitor.os);
        }
    }

    // get users
    public List<? extends User> getCurrentUsers() {
        try {
            List<? extends User> list = os.identity().users().list();
            return list;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }



}
