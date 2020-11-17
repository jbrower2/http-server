package com.jeffbrower.http;

import java.util.Optional;

public enum RequestHeader implements Header {
  A_IM("A-IM"),
  ACCEPT,
  ACCEPT_CHARSET,
  ACCEPT_DATETIME,
  ACCEPT_ENCODING,
  ACCEPT_LANGUAGE,
  ACCEPT_PUSH_POLICY,
  ACCEPT_SIGNATURE,
  ACCESS_CONTROL_REQUEST_HEADERS,
  ACCESS_CONTROL_REQUEST_METHOD,
  AUTHORIZATION,
  COOKIE,
  DEVICE_MEMORY,
  DNT("DNT"),
  DPR("DPR"),
  EARLY_DATA,
  EXPECT,
  FORWARDED,
  FROM,
  FRONT_END_HTTPS,
  HOST,
  HTTP2_SETTINGS("HTTP2-Settings"),
  IF_MATCH,
  IF_MODIFIED_SINCE,
  IF_NONE_MATCH,
  IF_RANGE,
  IF_UNMODIFIED_SINCE,
  LAST_EVENT_ID("Last-Event-ID"),
  MAX_FORWARDS,
  ORIGIN,
  PING_FROM,
  PING_TO,
  PROXY_AUTHORIZATION,
  PROXY_CONNECTION,
  RANGE,
  REFERER,
  SAVE_DATA,
  SEC_FETCH_DEST,
  SEC_FETCH_MODE,
  SEC_FETCH_SITE,
  SEC_FETCH_USER,
  SEC_WEBSOCKET_KEY,
  SEC_WEBSOCKET_VERSION,
  TE("TE"),
  UPGRADE_INSECURE_REQUESTS,
  USER_AGENT,
  VIEWPORT_WIDTH,
  WIDTH,
  X_ATT_DEVICEID("X-ATT-DeviceId"),
  X_CSRF_TOKEN("X-CSRF-Token", "X-CSRFToken", "X-XSRF-Token"),
  X_FORWARDED_FOR,
  X_FORWARDED_HOST,
  X_FORWARDED_PROTO,
  X_HTTP_METHOD_OVERRIDE("X-HTTP-Method-Override"),
  X_REQUESTED_WITH,
  X_UIDH("X-UIDH"),
  X_WAP_PROFILE;

  public final Header.Type type;
  public final String s;
  private final String[] aliases;

  private RequestHeader() {
    this(null);
  }

  private RequestHeader(final String s, final String... aliases) {
    this(Header.Type.SINGLE_VALUE, s, aliases);
  }

  private RequestHeader(final Header.Type type, final String s, final String... aliases) {
    this.type = type;
    this.s = s == null ? CaseUtil.upperUnderscoreToTitleKebab(name()) : s;
    this.aliases = aliases;
  }

  @Override
  public Header.Type type() {
    return type;
  }

  @Override
  public String toString() {
    return s;
  }

  public static Optional<RequestHeader> of(final String s) {
    for (final RequestHeader h : values()) {
      if (s.equalsIgnoreCase(h.s)) {
        return Optional.of(h);
      }
      for (final String alias : h.aliases) {
        if (s.equalsIgnoreCase(alias)) {
          return Optional.of(h);
        }
      }
    }
    return Optional.empty();
  }
}
