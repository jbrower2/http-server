package com.jeffbrower.http;

public class BadRequestException extends ErrorResponseException {
  private static final long serialVersionUID = 1L;

  public BadRequestException(final String message) {
    super(Status.BAD_REQUEST, message);
  }

  public BadRequestException(final String message, final Throwable cause) {
    super(Status.BAD_REQUEST, message, cause);
  }
}
