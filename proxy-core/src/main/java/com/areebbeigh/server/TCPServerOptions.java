package com.areebbeigh.server;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TCPServerOptions {
  public static int DEFAULT_MAX_CONNECTIONS = 1000;
  private int maxConnections = DEFAULT_MAX_CONNECTIONS;
}
