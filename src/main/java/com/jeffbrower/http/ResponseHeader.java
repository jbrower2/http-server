package com.jeffbrower.http;

import java.util.Optional;

public enum ResponseHeader implements Header {
  ACCEPT_CH("Accept-CH"),
  ACCEPT_CH_LIFETIME("Accept-CH-Lifetime"),
  ACCEPT_PATCH,
  ACCEPT_RANGES,
  ACCESS_CONTROL_ALLOW_CREDENTIALS,
  ACCESS_CONTROL_ALLOW_HEADERS,
  ACCESS_CONTROL_ALLOW_METHODS,
  ACCESS_CONTROL_ALLOW_ORIGIN,
  ACCESS_CONTROL_EXPOSE_HEADERS,
  ACCESS_CONTROL_MAX_AGE,
  AGE,
  ALT_SVC,
  CLEAR_SITE_DATA,
  CONTENT_DPR("Content-DPR"),
  CONTENT_SECURITY_POLICY,
  CONTENT_SECURITY_POLICY_REPORT_ONLY,
  CROSS_ORIGIN_EMBEDDER_POLICY,
  CROSS_ORIGIN_OPENER_POLICY,
  CROSS_ORIGIN_RESOURCE_POLICY,
  DELTA_BASE,
  ETAG("ETag"),
  EXPECT_CT("Expect-CT"),
  FEATURE_POLICY,
  IM("IM"),
  LARGE_ALLOCATION,
  LOCATION,
  NEL("NEL"),
  ORIGIN_ISOLATION,
  PROXY_AUTHENTICATE,
  PUBLIC_KEY_PINS,
  PUBLIC_KEY_PINS_REPORT_ONLY,
  PUSH_POLICY,
  REFERRER_POLICY,
  REFRESH,
  REPORT_TO,
  RETRY_AFTER,
  SEC_WEBSOCKET_ACCEPT,
  SEC_WEBSOCKET_EXTENSIONS,
  SEC_WEBSOCKET_PROTOCOL,
  SERVER,
  SERVER_TIMING,
  SERVICE_WORKER_ALLOWED,
  SET_COOKIE,
  SIGNATURE,
  SIGNED_HEADERS,
  SOURCEMAP("SourceMap", "X-SourceMap"),
  STATUS,
  STRICT_TRANSPORT_SECURITY,
  TIMING_ALLOW_ORIGIN,
  TK,
  VARY,
  WWW_AUTHENTICATE("WWW-Authenticate"),
  X_CONTENT_DURATION,
  X_CONTENT_SECURITY_POLICY,
  X_CONTENT_TYPE_OPTIONS,
  X_DNS_PREFETCH_CONTROL("X-DNS-Prefetch-Control"),
  X_DOWNLOAD_OPTIONS,
  X_FIREFOX_SPDY,
  X_FRAME_OPTIONS,
  X_PERMITTED_CROSS_DOMAIN_POLICIES,
  X_PINGBACK,
  X_POWERED_BY,
  X_ROBOTS_TAG,
  X_UA_COMPATIBLE("X-UA-Compatible"),
  X_WEBKIT_CSP("X-WebKit-CSP"),
  X_XSS_PROTECTION("X-XSS-Protection");

  public final Header.Type type;
  public final String s;
  private final String[] aliases;

  private ResponseHeader() {
    this(null);
  }

  private ResponseHeader(final String s, final String... aliases) {
    this(Header.Type.SINGLE_VALUE, s, aliases);
  }

  private ResponseHeader(final Header.Type type, final String s, final String... aliases) {
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

  public static Optional<ResponseHeader> of(final String s) {
    for (final ResponseHeader h : values()) {
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
