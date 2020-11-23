package com.jeffbrower.http;

public enum Status {
  // 1xx Informational
  CONTINUE(100),
  SWITCHING_PROTOCOLS(101),
  PROCESSING(102),
  EARLY_HINTS(103),
  CHECKPOINT(103),
  REQUEST_URI_TOO_LONG(122, "Request-URI Too Long"),

  // 2xx Success
  OK(200, "OK"),
  CREATED(201),
  ACCEPTED(202),
  NON_AUTHORITATIVE_INFORMATION(203, "Non-Authoritative Information"),
  NO_CONTENT(204),
  RESET_CONTENT(205),
  PARTIAL_CONTENT(206),
  MULTI_STATUS(207, "Multi-Status"),
  ALREADY_REPORTED(208),
  THIS_IS_FINE(218),
  IM_USED(226, "IM Used"),

  // 3xx Redirection
  MULTIPLE_CHOICES(300),
  MOVED_PERMANENTLY(301),
  FOUND(302),
  MOVED_TEMPORARILY(302),
  SEE_OTHER(303),
  NOT_MODIFIED(304),
  USE_PROXY(305),
  SWITCH_PROXY(306),
  TEMPORARY_REDIRECT(307),
  PERMANENT_REDIRECT(308),

  // 4xx Client Error
  BAD_REQUEST(400),
  UNAUTHORIZED(401),
  PAYMENT_REQUIRED(402),
  FORBIDDEN(403),
  NOT_FOUND(404),
  METHOD_NOT_ALLOWED(405),
  NOT_ACCEPTABLE(406),
  PROXY_AUTHENTICATION_REQUIRED(407),
  REQUEST_TIMEOUT(408),
  CONFLICT(409),
  GONE(410),
  LENGTH_REQUIRED(411),
  PRECONDITION_FAILED(412),
  PAYLOAD_TOO_LARGE(413),
  URI_TOO_LONG(414, "URI Too Long"),
  UNSUPPORTED_MEDIA_TYPE(415),
  RANGE_NOT_SATISFIABLE(416),
  REQUESTED_RANGE_NOT_SATISFIABLE(416),
  EXPECTATION_FAILED(417),
  I_AM_A_TEAPOT(418, "I'm a teapot"),
  PAGE_EXPIRED(419),
  METHOD_FAILURE(420),
  ENHANCE_YOUR_CALM(420),
  MISDIRECTED_REQUEST(421),
  UNPROCESSABLE_ENTITY(422),
  LOCKED(423),
  FAILED_DEPENDENCY(424),
  TOO_EARLY(425),
  UPGRADE_REQUIRED(426),
  PRECONDITION_REQUIRED(428),
  TOO_MANY_REQUESTS(429),
  REQUEST_HEADER_FIELDS_TOO_LARGE_430(430, "Request Header Fields Too Large"),
  REQUEST_HEADER_FIELDS_TOO_LARGE(431),
  LOGIN_TIME_OUT(440, "Login Time-out"),
  NO_RESPONSE(444),
  RETRY_WITH(449),
  BLOCKED_BY_WINDOWS_PARENTAL_CONTROLS(450),
  UNAVAILABLE_FOR_LEGAL_REASONS(451),
  REDIRECT(451),
  WRONG_EXCHANGE_SERVER(451),
  REQUEST_HEADER_TOO_LARGE(494),
  SSL_CERTIFICATE_ERROR(495, "SSL Certificate Error"),
  SSL_CERTIFICATE_REQUIRED(496, "SSL Certificate Required"),
  HTTP_REQUEST_SENT_TO_HTTPS_PORT(497, "HTTP Request Sent to HTTPS Port"),
  INVALID_TOKEN(498),
  TOKEN_REQUIRED(499),
  CLIENT_CLOSED_REQUEST(499),

  // 5xx Server Error
  INTERNAL_SERVER_ERROR(500),
  NOT_IMPLEMENTED(501),
  BAD_GATEWAY(502),
  SERVICE_UNAVAILABLE(503),
  GATEWAY_TIMEOUT(504),
  HTTP_VERSION_NOT_SUPPORTED(505, "HTTP Version Not supported"),
  VARIANT_ALSO_NEGOTIATES(506),
  INSUFFICIENT_STORAGE(507),
  LOOP_DETECTED(508),
  BANDWIDTH_LIMIT_EXCEEDED(509),
  NOT_EXTENDED(510),
  NETWORK_AUTHENTICATION_REQUIRED(511),
  WEB_SERVER_RETURNED_AN_UNKNOWN_ERROR(520),
  WEB_SERVER_IS_DOWN(521),
  CONNECTION_TIMED_OUT(522),
  ORIGIN_IS_UNREACHABLE(523),
  A_TIMEOUT_OCCURRED(524),
  SSL_HANDSHAKE_FAILED(525, "SSL Handshake Failed"),
  INVALID_SSL_CERTIFICATE(526, "Invalid SSL Certificate"),
  RAILGUN_ERROR(527),
  SITE_IS_OVERLOADED(529),
  SITE_IS_FROZEN(530),
  CLOUDFLARE_1XXX_ERROR(530, "Cloudflare 1XXX Error"),
  NETWORK_READ_TIMEOUT_ERROR(598),
  NETWORK_CONNECT_TIMEOUT_ERROR(599);

  public static final int MAX_LENGTH;

  static {
    int max = -1;
    for (final Status value : values()) {
      max = Math.max(max, value.reasonPhrase.length());
    }
    MAX_LENGTH = max;
  }

  public final int statusCode;
  public final String reasonPhrase;
  public final Class statusClass;

  private Status(final int statusCode) {
    this(statusCode, null);
  }

  private Status(final int statusCode, final String reasonPhrase) {
    this.statusCode = statusCode;
    this.reasonPhrase =
        reasonPhrase == null ? CaseUtil.upperUnderscoreToTitleSpace(name()) : reasonPhrase;
    statusClass = Class.valueOf(statusCode);
  }

  public ErrorResponseException exception(final String message) {
    return new ErrorResponseException(this, message);
  }

  public ErrorResponseException exception(final String message, final Throwable cause) {
    return new ErrorResponseException(this, message, cause);
  }

  @Override
  public String toString() {
    return statusCode + " " + reasonPhrase;
  }

  public enum Class {
    INFORMATIONAL(1),
    SUCCESSFUL(2),
    REDIRECTION(3),
    CLIENT_ERROR(4),
    SERVER_ERROR(5);

    public final int value;

    private Class(final int value) {
      this.value = value;
    }

    public static Class valueOf(final int statusCode) {
      final int seriesCode = statusCode / 100;
      for (final Class series : values()) {
        if (series.value == seriesCode) {
          return series;
        }
      }
      throw new IllegalArgumentException("No matching constant for [" + statusCode + "]");
    }
  }
}
