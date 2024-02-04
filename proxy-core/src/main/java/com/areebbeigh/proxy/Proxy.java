package com.areebbeigh.proxy;

import com.areebbeigh.server.TCPServer;
import java.io.IOException;

public class Proxy {

  private final TCPServer server;
  private final ProxyOptions options;

  private Proxy(TCPServer server, ProxyOptions options) {
    this.server = server;
    this.options = options;
  }

  public static Proxy create(TCPServer server, ProxyOptions options) {
    return new Proxy(server, options);
  }

  public void start() throws IOException, InterruptedException {
    server.listen();
  }
}
