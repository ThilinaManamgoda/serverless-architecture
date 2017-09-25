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

package core.utilities;


import java.io.*;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static core.utilities.FileType.*;

public class HTTPFileUpload  extends HttpUploadClient{
    private static final String FILE_UPLOAD_URL = "http://localhost:8084/file";
    public static final String FUNCTION_IS_UPLOADED = "Function is uploaded";
    public static final String FUNCTION_UPLOADING_FAILED = "Function uploading failed";
    public static final String FILE_PARAMETER_NAME = "file";
    public static final String UNKNOWN_FILE_TYPE = "Unknown file type !";
    public static final String FILE_TYPE_IS_NOT_COMPATIBLE = "File type is not compatible";
    public static final String DOT_REGEX = "\\.";

    public HTTPFileUpload() {
        super(FILE_UPLOAD_URL);
    }


    public  String uploadFile(FileType fileType, File file, List<Map.Entry<String, String>> attributes) throws IOException {
        if (!file.isFile()) {
            return "Cannot find the file !";
        }
        boolean result =false;
        switch (fileType) {
            case JAVA: {
                if (!isFileTypeCorrect(file.getName(), JAVA)) {
                    return FILE_TYPE_IS_NOT_COMPATIBLE;
                }
                try {
                     result = uploadFileAsMultiPart(file,attributes);

                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            }
            case PYTHON: {
                if (!isFileTypeCorrect(file.getName(), PYTHON)) {
                    return FILE_TYPE_IS_NOT_COMPATIBLE;
                }
                try {
                    result = uploadFileAsMultiPart(file,attributes);

                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            }
            case NODE_JS: {
                if (!isFileTypeCorrect(file.getName(), NODE_JS)) {
                    return FILE_TYPE_IS_NOT_COMPATIBLE;
                }
                break;
            }
            case XML: {
                if (!isFileTypeCorrect(file.getName(), XML)) {
                    return FILE_TYPE_IS_NOT_COMPATIBLE;
                }
                break;
            }
            default: {
                return UNKNOWN_FILE_TYPE;
            }
        }
        return  result ? "OK":"NOT OK";
    }

    private  boolean isFileTypeCorrect(String fileName, FileType fileType) {
        String[] array = fileName.split(DOT_REGEX);
        return array[array.length - 1].equals(fileType.getExtention());
    }

    public static void main(String[] args) throws IOException {
        List<Map.Entry<String, String>> attributes = new ArrayList<>();

        attributes.add(new AbstractMap.SimpleEntry<String, String>("testkey","testValue"));
        System.out.println(new HTTPFileUpload().uploadFile(PYTHON, new File("/home/deshan/main.py"),attributes ));
    }
}
