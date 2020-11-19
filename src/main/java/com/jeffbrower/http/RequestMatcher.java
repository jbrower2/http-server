package com.jeffbrower.http;

public interface RequestMatcher {
  static RequestMatcher all() {
    return req -> true;
  }

  static RequestMatcher none() {
    return req -> false;
  }

  boolean matches(Request req);

  default RequestMatcher negate() {
    return req -> !matches(req);
  }

  default RequestMatcher and(final RequestMatcher that) {
    return req -> matches(req) && that.matches(req);
  }

  default RequestMatcher or(final RequestMatcher that) {
    return req -> matches(req) || that.matches(req);
  }
}
