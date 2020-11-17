package com.jeffbrower.http;

public class ErrorResponseException extends RuntimeException {
  private static final long serialVersionUID = 1L;

  public final Response response;

  public ErrorResponseException(final Response response) {
    this.response = response;
  }

  public ErrorResponseException(final Status status, final String message) {
    super(message);
    response = Response.of(status, message);
  }

  public ErrorResponseException(final Status status, final String message, final Throwable cause) {
    super(message, cause);
    response = Response.of(status, message);
  }
}
