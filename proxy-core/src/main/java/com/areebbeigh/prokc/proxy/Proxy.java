package com.areebbeigh.prokc.proxy;

import com.areebbeigh.prokc.server.TCPServer;
import java.io.IOException;

public class Proxy {

  private final TCPServer server;
  private final ProxyConfiguration options;

  private Proxy(TCPServer server, ProxyConfiguration options) {
    this.server = server;
    this.options = options;
  }

  public static Proxy create(TCPServer server, ProxyConfiguration options) {
    return new Proxy(server, options);
  }

  public void start() throws IOException, InterruptedException {
    server.listen();
  }
}
