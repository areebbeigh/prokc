package com.areebbeigh.prokc.server;

public interface TCPServerFactory {
  TCPServer create(int port, TCPServerOptions options, TCPConnectionHandler handler);
}
