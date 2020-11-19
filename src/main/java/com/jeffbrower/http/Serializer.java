package com.jeffbrower.http;

public interface Serializer {
  byte[] serialize(Request req, Response res, Object object);
}
