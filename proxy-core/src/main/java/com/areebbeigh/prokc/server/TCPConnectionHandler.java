package com.areebbeigh.prokc.server;

import java.net.Socket;

public interface TCPConnectionHandler {
  void handle(Socket socket);
}
