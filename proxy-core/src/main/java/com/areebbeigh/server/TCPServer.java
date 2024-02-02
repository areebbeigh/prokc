package com.areebbeigh.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import javax.net.ServerSocketFactory;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TCPServer {
  private final ServerSocket socket;
  private final ExecutorService executor;
  private final TCPConnectionHandler handler;
  private final TCPServerOptions options;

  public TCPServer(ServerSocket socket, TCPServerOptions options,
      TCPConnectionHandler connectionHandler) {
    this.options = options;
    this.executor = createThreadPool();
    this.socket = socket;
    this.handler = connectionHandler;
  }

  private ExecutorService createThreadPool() {
    return Executors.newCachedThreadPool(TCPServerThreadFactory.getInstance());
  }

  public static TCPServer create(int port, TCPServerOptions options, TCPConnectionHandler handler)
      throws IOException {
    return new TCPServer(ServerSocketFactory.getDefault().createServerSocket(port), options,
        handler);
  }

  public void listen() throws IOException, InterruptedException {
    Semaphore semaphore = new Semaphore(options.getMaxConnections());

    while (!socket.isClosed()) {
      semaphore.acquire();
      Socket clientSocket = socket.accept();

      executor.submit(() -> {
        try {
          handler.handle(clientSocket);
        } catch (Exception e) {
          log.error("Exception in client socket handler", e);
        }
      });
    }
  }
}
