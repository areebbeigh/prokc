package com.areebbeigh.prokc.proxy;

import com.areebbeigh.prokc.proxy.scripts.Script;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections4.ListUtils;
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
  @Setter
  private RawHttpResponse response;
  private List<Script> scripts;

  public void applyRequestScripts() {
    for (Script script : ListUtils.emptyIfNull(scripts)) {
      this.request = script.onRequest(request);
    }
  }

  public void applyResponseScripts() {
    if (response == null)
      return;

    for (Script script : ListUtils.emptyIfNull(scripts)) {
      this.response = script.onResponse(this.response);
    }
  }
}
