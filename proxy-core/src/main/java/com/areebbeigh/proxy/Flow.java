package com.areebbeigh.proxy;

import com.areebbeigh.proxy.script.Script;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import rawhttp.core.RawHttpRequest;
import rawhttp.core.RawHttpResponse;

/**
 * A Flow represents the end-to-end lifecycle of a request:
 * client -> proxy -> scripts -> response -> scripts -> client
 */
@Builder
@Getter
public class Flow {
  private RawHttpRequest request;
  private RawHttpResponse response;
  private List<Script> scripts;
}
