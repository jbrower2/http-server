package com.jeffbrower.http;

public abstract class Message {
  public final Headers headers;
  public byte[] body;

  Message(final boolean request) {
    headers = new Headers(request);
  }
}
