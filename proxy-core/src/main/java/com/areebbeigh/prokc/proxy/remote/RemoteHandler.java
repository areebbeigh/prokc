package com.areebbeigh.prokc.proxy.remote;

import com.areebbeigh.prokc.proxy.Flow;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URI;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.impl.GenericKeyedObjectPool;
import rawhttp.core.RawHttp;
import rawhttp.core.RawHttpRequest;

/**
 * Handles proxy to remote host interactions
 */
@Slf4j
public class RemoteHandler {
  private final RawHttp rawHttp;
  private final GenericKeyedObjectPool<URI, Socket> socketPool;

  private RemoteHandler(RawHttp rawHttp, GenericKeyedObjectPool<URI, Socket> socketPool) {
    this.rawHttp = rawHttp;
    this.socketPool = socketPool;
  }

  public static RemoteHandler create(RawHttp rawHttp, GenericKeyedObjectPool<URI,Socket> socketPool) {
    return new RemoteHandler(rawHttp, socketPool);
  }

  public void handle(Flow flow) {
    RawHttpRequest request = flow.getRequest();
    URI uri = request.getUri();
    Socket socket = null;
    try {
      socket = socketPool.borrowObject(uri);
      InputStream inputStream = new BufferedInputStream(socket.getInputStream());
      OutputStream outputStream = new BufferedOutputStream(socket.getOutputStream());
      request.writeTo(outputStream);
      outputStream.flush();
      flow.setResponse(rawHttp.parseResponse(inputStream));
      socketPool.returnObject(uri, socket);
    } catch (Exception e) {
      log.info("Closing remote socket: {} - {}", uri, e.getMessage());
      log.debug("Remote send exception {}", uri, e);
      if (socket != null)
        close(uri, socket);
    }
  }

  private void close(URI uri, Socket socket) {
    try {
      socket.close();
    } catch (Exception e) {
      log.error("Error while closing socket", e);
    } finally {
      try {
        socketPool.invalidateObject(uri, socket);
      } catch (Exception e) {
        log.error("CRITICAL: Could not invalidate socket in pool {}", socket, e);
      }
    }
  }
}
