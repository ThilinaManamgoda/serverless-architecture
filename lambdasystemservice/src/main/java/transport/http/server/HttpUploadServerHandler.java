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

package transport.http.server;

import db.modal.*;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.multipart.*;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder.EndOfDataDecoderException;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder.ErrorDataDecoderException;
import io.netty.handler.codec.http.multipart.InterfaceHttpData.HttpDataType;
import io.netty.util.CharsetUtil;
import lambda.netty.loadbalancer.core.etcd.EtcdClientException;
import lambda.netty.loadbalancer.core.etcd.EtcdUtil;
import lambda.netty.loadbalancer.core.loadbalance.StateImplJsonHelp;
import lambda.netty.loadbalancer.core.loadbalance.statemodels.InstanceStates;
import lambda.netty.loadbalancer.core.loadbalance.statemodels.OSVInstance;
import lambda.netty.loadbalancer.core.loadbalance.statemodels.State;
import lambda.netty.loadbalancer.core.loadbalance.statemodels.StateImpl;
import launch.ConfigConstantKeys;
import launch.Launcher;
import object_storage.ObjectStorage;
import object_storage.ObjectStorageImpl;
import org.apache.log4j.Logger;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;

/**
 * This handler is handling the file upload.
 * If the request is for PREFIX and is multipart then next handlers in the channel are not executed
 * User data can be send via body attributes in the multipart request and need to handle database/VM building here
 */
public class HttpUploadServerHandler extends SimpleChannelInboundHandler<HttpObject> {

    private static final Logger logger = Logger.getLogger(HttpUploadServerHandler.class);
    private static final HttpDataFactory factory =
            new DefaultHttpDataFactory(DefaultHttpDataFactory.MINSIZE); // Disk if size exceed
    public static final String PREFIX = Launcher.getString(ConfigConstantKeys.FILE_UPLOAD_URL);

    static {
        DiskFileUpload.deleteOnExitTemporaryFile = true; // should delete file
        // on exit (in normal
        // exit)
        DiskFileUpload.baseDirectory = null; // system temp directory
        DiskAttribute.deleteOnExitTemporaryFile = true; // should delete file on
        // exit (in normal exit)
        DiskAttribute.baseDirectory = null; // system temp directory
    }

    private HttpRequest request;
    private boolean isFileUpload = true;
    private HttpData partialContent;
    private HttpPostRequestDecoder decoder;
    private User user;
    private Function function;

    HttpUploadServerHandler() {
        super(false);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        if (decoder != null) {
            decoder.cleanFiles();
        }
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, HttpObject msg) throws Exception {
        if (isFileUpload) {
            if (msg instanceof HttpRequest) {
                HttpRequest request = this.request = (HttpRequest) msg;
                URI uri = new URI(request.uri());
                if (!(uri.getPath().startsWith(PREFIX) && HttpPostRequestDecoder.isMultipart(request))) {
                    notAFileUpload(ctx, msg);
                    return;
                }
                // allocating memory
                user = new User();
                function = new Function();
                try {
                    decoder = new HttpPostRequestDecoder(factory, request);
                } catch (ErrorDataDecoderException e1) {
                    logger.error("Decoder error", e1);
                    writeErrorResponse(ctx.channel());
                    ctx.channel().close();
                    return;
                }
            }

            // check if the decoder was constructed before
            // if not it handles the form get
            if (decoder != null) {
                if (msg instanceof HttpContent) {
                    // New chunk is received
                    HttpContent chunk = (HttpContent) msg;
                    try {
                        decoder.offer(chunk);
                    } catch (ErrorDataDecoderException e1) {
                        e1.printStackTrace();
                        writeErrorResponse(ctx.channel());
                        ctx.channel().close();
                        return;
                    }
                    // example of reading chunk by chunk (minimize memory usage due to
                    // Factory)
                    readHttpDataChunkByChunk();
                    // example of reading only if at the end
                    if (chunk instanceof LastHttpContent) {
                        writeOkResponse(ctx.channel());

                        reset();
                    }
                }
            }

        } else {

            notAFileUpload(ctx, msg);
        }

    }


    private void notAFileUpload(ChannelHandlerContext ctx, HttpObject msg) {
        isFileUpload = false;
        ctx.fireChannelRead(msg);
    }

    private void reset() {
        request = null;

        // destroy the decoder to release all resources
        decoder.destroy();
        decoder = null;
    }

    /**
     * Example of reading request by chunk and getting values from chunk to chunk
     */
    private void readHttpDataChunkByChunk() {
        try {
            while (decoder.hasNext()) {
                InterfaceHttpData data = decoder.next();
                if (data != null) {
                    // check if current HttpData is a FileUpload and previously set as partial
                    if (partialContent == data) {
                        logger.info(" 100% (FinalSize: " + partialContent.length() + ")");
                        partialContent = null;
                    }
                    try {
                        // new value
                        writeHttpData(data);
                    } finally {
                        data.release();
                    }
                }
            }
            // Check partial decoding for a FileUpload
            InterfaceHttpData data = decoder.currentPartialHttpData();
            if (data != null) {
                StringBuilder builder = new StringBuilder();
                if (partialContent == null) {
                    partialContent = (HttpData) data;
                    if (partialContent instanceof FileUpload) {
                        builder.append("Start FileUpload: ")
                                .append(((FileUpload) partialContent).getFilename()).append(" ");
                    } else {
                        builder.append("Start Attribute: ")
                                .append(partialContent.getName()).append(" ");
                    }
                    builder.append("(DefinedSize: ").append(partialContent.definedLength()).append(")");
                }
                if (partialContent.definedLength() > 0) {
                    builder.append(" ").append(partialContent.length() * 100 / partialContent.definedLength())
                            .append("% ");
                    logger.info("defined " + builder.toString());
                } else {
                    builder.append(" ").append(partialContent.length()).append(" ");
                    logger.info("else " + builder.toString());
                }
            }
        } catch (EndOfDataDecoderException e1) {
            logger.info("Done uploading");

        }
    }

