package com.jeffbrower.http;

public interface ErrorHandler {
  void handle(Request req, Response res, Throwable t);
}
