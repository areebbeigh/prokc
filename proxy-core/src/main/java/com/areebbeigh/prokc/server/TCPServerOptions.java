package com.areebbeigh.prokc.server;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TCPServerOptions {

  public static int DEFAULT_MAX_CONNECTIONS = 1000;
  private int maxConnections;

  public static TCPServerOptions getDefault() {
    return TCPServerOptions.builder()
        .maxConnections(DEFAULT_MAX_CONNECTIONS)
        .build();
  }
}
