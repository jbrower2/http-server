package com.jeffbrower.http;

public class ErrorResponseException extends RuntimeException {
  private static final long serialVersionUID = 1L;

  public final Status status;

  public ErrorResponseException(final Status status, final String message) {
    super(message);
    this.status = status;
  }

  public ErrorResponseException(final Status status, final String message, final Throwable cause) {
    super(message, cause);
    this.status = status;
  }
}
