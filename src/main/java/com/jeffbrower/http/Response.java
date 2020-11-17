package com.jeffbrower.http;

import java.nio.charset.StandardCharsets;

public class Response extends Message {
  private static final String MIME_TEXT_UTF_8 = "text/plain;charset=utf-8";

  public static Response of(final String body) {
    final Response response = new Response();
    response.headers.entity.add(EntityHeader.CONTENT_TYPE, MIME_TEXT_UTF_8);
    response.body = body.getBytes(StandardCharsets.UTF_8);
    return response;
  }

  public static Response of(final Status status, final String body) {
    final Response response = new Response();
    response.status = status;
    response.headers.entity.add(EntityHeader.CONTENT_TYPE, MIME_TEXT_UTF_8);
    response.body = body.getBytes(StandardCharsets.UTF_8);
    return response;
  }

  public static Response of(final String mime, final byte[] body) {
    final Response response = new Response();
    response.headers.entity.add(EntityHeader.CONTENT_TYPE, mime);
    response.body = body;
    return response;
  }

  public static Response of(final Status status, final String mime, final byte[] body) {
    final Response response = new Response();
    response.status = status;
    response.headers.entity.add(EntityHeader.CONTENT_TYPE, mime);
    response.body = body;
    return response;
  }

  public Status status = Status.OK;

  public Response() {
    super(false);
  }
}
