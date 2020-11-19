package com.jeffbrower.http;

public interface RequestHandler {
  /**
   * Handle a request.
   *
   * @param req The {@link Request HTTP request}.
   * @param res The {@link Response HTTP response}.
   * @return {@code true} if the request has been fully handled.
   */
  boolean handle(Request req, Response res);
}
