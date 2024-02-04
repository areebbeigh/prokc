package com.areebbeigh.prokc.proxy.scripts;

import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class BaseScript implements Script {
  @Override
  public boolean matches(String host) {
    Pattern pattern = getPattern();
    if (pattern == null) {
      log.warn("Unexpected null pattern in script {}", getName());
      return false;
    }

    return pattern.matcher(host).matches();
  }
}
