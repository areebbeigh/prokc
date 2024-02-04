package com.areebbeigh.proxy;

import com.areebbeigh.proxy.script.Script;
import java.util.Collections;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ProxyOptions {

  List<Script> scripts;

  public static ProxyOptions getDefault() {
    return ProxyOptions.builder()
        .scripts(Collections.emptyList())
        .build();
  }
}
