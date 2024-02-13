package com.areebbeigh.prokc.proxy.client;

import com.areebbeigh.prokc.certificates.CertificateManager;
import com.areebbeigh.prokc.common.HTTPMethod;
import com.areebbeigh.prokc.proxy.Flow;
import com.areebbeigh.prokc.proxy.ProxyConfiguration;
import com.areebbeigh.prokc.proxy.remote.RemoteHandler;
import com.areebbeigh.prokc.proxy.scripts.Script;
import com.areebbeigh.prokc.server.TCPConnectionHandler;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import rawhttp.core.RawHttp;
import rawhttp.core.RawHttpRequest;
import rawhttp.core.RawHttpResponse;
import rawhttp.core.errors.InvalidHttpRequest;

/**
 * Handles client to proxy interactions.
 */
@Slf4j
@RequiredArgsConstructor
public class ClientHandler implements TCPConnectionHandler {

  private static final String SSL_PROTOCOL = "TLSv1.2";

  private final RawHttp rawHttp;
  private final ProxyConfiguration config;
  private final RemoteHandler remoteHandler;
  private final CertificateManager certificateManager;

  @Override
  public void handle(Socket socket) {
    log.info("[Client] New connection {}", socket.getRemoteSocketAddress());
    try {
      process(socket);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private void process(Socket socket) throws Exception {
    socket.setSoTimeout(config.getClientSoTimeout());
    var inputStream = new BufferedInputStream(socket.getInputStream());
    var outputStream = new BufferedOutputStream(socket.getOutputStream());

    while (!socket.isClosed()) {
      RawHttpRequest request;
      try {
        request = rawHttp.parseRequest(inputStream);
      } catch (InvalidHttpRequest e) {
        log.info("Closing socket {}: {} {}", socket.getRemoteSocketAddress(),
                 e.getClass().getName(), e.getMessage());
        log.debug("Error while parsing request", e);
        inputStream.close();
        outputStream.close();
        socket.close();
        return;
      }

      if (StringUtils.equalsIgnoreCase(request.getMethod(), HTTPMethod.CONNECT.name())) {
        var response = rawHttp.parseResponse("HTTP/1.1 200 Connection established");
        writeResponse(outputStream, response);
        socket = upgradeToSSLSocket(socket, request);
      } else {
        var flow = Flow.builder()
                       .request(request)
                       .scripts(getScripts(request))
                       .build();

        flow.applyRequestScripts();
        remoteHandler.handle(flow);
        flow.applyResponseScripts();

        RawHttpResponse response = flow.getResponse();
        if (response != null) {
          writeResponse(outputStream, response);
        } else {
          log.error("Null response for remote call {}", flow);
          // TODO: Write error to client?
        }
      }

    }
  }

  private void writeResponse(BufferedOutputStream outputStream, RawHttpResponse response)
      throws IOException {
    response.writeTo(outputStream);
    outputStream.flush();
  }

  private SSLSocket upgradeToSSLSocket(Socket socket, RawHttpRequest request)
      throws KeyStoreException, CertificateException, NoSuchAlgorithmException, SignatureException, InvalidKeyException, NoSuchProviderException, IOException, UnrecoverableEntryException, KeyManagementException {
    var host = request.getStartLine().getUri().getHost();
    certificateManager.addX509CertToTrustStore(host);
    var sslContext = SSLContext.getInstance(SSL_PROTOCOL);
//    var tmFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
    sslContext.init(certificateManager.getX509KeyManagerFactory(host).getKeyManagers(), null, null);
    var socketFactory = sslContext.getSocketFactory();
    var sslSocket = (SSLSocket) socketFactory.createSocket(socket, null, socket.getPort(),
                                                           true);
    sslSocket.setUseClientMode(false);
    log.debug("Starting SSL handshake");
    sslSocket.startHandshake();
    log.debug("SSL handshake done");
    sslSocket.addHandshakeCompletedListener((e) -> log.info("Handshake completed {}", e));
    return sslSocket;
  }

  private List<Script> getScripts(RawHttpRequest request) {
    if (request == null) {
      return Collections.emptyList();
    }

    var scripts = ListUtils.emptyIfNull(config.getScripts());
    var path = request.getStartLine().getUri().getRawPath();
    return scripts.stream()
                  .filter(s -> s.matches(path))
                  .collect(Collectors.toList());
  }


}
