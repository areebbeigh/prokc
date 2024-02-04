package com.areebbeigh.prokc.proxy.remote;

import com.areebbeigh.prokc.proxy.Flow;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import lombok.SneakyThrows;
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

  @SneakyThrows
  public void handle(Flow flow) {
    RawHttpRequest request = flow.getRequest();
    URI uri = request.getUri();
    // TODO: Evict socket here on connection error
    Socket socket = socketPool.borrowObject(uri);
    InputStream inputStream = new BufferedInputStream(socket.getInputStream());
    OutputStream outputStream = new BufferedOutputStream(socket.getOutputStream());
    request.writeTo(outputStream);
    outputStream.flush();
    flow.setResponse(rawHttp.parseResponse(inputStream));
  }
}
