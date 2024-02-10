package com.areebbeigh.prokc.proxy.remote;

import com.areebbeigh.prokc.common.HTTPSchemes;
import com.areebbeigh.prokc.proxy.ProxyConfiguration;
import java.net.Socket;
import java.net.URI;
import javax.net.SocketFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.KeyedPooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;

@Slf4j
public class SocketPoolFactory implements KeyedPooledObjectFactory<URI, Socket> {

  private final ProxyConfiguration options;

  public SocketPoolFactory(ProxyConfiguration options) {
    this.options = options;
  }

  @Override
  public void activateObject(URI key, PooledObject<Socket> p) throws Exception {
  }

  @Override
  public void destroyObject(URI key, PooledObject<Socket> p) throws Exception {
    var socket = p.getObject();

    if (socket != null) {
      log.debug("Destroying socket {}", socket);
      socket.close();
    }
  }

  @Override
  public PooledObject<Socket> makeObject(URI key) throws Exception {
    String host = key.getHost();
    int port = key.getPort();
    String scheme = key.getScheme();
    if (port == -1) {
      if (scheme.equalsIgnoreCase(HTTPSchemes.HTTP.name())) {
        port = 80;
      } else {
        port = 443;
      }
    }

    // TODO: Upgrade to TLS socket for HTTPS
    var socket = SocketFactory.getDefault().createSocket(host, port);
    socket.setSoTimeout(options.getRemoteSoTimeout());
    socket.setKeepAlive(true);
    return new DefaultPooledObject<>(socket);
  }

  @Override
  public void passivateObject(URI key, PooledObject<Socket> p) throws Exception {
  }

  @Override
  public boolean validateObject(URI key, PooledObject<Socket> p) {
    return p.getObject() != null && !p.getObject().isClosed();
  }
}
