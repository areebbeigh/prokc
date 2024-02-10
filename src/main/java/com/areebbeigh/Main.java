package com.areebbeigh;

import com.areebbeigh.prokc.certificates.CertificateGenerator;
import com.areebbeigh.prokc.certificates.CertificateManager;
import com.areebbeigh.prokc.certificates.KeyStoreManager;
import com.areebbeigh.prokc.proxy.Proxy;
import com.areebbeigh.prokc.proxy.ProxyConfiguration;
import com.areebbeigh.prokc.proxy.client.ClientHandler;
import com.areebbeigh.prokc.proxy.remote.RemoteHandler;
import com.areebbeigh.prokc.proxy.remote.SocketPoolFactory;
import com.areebbeigh.prokc.proxy.remote.SocketPoolUtil;
import com.areebbeigh.prokc.proxy.scripts.BaseScript;
import com.areebbeigh.prokc.server.TCPServer;
import com.areebbeigh.prokc.server.TCPServerOptions;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.time.Duration;
import java.util.List;
import java.util.regex.Pattern;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.impl.GenericKeyedObjectPool;
import rawhttp.core.EagerHttpResponse;
import rawhttp.core.RawHttp;
import rawhttp.core.RawHttpRequest;
import rawhttp.core.RawHttpResponse;
import rawhttp.core.body.EagerBodyReader;
import rawhttp.core.body.StringBody;

public class Main {

  public static void main(String[] args)
      throws IOException, InterruptedException, CertificateException, KeyStoreException, NoSuchAlgorithmException {
    // TODO: Move out init and config to utility classes/properties files
    // Configuration
    var config = ProxyConfiguration.builder().scripts(List.of(new SampleScript()))
                                   .maxConnectionIdleTimeMillis(2000).keyStorePath(
            Paths.get(System.getProperty("user.home"), ".prokc/prokc.keystore")).build();

    // Certificate manager
    var certGenerator = new CertificateGenerator();
    var keyStoreManager = new KeyStoreManager(config);
    var certificateManager = new CertificateManager(certGenerator, keyStoreManager);

    // HTTP Parser
    var rawHttp = new RawHttp();

    // Socket pool
    var connectionPool = new GenericKeyedObjectPool<>(new SocketPoolFactory(config));
    connectionPool.setMaxTotalPerKey(50);
    connectionPool.setDurationBetweenEvictionRuns(Duration.ofMillis(2000));
    connectionPool.setEvictionPolicy(SocketPoolUtil.getEvictionPolicy(config));

    // Handlers
    var remoteHandler = RemoteHandler.create(rawHttp, connectionPool);
    var clientHandler = new ClientHandler(rawHttp, config, remoteHandler, certificateManager);

    // Server
    var server = TCPServer.create(7070, TCPServerOptions.getDefault(), clientHandler);
    Proxy.create(server, config).start();

  }

  @Slf4j
  static class SampleScript extends BaseScript {

    @Override
    public RawHttpRequest onRequest(RawHttpRequest request) {
      log.info("Received request:\n{}", request.toString());
      return request;
    }

    @Override
    @SneakyThrows
    public RawHttpResponse<?> onResponse(RawHttpResponse<?> response) {
      EagerHttpResponse<?> readResponse = response.eagerly();
      EagerBodyReader bodyReader = readResponse.getBody().get();
      var body = bodyReader.decodeBodyToString(StandardCharsets.UTF_8);

      var newBody = new StringBody(body.replace("example", "example1"));

      var newResponse = new RawHttpResponse<>(readResponse.getLibResponse(),
                                              readResponse.getRequest().orElse(null),
                                              readResponse.getStartLine(),
                                              readResponse.getHeaders().except(
                                                  "Content-Encoding"),
                                              newBody.toBodyReader()).withBody(
          newBody).eagerly();

      log.info("New response:\n{}", newResponse);
      return newResponse;
    }

    @Override
    public Pattern getPattern() {
      return Pattern.compile(".*");
    }

    @Override
    public String getName() {
      return "Sample Script";
    }
  }
}