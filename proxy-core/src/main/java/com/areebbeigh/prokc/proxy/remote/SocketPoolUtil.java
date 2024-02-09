package com.areebbeigh.prokc.proxy.remote;

import com.areebbeigh.prokc.proxy.ProxyOptions;
import java.net.Socket;
import java.time.Instant;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.impl.EvictionPolicy;

@UtilityClass
@Slf4j
public class SocketPoolUtil {

  public static EvictionPolicy<Socket> getEvictionPolicy(ProxyOptions options) {
    return (config, object, idleCount) -> {
      boolean evict =
          Instant.now().toEpochMilli() - object.getLastUsedInstant().toEpochMilli()
          > options.getMaxConnectionIdleTimeMillis();
      log.debug("Eviction test for {} (borrowed: {} idleCount: {}): {}", object.getObject(),
                object.getBorrowedCount(), idleCount, evict);
      return evict;
    };
  }
}
