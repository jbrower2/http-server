package com.jeffbrower.http;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Request extends Message {
  public Method method;
  public String url;
  public final Map<String, String> path = new HashMap<>();
  public final Map<String, List<String>> query = new HashMap<>();

  public Request() {
    super(true);
  }
}
