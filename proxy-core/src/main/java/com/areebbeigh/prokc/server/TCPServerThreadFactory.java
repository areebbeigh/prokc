package com.areebbeigh.prokc.server;

import java.util.concurrent.ThreadFactory;
import lombok.Getter;

public class TCPServerThreadFactory implements ThreadFactory {

  @Getter
  private static final TCPServerThreadFactory instance = new TCPServerThreadFactory();

  private TCPServerThreadFactory() {
  }

  @Override
  public Thread newThread(Runnable r) {
    Thread t = new Thread(r);
    t.setName("ProkcTCPHandler-" + t.getName());
    return t;
  }
}
