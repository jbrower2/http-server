package com.jeffbrower.http;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.regex.MatchResult;
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

  private static Pattern parsePath(
      final String path, final boolean ignoreCase, final PathMatcher self) {
    // TODO add support for numeric ranges (min, max, step, roundingMode)
    // step enforces that the given value is a multiple of the step, unless roundingMode is set
    // roundingMode rounds to the nearest step, and is one of: round, floor, ceiling
    // if step is left out, it is inferred from min/max
    // if step is "unlimited", arbitrary precision is accepted and no rounding is performed
    // if min/max are left out, they are inferred to be negative/positive infinity (but must be open
    // bound)
    // {param[0,10]}
    // {param(-1.23,4.56)}
    // {param(0,1,0.001]}
    // {param[0,1,unlimited]}
    // {param(,10]}
    // {param[10,)}
    if (!path.startsWith("/")) {
      throw new IllegalArgumentException("Path must start with /");
    }
    int open = path.indexOf('{');
    if (open == -1) {
      return Pattern.compile(Pattern.quote(path), ignoreCase ? Pattern.CASE_INSENSITIVE : 0);
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
          self.names.add(name);
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
            self.names.add(name);
            continue outer;
          }
        }
      }
      throw new IllegalArgumentException("Unclosed path param");
    }
    if (close != path.length()) {
      regex.append(Pattern.quote(path.substring(close)));
    }
    return Pattern.compile(regex.toString(), ignoreCase ? Pattern.CASE_INSENSITIVE : 0);
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
    this.pattern = parsePath(path, ignoreCase, this);
  }

  public PathMatcher(final Pattern pattern) {
    this.pattern = pattern;
  }

  public Optional<MatchResult> startsWith(final Request req) {
    final Matcher m = pattern.matcher(req.url);
    return m.lookingAt() ? Optional.of(m.toMatchResult()) : Optional.empty();
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

  public PathMatcher concat(final PathMatcher that) {
    if (pattern.flags() == that.pattern.flags()) {
      return new PathMatcher(
          Pattern.compile(pattern.pattern() + that.pattern.pattern(), pattern.flags()));
    }
    final StringBuilder b = new StringBuilder();
    if ((pattern.flags() & Pattern.CASE_INSENSITIVE) != 0) {
      b.append("(?i)");
    }
    b.append(pattern.pattern());
    if ((that.pattern.flags() & Pattern.CASE_INSENSITIVE) != 0) {
      b.append("(?i)");
    }
    b.append(that.pattern.pattern());
    return new PathMatcher(Pattern.compile(b.toString()));
  }
}
