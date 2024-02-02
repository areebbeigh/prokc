package com.areebbeigh.server;

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
    t.setName("ProkcTCPThread-" + t.getName());
    return t;
  }
}
