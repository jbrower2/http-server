package com.jeffbrower.http;

public interface Deserializer {
  <T> T deserialize(byte[] bytes, Class<? extends T> clazz);
}
