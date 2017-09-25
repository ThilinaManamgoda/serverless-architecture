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

package core.commands;


import core.utilities.FileType;
import core.utilities.HTTPFileUpload;
import core.utilities.HttpUploadClient;
import org.springframework.shell.core.CommandMarker;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.shell.core.annotation.CliOption;
import org.springframework.stereotype.Component;

import java.io.*;

@Component
public class MainCLI implements CommandMarker {

    private static final String ALL_FUNCTIONS = "ALL_FUNCTIONSlf";

    @CliCommand(value = { "create_func", "cf" })
    public String createFuntion(
//            @CliOption(key = "name",mandatory = true) String name,
            @CliOption(key = "file",mandatory = true) String file
//            @CliOption(key="runtime",mandatory = true)String runtime,
//            @CliOption(key="event",mandatory = true)String event
    ) throws FileNotFoundException {

        String result=null;
        try {
            result= new HTTPFileUpload().uploadFile(FileType.JAVA,new File(file),null);
        } catch (IOException e) {
            e.printStackTrace();
            result = "wrong";
        }


        return result;
    }

    @CliCommand(value = { "list_func", "lf" })
    public String listFunctions(
            @CliOption(key = "runtime",specifiedDefaultValue = ALL_FUNCTIONS) String runtime){
        return runtime;
    }

    @CliCommand(value = { "del_func", "dlf" })
    public String deleteFunction(
            @CliOption(key = "id" ,mandatory = true) String id,
            @CliOption(key = "runtime",mandatory = true) String runtime){
        return id+runtime;
    }
    @CliCommand(value = { "update_event", "upev" })
    public String updateEvent(
            @CliOption(key = "id" ,mandatory = true) String id,
            @CliOption(key = "runtime",mandatory = true) String runtime,
                    @CliOption(key = "event",mandatory = true) String event){
        return id+runtime+event;
    }
    @CliCommand(value = { "update_func", "upf" })
    public String updateFunction(
            @CliOption(key = "id" ,mandatory = true) String id,
            @CliOption(key = "event",mandatory = true) String event,
            @CliOption(key = "runtime",mandatory = true) String runtime
            ){
        return id+runtime+event;
    }
}
