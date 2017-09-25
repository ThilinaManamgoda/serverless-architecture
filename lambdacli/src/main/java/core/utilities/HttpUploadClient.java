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

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.multipart.*;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.util.internal.SocketUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URI;
import java.util.List;
import java.util.Map.Entry;


public class HttpUploadClient {

     private String baseUrl;



    public HttpUploadClient(String baseUrl){
        this.baseUrl =baseUrl;
    }

    public   boolean uploadFileAsMultiPart(File file, List<Entry<String, String>> attributes) throws Exception {


        URI uriSimple = new URI(baseUrl);
        String scheme = uriSimple.getScheme() == null? "http" : uriSimple.getScheme();
        String host = uriSimple.getHost() == null? "127.0.0.1" : uriSimple.getHost();
        int port = uriSimple.getPort();
        if (port == -1) {
            if ("http".equalsIgnoreCase(scheme)) {
                port = 80;
            } else if ("https".equalsIgnoreCase(scheme)) {
                port = 443;
            }
        }

        if (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme)) {
            System.err.println("Only HTTP(S) is supported.");
            return false;
        }

        final boolean ssl = "https".equalsIgnoreCase(scheme);
        final SslContext sslCtx;
        if (ssl) {
            sslCtx = SslContextBuilder.forClient()
                .trustManager(InsecureTrustManagerFactory.INSTANCE).build();
        } else {
            sslCtx = null;
        }


        if (!file.canRead()) {
            throw new FileNotFoundException(file.getPath());
        }

        // Configure the client.
        EventLoopGroup group = new NioEventLoopGroup();

        // setup the factory: here using a mixed memory/disk based on size threshold
        HttpDataFactory factory = new DefaultHttpDataFactory(DefaultHttpDataFactory.MINSIZE); // Disk if MINSIZE exceed

        DiskFileUpload.deleteOnExitTemporaryFile = true; // should delete file on exit (in normal exit)
        DiskFileUpload.baseDirectory = null; // system temp directory
        DiskAttribute.deleteOnExitTemporaryFile = true; // should delete file on exit (in normal exit)
        DiskAttribute.baseDirectory = null; // system temp directory

        CustomFuture customFuture = new CustomFuture();
        try {

            Bootstrap b = new Bootstrap();
            b.group(group).channel(NioSocketChannel.class).handler(new HttpUploadClientInitializer(sslCtx,customFuture));

            // Simple Get form: no factory used (not usable)
            List<Entry<String, String>> headers = formHeaders( host, uriSimple);
            if (headers == null) {
                factory.cleanAllHttpData();
                return false;
            }

            // Simple Post form: factory used for big attributes
            List<InterfaceHttpData> bodylist = buildBody(uriSimple, file, factory, attributes);
            if (bodylist == null) {
                factory.cleanAllHttpData();
                return false;
            }

            // Multipart Post form: factory used
            formpostmultipart(b, host, port, uriSimple, factory, headers, bodylist);
        } finally {
            // Shut down executor threads to exit.
            group.shutdownGracefully();

            // Really clean all temporary files if they still exist
            factory.cleanAllHttpData();
        }
        return customFuture.get();
    }


    private  List<Entry<String, String>> formHeaders(String host, URI uriSimple) throws Exception {


        HttpRequest request = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, uriSimple.toASCIIString());
        HttpHeaders headers = request.headers();
        headers.set(HttpHeaderNames.HOST, host);
        headers.set(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE);
        headers.set(HttpHeaderNames.ACCEPT_ENCODING, HttpHeaderValues.BINARY + "," + HttpHeaderValues.DEFLATE);

        headers.set(HttpHeaderNames.ACCEPT_CHARSET, "ISO-8859-1,utf-8;q=0.7,*;q=0.7");
        headers.set(HttpHeaderNames.ACCEPT_LANGUAGE, "fr");
        headers.set(HttpHeaderNames.REFERER, uriSimple.toString());
        headers.set(HttpHeaderNames.USER_AGENT, "Netty Simple Http Client side");
        headers.set(HttpHeaderNames.ACCEPT, "text/html,application/xhtml+xml,application/json;q=0.9,*/*;q=0.8");

        //connection will not close but needed
        // headers.set("Connection","keep-alive");
        // headers.set("Keep-Alive","300");
//
//        headers.set(
//                HttpHeaderNames.COOKIE, ClientCookieEncoder.STRICT.encode(
//                        new DefaultCookie("my-cookie", "foo"),
//                        new DefaultCookie("another-cookie", "bar"))
//        );


        // convert headers to list
        return headers.entries();
    }

    /**
     * Standard post without multipart but already support on Factory (memory management)
     *
     * @return the list of HttpData object (attribute and file) to be reused on next post
     */
    private  List<InterfaceHttpData> buildBody(URI uriSimple, File file, HttpDataFactory factory,
            List<Entry<String, String>> attributes) throws Exception {

        // Prepare the HTTP request.
        HttpRequest request = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, uriSimple.toASCIIString());

        // Use the PostBody encoder
        HttpPostRequestEncoder bodyRequestEncoder =
                new HttpPostRequestEncoder(factory, request, true);


        // add Form attribute

      for (Entry<String,String> item:attributes){
          bodyRequestEncoder.addBodyAttribute(item.getKey(),item.getValue());
      }



        bodyRequestEncoder.addBodyFileUpload("file", file, "binary", false);


        // Create the bodylist to be reused on the last version with Multipart support
        List<InterfaceHttpData> bodylist = bodyRequestEncoder.getBodyListAttributes();

        return bodylist;
    }

    /**
     * Multipart example
     */
    private  void formpostmultipart(
            Bootstrap bootstrap, String host, int port, URI uriFile, HttpDataFactory factory,
            Iterable<Entry<String, String>> headers, List<InterfaceHttpData> bodylist) throws Exception {
        // XXX /formpostmultipart
        // Start the connection attempt.
        ChannelFuture future = bootstrap.connect(SocketUtils.socketAddress(host, port));
        // Wait until the connection attempt succeeds or fails.
        Channel channel = future.sync().channel();

        // Prepare the HTTP request.
        HttpRequest request = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, uriFile.toASCIIString());

        // Use the PostBody encoder
        HttpPostRequestEncoder bodyRequestEncoder =
                new HttpPostRequestEncoder(factory, request, true); // true => multipart

        // it is legal to add directly header or cookie into the request until finalize
        for (Entry<String, String> entry : headers) {
            request.headers().set(entry.getKey(), entry.getValue());
        }

        // add Form attribute from previous request in buildBody()
        bodyRequestEncoder.setBodyHttpDatas(bodylist);

        // finalize request
        bodyRequestEncoder.finalizeRequest();

        // send request
        channel.write(request);

        // test if request was chunked and if so, finish the write
        if (bodyRequestEncoder.isChunked()) {
            channel.write(bodyRequestEncoder);
        }
        channel.flush();

        // Now no more use of file representation (and list of HttpData)
        bodyRequestEncoder.cleanFiles();

        // Wait for the server to close the connection.
        channel.closeFuture().sync();
    }
}
