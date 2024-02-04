package com.areebbeigh.proxy;

import com.areebbeigh.proxy.script.Script;
import com.areebbeigh.server.TCPConnectionHandler;
import java.io.BufferedInputStream;
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

@Slf4j
public class ClientHandler implements TCPConnectionHandler {

  private final RawHttp rawHttp;
  private final ProxyOptions options;

  private ClientHandler(ProxyOptions options) {
    this.options = options;
    rawHttp = new RawHttp();
  }

  public static ClientHandler create(ProxyOptions options) {
    return new ClientHandler(options);
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
    BufferedInputStream inputStream = new BufferedInputStream(socket.getInputStream());
    while (!socket.isClosed()) {
      RawHttpRequest request = rawHttp.parseRequest(inputStream);
      // TODO: Handle HTTPS here
      if (StringUtils.equals(request.getMethod(), "CONNECT")) {
        throw new NotImplementedException("TLS not implemented");
      }
      Flow flow = Flow.builder()
          .request(request)
          .scripts(getScripts(request))
          .build();
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
