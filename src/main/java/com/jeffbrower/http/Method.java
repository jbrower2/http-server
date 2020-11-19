package com.jeffbrower.http;

import java.util.Optional;

public enum Method implements RequestMatcher {
  GET(true, true),
  HEAD(true, true),
  POST(false, false),
  PUT(false, true),
  DELETE(false, true),
  CONNECT(false, false),
  OPTIONS(true, true),
  TRACE(true, true);

  public final boolean safe;
  public final boolean idempotent;

  private Method(final boolean safe, final boolean idempotent) {
    this.safe = safe;
    this.idempotent = idempotent;
  }

  @Override
  public boolean matches(final Request req) {
    return req.method == this;
  }

  public static Optional<Method> of(final String s) {
    for (final Method m : values()) {
      if (s.equalsIgnoreCase(m.name())) {
        return Optional.of(m);
      }
    }
    return Optional.empty();
  }
}
