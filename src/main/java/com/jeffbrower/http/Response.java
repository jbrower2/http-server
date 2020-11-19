package com.jeffbrower.http;

import static java.nio.charset.StandardCharsets.UTF_8;

public class Response {
  private static final String MIME_TEXT_UTF_8 = "text/plain;charset=utf-8";

  public static Response of(final String body) {
    final Response response = new Response();
    response.headers.entity.add(EntityHeader.CONTENT_TYPE, MIME_TEXT_UTF_8);
    response.body = body.getBytes(UTF_8);
    return response;
  }

  public static Response of(final Status status, final String body) {
    final Response response = new Response();
    response.status = status;
    response.headers.entity.add(EntityHeader.CONTENT_TYPE, MIME_TEXT_UTF_8);
    response.body = body.getBytes(UTF_8);
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
  public final Headers headers = new Headers(false);
  public Serializer serializer;
  public Object body;

  public void reset() {
    status = Status.OK;
    headers.reset();
    body = null;
  }

  public byte[] serializeBody() {
    if (serializer == null) {
      if (body == null || body instanceof byte[]) {
        return (byte[]) body;
      }
      throw new IllegalArgumentException("No serializer specified");
    }
    return serializer.serialize(body);
  }
}
