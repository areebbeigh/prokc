package com.areebbeigh.prokc.server;

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

  private TCPServer(ServerSocket socket, TCPServerOptions options,
      TCPConnectionHandler connectionHandler) {
    this.options = options;
    this.executor = createThreadPool();
    this.socket = socket;
    this.handler = connectionHandler;
  }

  public static TCPServer create(int port, TCPServerOptions options, TCPConnectionHandler handler) {
    try {
      return new TCPServer(ServerSocketFactory.getDefault().createServerSocket(port), options,
          handler);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private ExecutorService createThreadPool() {
    return Executors.newCachedThreadPool(TCPServerThreadFactory.getInstance());
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
        } finally {
          close(clientSocket);
          semaphore.release();
        }
      });
    }
  }

  private void close(Socket socket) {
    try {
      socket.close();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
