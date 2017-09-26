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

package object_storage;

import launch.Launcher;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class DirectoryUtil {
    private static final Logger logger = Logger.getLogger(DirectoryUtil.class);

    public final static String BASE_DIR = Launcher.TMP_FILE_LOCATION;

    public static Path createDir(String name) {
        Path path = Paths.get(BASE_DIR + name);
        if (!Files.exists(path)) {
            try {
                path = Files.createDirectories(path);
                logger.info(name + " Dir is created !");
            } catch (IOException e) {
                logger.error("fail to create directory", e);
            }
        }
        return path;
    }

    public static boolean deleteDir(String name,String file) {
        Path filePath=Paths.get(BASE_DIR+name+"/"+file);
        Path path = Paths.get(BASE_DIR + name);
        boolean result = false;


        try {
            Files.deleteIfExists(filePath);
            result = Files.deleteIfExists(path);
            logger.info(name + " Dir is deleted !");
        } catch (IOException e) {
            logger.error("fail to delete directory", e);
        }
        return result;
    }

}
