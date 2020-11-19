package com.jeffbrower.http;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Request {
  public Method method;
  public String url;
  public final Map<String, String> pathParams = new HashMap<>();
  public final Map<String, List<String>> queryParams = new HashMap<>();
  public final Headers headers = new Headers(true);
  public Deserializer deserializer;
  public byte[] body;

  public <T> T getBody(final Class<? extends T> clazz) {
    if (deserializer == null) {
      if (body == null || clazz == byte[].class) {
        return (T) body;
      }
      throw new IllegalArgumentException("No deserializer specified");
    }
    return deserializer.deserialize(body, clazz);
  }
}
