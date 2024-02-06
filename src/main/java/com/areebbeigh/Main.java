package com.areebbeigh;

import com.areebbeigh.prokc.proxy.client.ClientHandler;
import com.areebbeigh.prokc.proxy.Proxy;
import com.areebbeigh.prokc.proxy.ProxyOptions;
import com.areebbeigh.prokc.proxy.remote.RemoteHandler;
import com.areebbeigh.prokc.proxy.remote.SocketPoolFactory;
import com.areebbeigh.prokc.proxy.scripts.BaseScript;
import com.areebbeigh.prokc.server.TCPServer;
import com.areebbeigh.prokc.server.TCPServerOptions;
import java.io.IOException;
import java.net.Socket;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.regex.Pattern;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.impl.GenericKeyedObjectPool;
import rawhttp.core.EagerHttpResponse;
import rawhttp.core.RawHttp;
import rawhttp.core.RawHttpHeaders;
import rawhttp.core.RawHttpRequest;
import rawhttp.core.RawHttpResponse;
import rawhttp.core.body.BodyReader;
import rawhttp.core.body.EagerBodyReader;
import rawhttp.core.body.HttpMessageBody;
import rawhttp.core.body.LazyBodyReader;
import rawhttp.core.body.StringBody;

public class Main {

  public static void main(String[] args) throws IOException, InterruptedException {
    ProxyOptions options = ProxyOptions.builder().scripts(List.of(new SampleScript())).build();

    RawHttp rawHttp = new RawHttp();
    GenericKeyedObjectPool<URI, Socket> connectionPool = new GenericKeyedObjectPool<>(
        new SocketPoolFactory(options));
    connectionPool.setMaxTotalPerKey(50);
    RemoteHandler remoteHandler = RemoteHandler.create(rawHttp, connectionPool);

    TCPServer server = TCPServer.create(7070, TCPServerOptions.getDefault(),
        ClientHandler.create(options, remoteHandler, rawHttp));
    Proxy.create(server, options).start();

//    ServerSocket serverSocket = new ServerSocket(7070);
//
//    while (true) {
//      Socket accept = serverSocket.accept();
//      accept.setSoTimeout(10000);
//      RawHttp rawHttp = new RawHttp();
//      while (!accept.isClosed()) {
//        try {
//          System.out.println("Reading request");
//          RawHttpRequest rawHttpRequest = rawHttp.parseRequest(accept.getInputStream()).eagerly();
//          rawHttpRequest.writeTo(System.out);
//          rawHttp.parseResponse("HTTP/1.1 200 OK\\r\\n").writeTo(accept.getOutputStream());
//          Thread.sleep(10000);
//        } catch (SocketException e) {
//          System.out.println("\n SOCKET EXCEPTION: " + e.getMessage());
//          accept.close();
//        } catch (Exception e) {
//          System.out.println(
//              "\nError while reading input: " + e.getClass().getName() + " " + e.getMessage());
//          accept.close();
//        }
//      }
//    }
  }

  @Slf4j
  static class SampleScript extends BaseScript {

    @Override
    public RawHttpRequest onRequest(RawHttpRequest request) {
      log.info("Received request:\n{}", request.toString());
      return request;
    }

    @Override
    @SneakyThrows
    public RawHttpResponse<?> onResponse(RawHttpResponse<?> response) {
      EagerHttpResponse<?> readResponse = response.eagerly();
      EagerBodyReader bodyReader = readResponse.getBody().get();
      String body = bodyReader.decodeBodyToString(StandardCharsets.UTF_8);

      StringBody newBody = new StringBody(body.replace("example", "example1"));

      RawHttpResponse<?> newResponse = new RawHttpResponse<>(readResponse.getLibResponse(),
          readResponse.getRequest().orElse(null), readResponse.getStartLine(),
          readResponse.getHeaders().except("Content-Encoding"), newBody.toBodyReader())
          .withBody(newBody).eagerly();

      log.info("New response:\n{}", newResponse);
      return newResponse;
    }

    @Override
    public Pattern getPattern() {
      return Pattern.compile(".*");
    }

    @Override
    public String getName() {
      return "Sample Script";
    }
  }
}