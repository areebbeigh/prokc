package com.areebbeigh.server;

public interface TCPServerFactory {
  TCPServer create(int port, TCPServerOptions options, TCPConnectionHandler handler);
}