    private void fillData(String key, String value) {
        switch (key) {
            case "user_id": {
                user.setId(Long.parseLong(value));
                logger.info("User Id: " + value);
                break;
            }
            case "user_domain": {
                user.setDomain(value);
                logger.info("User: " + user.getId() + " Domain: " + value);
                break;
            }
            case "function_name": {
                function.setName(value);
                logger.info("User: " + user.getId() + " Function Name: " + value);
                break;
            }
            case "function_event": {
                function.setEvent(value);
                logger.info("User: " + user.getId() + " Function event: " + value);
                break;
            }
            case "function_type": {
                function.setType(setFunctionType(Integer.parseInt(value)));
                logger.info("User: " + user.getId() + " Function type: " + value);
                break;
            }
        }

    }

    private FileType setFunctionType(int value) {
        //TODO Implement a way to find file type
        return FileType.PYTHON;
    }

    private void writeHttpData(InterfaceHttpData data) {
        if (data.getHttpDataType() == HttpDataType.Attribute) {
            Attribute attribute = (Attribute) data;
            try {
                fillData(attribute.getName(), attribute.getValue());
            } catch (IOException e1) {
                // Error while reading data from File, only print name and error
                e1.printStackTrace();
                return;
            }
        } else {
            if (data.getHttpDataType() == HttpDataType.FileUpload) {

                FileUpload fileUpload = (FileUpload) data;
                if (fileUpload.isCompleted()) {

                    ByteBuf byteBuf = fileUpload.content();
                    FileOutputStream fileOutputStream = null;
                    try {
                        fileOutputStream = new FileOutputStream(Launcher.TMP_FILE_LOCATION + fileUpload.getFilename());
                        while (byteBuf.isReadable()) {
                            fileOutputStream.write(byteBuf.readByte());
                        }
                       //Store user info mongodb
                        assignFunctionToUser();

                        // save user in etcd
                        EtcdUtil.putValue(String.valueOf(user.getId()), UserJsonHelp.ToJson(user));

                        // push function stte to etcd
                        pushStateToEtcd();

                        //Upload function to Minion server
                        ObjectStorage objectStorage = ObjectStorageImpl.getInstance();
                        // bucket = user ID      ObjName = functionName
                        objectStorage.storeOBJ(String.valueOf(user.getId()), function.getName(), fileUpload.getFilename());
                    } catch (IOException e) {
                        logger.error("Cannot write to the file", e);
                    } catch (EtcdClientException e) {
                        e.printStackTrace();

                    }finally {
                        try {
                            fileOutputStream.close();
                        } catch (IOException e) {
                            logger.error("Cannot close File Stream !", e);
                        }
                    }

                } else {
                    logger.info("develop");
                }
            }
        }
    }

    private void pushStateToEtcd() {

        OSVInstance osvInstance = new OSVInstance();
        State state = new StateImpl();
        String domain = function.getDomainName();
        state.setDomain(domain);
        state.setState(InstanceStates.DOWN);
        state.pushOSVInstance(osvInstance);
        try {
            EtcdUtil.putValue(domain, StateImplJsonHelp.toString(state));
        } catch (EtcdClientException e) {
            logger.error("Cannot' convert to String !", e);
        }
        logger.info("function state is added to Etcd !: " + domain);
    }

    private void assignFunctionToUser() {
        // Function domain is created by user.Domain + function.Name
        function.setDomainName( function.getName()+user.getDomain() );
        user.addFunction(function);
        logger.info("Function is assigned to User ! " + function.getDomainName());
    }

    private void initiateFunction() {

    }

    private void writeOkResponse(Channel channel) {
        sendResponse(channel, HttpResponseStatus.OK, "Done");
    }

    private void writeErrorResponse(Channel channel) {
        sendResponse(channel, HttpResponseStatus.INTERNAL_SERVER_ERROR, "File upload is failed");
    }

    private void sendResponse(Channel channel, HttpResponseStatus ok, String string) {
        // Decide whether to close the connection or not.
        boolean close = request.headers().contains(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE, true)
                || request.protocolVersion().equals(HttpVersion.HTTP_1_0)
                && !request.headers().contains(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE, true);
        ByteBuf content = Unpooled.copiedBuffer(string, CharsetUtil.UTF_8);
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, ok, content);
        response.headers().set(HttpHeaders.Names.CONTENT_TYPE, "text/plain");
        response.headers().set(HttpHeaders.Names.CONTENT_LENGTH, content.readableBytes());
        // Write the response.
        ChannelFuture future = channel.writeAndFlush(response);
//         Close the connection after the write operation is done if necessary.
        if (close) {
            future.addListener(ChannelFutureListener.CLOSE);
        }
//        channel.close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.channel().close();
    }
}
