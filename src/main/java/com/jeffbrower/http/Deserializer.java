package com.jeffbrower.http;

public interface Deserializer {
  <T> T deserialize(Request req, Class<? extends T> clazz);
}
