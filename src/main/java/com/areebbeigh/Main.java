package com.areebbeigh;

import com.areebbeigh.proxy.ClientHandler;
import com.areebbeigh.proxy.Proxy;
import com.areebbeigh.proxy.ProxyOptions;
import com.areebbeigh.server.TCPServer;
import com.areebbeigh.server.TCPServerOptions;
import java.io.IOException;

public class Main {

  public static void main(String[] args) throws IOException, InterruptedException {
    ProxyOptions options = ProxyOptions.getDefault();
    TCPServer server = TCPServer.create(7070, TCPServerOptions.getDefault(),
        ClientHandler.create(options));
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
}