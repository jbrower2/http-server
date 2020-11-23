package com.jeffbrower.http;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.nio.charset.Charset;

public class Response {
  public final Request request;
  public Status status = Status.OK;
  public final Headers headers = new Headers(false);
  public Serializer serializer;
  public Object body;

  Response(final Request request) {
    this.request = request;
  }

  public Response stringBody(final String string) {
    return stringBody(string, UTF_8);
  }

  public Response stringBody(final String string, final Charset charset) {
    headers.entity.add(EntityHeader.CONTENT_TYPE, "text/plain;charset=" + charset.name());
    body = string.getBytes(charset);
    return this;
  }

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
    return serializer.serialize(this);
  }
}
