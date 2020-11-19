package com.jeffbrower.http;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class PathMatcher implements RequestMatcher {
  private static final Pattern NAME_VALIDATOR = Pattern.compile("\\w+", Pattern.CASE_INSENSITIVE);

  public static RequestMatcher of(final String path) {
    return new PathMatcher(path, false);
  }

  public static RequestMatcher ofIgnoreCase(final String path) {
    return new PathMatcher(path, true);
  }

  private static boolean validRegex(final String regex) {
    try {
      Pattern.compile(regex);
    } catch (final PatternSyntaxException e) {
      return false;
    }
    return true;
  }

  private static String validateName(final String name) {
    if (!NAME_VALIDATOR.matcher(name).matches()) {
      throw new IllegalArgumentException("Unexpected path variable name: " + name);
    }
    return name;
  }

  private final Set<String> names = new HashSet<>();
  public final Pattern pattern;

  public PathMatcher(final String path, final boolean ignoreCase) {
    if (!path.startsWith("/")) {
      throw new IllegalArgumentException("Path must start with /");
    }
    int open = path.indexOf('{');
    if (open == -1) {
      pattern = Pattern.compile(Pattern.quote(path), ignoreCase ? Pattern.CASE_INSENSITIVE : 0);
      return;
    }
    final StringBuilder regex = new StringBuilder();
    int close = 0;
    outer:
    for (int colon; open != -1; open = path.indexOf('{', ++close)) {
      if (open > close) {
        regex.append(Pattern.quote(path.substring(close, open)));
      }
      colon = -1;
      for (int i = open + 1; i < path.length(); i++) {
        final char c = path.charAt(i);
        if (c == '}') {
          final String name = validateName(path.substring(open + 1, close = i));
          regex.append("(?<" + name + ">[^/]+?)");
          names.add(name);
          continue outer;
        }
        if (c == ':') {
          colon = i;
          break;
        }
      }
      if (colon != -1) {
        for (close = path.indexOf('}', colon + 1);
            close != -1;
            close = path.indexOf('}', close + 1)) {
          final String part = path.substring(colon + 1, close);
          if (validRegex(part)) {
            final String name = validateName(path.substring(open + 1, colon));
            regex.append("(?<" + name + ">" + part + ")");
            names.add(name);
            continue outer;
          }
        }
      }
      throw new IllegalArgumentException("Unclosed path param");
    }
    if (close != path.length()) {
      regex.append(Pattern.quote(path.substring(close)));
    }
    pattern = Pattern.compile(regex.toString(), ignoreCase ? Pattern.CASE_INSENSITIVE : 0);
  }

  @Override
  public boolean matches(final Request req) {
    final Matcher m = pattern.matcher(req.url);
    if (!m.matches()) {
      return false;
    }
    names.forEach(name -> req.pathParams.put(name, m.group(name)));
    return true;
  }
}
