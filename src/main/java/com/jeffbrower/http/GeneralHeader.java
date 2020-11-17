package com.jeffbrower.http;

import java.util.Optional;

public enum GeneralHeader implements Header {
  CACHE_CONTROL,
  CONNECTION,
  CONTENT_DISPOSITION,
  DATE,
  KEEP_ALIVE,
  PRAGMA,
  TRAILER,
  TRANSFER_ENCODING,
  UPGRADE,
  VIA,
  WARNING,
  X_CORRELATION_ID("X-Correlation-ID"),
  X_REQUEST_ID("X-Request-ID");

  public final Header.Type type;
  public final String s;

  private GeneralHeader() {
    this(null);
  }

  private GeneralHeader(final String s) {
    this(Header.Type.SINGLE_VALUE, s);
  }

  private GeneralHeader(final Header.Type type, final String s) {
    this.type = type;
    this.s = s == null ? CaseUtil.upperUnderscoreToTitleKebab(name()) : s;
  }

  @Override
  public Header.Type type() {
    return type;
  }

  @Override
  public String toString() {
    return s;
  }

  public static Optional<GeneralHeader> of(final String s) {
    for (final GeneralHeader h : values()) {
      if (s.equalsIgnoreCase(h.s)) {
        return Optional.of(h);
      }
    }
    return Optional.empty();
  }
}
