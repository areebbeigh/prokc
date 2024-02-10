package com.areebbeigh.prokc.proxy;

import com.areebbeigh.prokc.proxy.scripts.Script;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ProxyConfiguration {

  // Transformers/scripts for requests
  List<Script> scripts;

  // Sockets
  private int remoteSoTimeout;
  private int clientSoTimeout;
  private long maxConnectionIdleTimeMillis;

  // Certificate management
  private Path rootCAPath;
  private Path keyStorePath;
}
