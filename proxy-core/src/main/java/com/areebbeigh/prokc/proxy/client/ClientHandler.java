package com.areebbeigh.prokc.proxy.client;

import com.areebbeigh.prokc.proxy.Flow;
import com.areebbeigh.prokc.proxy.ProxyOptions;
import com.areebbeigh.prokc.proxy.remote.RemoteHandler;
import com.areebbeigh.prokc.proxy.scripts.Script;
import com.areebbeigh.prokc.server.TCPConnectionHandler;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.StringUtils;
import rawhttp.core.RawHttp;
import rawhttp.core.RawHttpRequest;
import rawhttp.core.RawHttpResponse;
import rawhttp.core.errors.InvalidHttpRequest;

/**
 * Handles client to proxy interactions.
 */
@Slf4j
public class ClientHandler implements TCPConnectionHandler {

  private final RawHttp rawHttp;
  private final ProxyOptions options;
  private final RemoteHandler remoteHandler;

  private ClientHandler(ProxyOptions options, RemoteHandler remoteHandler, RawHttp rawHttp) {
    this.options = options;
    this.remoteHandler = remoteHandler;
    this.rawHttp = rawHttp;
  }

  public static ClientHandler create(ProxyOptions options, RemoteHandler remoteHandler,
      RawHttp rawHttp) {
    return new ClientHandler(options, remoteHandler, rawHttp);
  }

  @Override
  public void handle(Socket socket) {
    log.info("[Client] New connection {}", socket.getRemoteSocketAddress());
    try {
      process(socket);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private void process(Socket socket) throws IOException {
    socket.setSoTimeout(options.getClientSoTimeout());
    BufferedInputStream inputStream = new BufferedInputStream(socket.getInputStream());
    BufferedOutputStream outputStream = new BufferedOutputStream(socket.getOutputStream());
    while (!socket.isClosed()) {
      RawHttpRequest request;
      try {
        request = rawHttp.parseRequest(inputStream);
      } catch (InvalidHttpRequest e) {
        log.info("Closing socket {}: {}", socket.getRemoteSocketAddress(), e.getMessage());
        log.debug("Error while parsing request", e);
        inputStream.close();
        outputStream.close();
        socket.close();
        return;
      }

      if (StringUtils.equals(request.getMethod(), "CONNECT")) {
        // TODO: Handle HTTPS here
        throw new NotImplementedException("TLS not implemented");
      }

      Flow flow = Flow.builder()
          .request(request)
          .scripts(getScripts(request))
          .build();
      flow.applyRequestScripts();
      remoteHandler.handle(flow);
      flow.applyResponseScripts();
      RawHttpResponse response = flow.getResponse();
      if (response != null) {
        response.writeTo(outputStream);
        outputStream.flush();
      } else {
        // TODO: Write error to client?
      }
    }
  }

  private List<Script> getScripts(RawHttpRequest request) {
    if (request == null) {
      return Collections.emptyList();
    }

    List<Script> scripts = ListUtils.emptyIfNull(options.getScripts());
    String path = request.getStartLine().getUri().getRawPath();
    return scripts.stream()
        .filter(s -> s.matches(path))
        .collect(Collectors.toList());
  }
}
