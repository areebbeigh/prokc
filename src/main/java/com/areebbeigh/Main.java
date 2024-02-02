package com.areebbeigh;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import rawhttp.core.RawHttp;
import rawhttp.core.RawHttpRequest;

public class Main {

  public static void main(String[] args) throws IOException {
    ServerSocket serverSocket = new ServerSocket(7070);

    while (true) {
      Socket accept = serverSocket.accept();
      accept.setSoTimeout(10000);
      RawHttp rawHttp = new RawHttp();
      while (!accept.isClosed()) {
        try {
          System.out.println("Reading request");
          RawHttpRequest rawHttpRequest = rawHttp.parseRequest(accept.getInputStream()).eagerly();
          rawHttpRequest.writeTo(System.out);
          rawHttp.parseResponse("HTTP/1.1 200 OK\\r\\n").writeTo(accept.getOutputStream());
          Thread.sleep(10000);
        } catch (SocketException e) {
          System.out.println("\n SOCKET EXCEPTION: " + e.getMessage());
          accept.close();
        } catch (Exception e) {
          System.out.println(
              "\nError while reading input: " + e.getClass().getName() + " " + e.getMessage());
          accept.close();
        }
      }
    }
  }
}