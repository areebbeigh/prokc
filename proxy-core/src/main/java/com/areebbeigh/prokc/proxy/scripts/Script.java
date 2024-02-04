package com.areebbeigh.prokc.proxy.scripts;

import java.util.regex.Pattern;
import rawhttp.core.RawHttpRequest;
import rawhttp.core.RawHttpResponse;

public interface Script {
  RawHttpRequest onRequest(RawHttpRequest request);

  RawHttpResponse<?> onResponse(RawHttpResponse<?> response);

  boolean matches(String host);

  Pattern getPattern();

  String getName();
}
