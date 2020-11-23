package com.jeffbrower.http;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public interface Header {
  Pattern COMMA = Pattern.compile(",");
  Pattern SEMICOLON = Pattern.compile(";");

  static String combineMultiple0(
      final String header, final Header.Type type, final String existing, final String newValue) {
    switch (type) {
      case COMMA_SEPARATED:
        return existing + ',' + newValue;
      default:
        throw Status.BAD_REQUEST.exception("Duplicate header: " + header);
    }
  }

  static WithParams[] parseWithParams(final String header) {
    final String[] parts = COMMA.split(header);
    final WithParams[] headers = new WithParams[parts.length];
    for (int i = 0; i < parts.length; i++) {
      final String[] params = SEMICOLON.split(parts[i].trim());
      final Map<String, String> map = new HashMap<>();
      for (int j = 1; j < params.length; j++) {
        final String s = params[j].trim();
        final int eq = s.indexOf('=');
        if (eq == -1) {
          throw Status.BAD_REQUEST.exception("Malformed header params: " + header);
        }
        map.put(s.substring(0, eq).trim(), s.substring(eq + 1).trim());
      }
      headers[i] = new WithParams(params[0].trim(), map);
    }
    return headers;
  }

  public static final class WithParams {
    public final String value;
    public final Map<String, String> params;

    public WithParams(final String value, final Map<String, String> params) {
      this.value = value;
      this.params = params;
    }
  }

  Type type();

  default String combineMultiple(final String existing, final String newValue) {
    return combineMultiple0(toString(), type(), existing, newValue);
  }

  public enum Type {
    SINGLE_VALUE,
    COMMA_SEPARATED
  }
}
