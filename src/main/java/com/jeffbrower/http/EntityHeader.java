package com.jeffbrower.http;

import java.util.Optional;

public enum EntityHeader implements Header {
  ALLOW,
  CONTENT_ENCODING,
  CONTENT_LANGUAGE,
  CONTENT_LENGTH,
  CONTENT_LOCATION,
  CONTENT_MD5("Content-MD5"),
  CONTENT_RANGE,
  CONTENT_TYPE,
  EXPIRES,
  LAST_MODIFIED,
  LINK;

  public final Header.Type type;
  public final String s;

  private EntityHeader() {
    this(null);
  }

  private EntityHeader(final String s) {
    this(Header.Type.SINGLE_VALUE, s);
  }

  private EntityHeader(final Header.Type type, final String s) {
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

  public static Optional<EntityHeader> of(final String s) {
    for (final EntityHeader h : values()) {
      if (s.equalsIgnoreCase(h.s)) {
        return Optional.of(h);
      }
    }
    return Optional.empty();
  }
}
