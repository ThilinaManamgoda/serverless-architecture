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

import io.minio.MinioClient;
import io.minio.Result;
import io.minio.errors.*;
import io.minio.messages.Upload;
import launch.ConfigConstantKeys;
import launch.Launcher;
import org.apache.log4j.Logger;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class ObjectStorageImpl implements ObjectStorage {
    private static final Logger logger = Logger.getLogger(ObjectStorageImpl.class);

    public final static String OBJ_CONNECTION_URL = Launcher.getString(ConfigConstantKeys.CONFIG_OBJ_SERVER_CONNECTION_URL);
    public final static String OBJ_CONNECTION_ACCESSKEY = Launcher.getString(ConfigConstantKeys.CONFIG_OBJ_SERVER_CONNECTION_ACCESSKEY);
    public final static String OBJ_CONNECTION_SECRETKEY = Launcher.getString(ConfigConstantKeys.CONFIG_OBJ_SERVER_CONNECTION_SECRETKEY);

    private MinioClient minioClient;
    private static ObjectStorageImpl instance = null;


    private ObjectStorageImpl() {
        try {
            minioClient = new MinioClient(OBJ_CONNECTION_URL, OBJ_CONNECTION_ACCESSKEY, OBJ_CONNECTION_SECRETKEY);
        } catch (InvalidEndpointException e) {
            logger.error("Invalid end point for Minio client", e);
        } catch (InvalidPortException e) {
            logger.error("Invalid port for Minio client", e);
        }
    }

    public static ObjectStorageImpl getInstance() {
        if (instance == null) {
            instance = new ObjectStorageImpl();
        }
        return instance;
    }


    @Override
    public void storeOBJ(String bucket, String objName, String file) {

        boolean isExist = false;
        try {
            isExist = minioClient.bucketExists(bucket);
            if (isExist) {
                logger.info("Bucket already exists.");
            } else {
                // Make a new bucket called asiatrip to hold a zip file of photos.
                minioClient.makeBucket(bucket);
                logger.info("Bucket is created !");
            }
            minioClient.putObject(bucket, objName,  file);
        } catch (InvalidBucketNameException e) {
            logger.error("Minio client", e);
        } catch (NoSuchAlgorithmException e) {
            logger.error("Minio client", e);
        } catch (InsufficientDataException e) {
            logger.error("Minio client", e);
        } catch (IOException e) {
            logger.error("Minio client", e);
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (NoResponseException e) {
            logger.error("Minio client", e);
        } catch (XmlPullParserException e) {
            logger.error("Minio client", e);
        } catch (ErrorResponseException e) {
            logger.error("Minio client", e);
        } catch (InternalException e) {
            logger.error("Minio client", e);
        } catch (InvalidArgumentException e) {
            logger.error("Minio client", e);
        } catch (RegionConflictException e) {
            logger.error("Minio client", e);
        }

    }

    @Override
    public InputStream getObj(String bucket, String objName) {
        InputStream inputStream = null;
        try {
            inputStream = minioClient.getObject(bucket, objName);
        } catch (InvalidBucketNameException e) {
            logger.error("Invalid bucket name: " + bucket, e);
        } catch (NoSuchAlgorithmException e) {
            logger.error("Minio client", e);
        } catch (InsufficientDataException e) {
            logger.error("Minio client", e);
        } catch (IOException e) {
            logger.error("Minio client", e);
        } catch (InvalidKeyException e) {
            logger.error("Minio client", e);
        } catch (NoResponseException e) {
            logger.error("Minio client", e);
        } catch (XmlPullParserException e) {
            logger.error("Minio client", e);
        } catch (ErrorResponseException e) {
            logger.error("Minio client", e);
        } catch (InternalException e) {
            logger.error("Minio client", e);
        } catch (InvalidArgumentException e) {
            logger.error("Minio client", e);
        }
        return inputStream;
    }

    @Override
    public boolean isIncompleteUpload(String bucket, String objName) {
        boolean return_result = false;
        try {
            Iterable<Result<Upload>> myObjects = minioClient.listIncompleteUploads(bucket);
            Upload upload = null;
            for (Result<Upload> result : myObjects) {
                upload = result.get();
                return_result = upload.objectName().equals(objName);
            }
        } catch (InvalidBucketNameException e) {
            logger.error("Invalid bucket name: " + bucket, e);
        } catch (NoSuchAlgorithmException e) {
            logger.error("Minio client", e);
        } catch (InsufficientDataException e) {
            logger.error("Minio client", e);
        } catch (IOException e) {
            logger.error("Minio client", e);
        } catch (InvalidKeyException e) {
            logger.error("Minio client", e);
        } catch (NoResponseException e) {
            logger.error("Minio client", e);
        } catch (XmlPullParserException e) {
            logger.error("Minio client", e);
        } catch (ErrorResponseException e) {
            logger.error("Minio client", e);
        } catch (InternalException e) {
            logger.error("Minio client", e);
        }


        return return_result;
    }


}
