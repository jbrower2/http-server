package com.jeffbrower.http;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.function.Supplier;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class PathMatcherTest {
  private static Request req(final String url) {
    if (!url.startsWith("/")) {
      throw new IllegalArgumentException("URL must start with /");
    }
    final Request req = new Request();
    req.url = url.startsWith("/") ? url : "/" + url;
    return req;
  }

  private static final String[] ALL_TESTS = {
    "/",
    "//",
    "/test",
    "/test/",
    "/test//",
    "//test",
    "//test/",
    "//test//",
    "/test/test2",
    "/test/test2/",
    "/test/TEST2",
    "/test/TEST2/",
    "/TEST/test2",
    "/TEST/test2/",
    "/123",
    "/123/",
    "/123/test2",
    "/123/test2/",
    "/test/123",
    "/test/123/",
    "/123/456",
    "/123/456/"
  };

  private static final String varyCase(final String s) {
    final StringBuilder b = new StringBuilder(s.length());
    boolean even = true;
    for (int i = 0, c; i < s.length(); i += Character.charCount(c), even = !even) {
      c = s.codePointAt(i);
      b.appendCodePoint(even ? Character.toUpperCase(c) : Character.toLowerCase(c));
    }
    return b.toString();
  }

  private static boolean equalsAny(final String test, final String... success) {
    for (final String succes : success) {
      if (succes.equals(test)) {
        return true;
      }
    }
    return false;
  }

  private static boolean equalsAnyIgnoreCase(final String test, final String... success) {
    for (final String succes : success) {
      if (succes.equalsIgnoreCase(test)) {
        return true;
      }
    }
    return false;
  }

  private interface Match {
    void _test(String test, boolean ignoreCase, boolean matches);

    default void test(final String test, final boolean matches, final boolean matchesCI) {
      _test(test, false, matches);
      _test(test, true, matchesCI);
    }
  }

  public static Stream<Arguments> test() {
    class Helper {
      final Arguments a(final String path, final String... success) {
        return Arguments.of(path, success);
      }
    }
    final Helper x = new Helper();
    return Stream.of(
        x.a("/", "/"),
        x.a("//", "//"),
        x.a("/test", "/test"),
        x.a("/test/", "/test/"),
        x.a("/test//", "/test//"),
        x.a("//test", "//test"),
        x.a("//test/", "//test/"),
        x.a("//test//", "//test//"),
        x.a("/test/test2", "/test/test2"),
        x.a("/test/test2/", "/test/test2/"),
        x.a("/{param}", "/test", "/TEST", varyCase("/test"), "/123"),
        x.a("/{param}/", "/test/", "/TEST/", varyCase("/test/"), "/123/"),
        x.a("/{param:[a-z]+}", "/test"),
        x.a("/{param:[a-z]+}/", "/test/"),
        x.a("/{param:[A-Z]+}", "/TEST"),
        x.a("/{param:[A-Z]+}/", "/TEST/"),
        x.a("/{param:\\d+}", "/123"),
        x.a("/{param:\\d+}/", "/123/"),
        x.a("/test/{param}", "/test/test2", "/test/TEST2", "/test/123"),
        x.a("/test/{param}/", "/test/test2/", "/test/TEST2/", "/test/123/"),
        x.a("/test/{param:[a-z]+2}", "/test/test2"),
        x.a("/test/{param:[a-z]+2}/", "/test/test2/"),
        x.a("/test/{param:[A-Z]+2}", "/test/TEST2"),
        x.a("/test/{param:[A-Z]+2}/", "/test/TEST2/"),
        x.a("/test/{param:\\d+}", "/test/123"),
        x.a("/test/{param:\\d+}/", "/test/123/"),
        x.a("/{param}/test2", "/test/test2", "/TEST/test2", "/123/test2"),
        x.a("/{param}/test2/", "/test/test2/", "/TEST/test2/", "/123/test2/"),
        x.a("/{param:[a-z]+}/test2", "/test/test2"),
        x.a("/{param:[a-z]+}/test2/", "/test/test2/"),
        x.a("/{param:[A-Z]+}/test2", "/TEST/test2"),
        x.a("/{param:[A-Z]+}/test2/", "/TEST/test2/"),
        x.a("/{param:\\d+}/test2", "/123/test2"),
        x.a("/{param:\\d+}/test2/", "/123/test2/"),
        x.a(
            "/{param1}/{param2}",
            "/test/test2",
            "/test/TEST2",
            "/TEST/test2",
            "/TEST/TEST2",
            varyCase("/test/test2"),
            "/123/test2",
            "/123/TEST2",
            varyCase("/123/test2"),
            "/test/123",
            "/TEST/123",
            varyCase("/test/123"),
            "/123/456"),
        x.a(
            "/{param1}/{param2}/",
            "/test/test2/",
            "/test/TEST2/",
            "/TEST/test2/",
            "/TEST/TEST2/",
            varyCase("/test/test2/"),
            "/123/test2/",
            "/123/TEST2/",
            varyCase("/123/test2/"),
            "/test/123/",
            "/TEST/123/",
            varyCase("/test/123/"),
            "/123/456/"),
        x.a("/{param1}/{param2:[a-z]+2}", "/test/test2", "/TEST/test2", "/123/test2"),
        x.a("/{param1}/{param2:[a-z]+2}/", "/test/test2/", "/TEST/test2/", "/123/test2/"),
        x.a("/{param1}/{param2:[A-Z]+2}", "/test/TEST2", "/TEST/TEST2", "/123/TEST2"),
        x.a("/{param1}/{param2:[A-Z]+2}/", "/test/TEST2/", "/TEST/TEST2/", "/123/TEST2/"),
        x.a("/{param1}/{param2:\\d+}", "/test/123", "/TEST/123", varyCase("/test/123"), "/123/456"),
        x.a(
            "/{param1}/{param2:\\d+}/",
            "/test/123/",
            "/TEST/123/",
            varyCase("/test/123/"),
            "/123/456/"),
        x.a("/{param1:[a-z]+}/{param2}", "/test/test2", "/test/TEST2", "/test/123"),
        x.a("/{param1:[a-z]+}/{param2}/", "/test/test2/", "/test/TEST2/", "/test/123/"),
        x.a("/{param1:[a-z]+}/{param2:[a-z]+2}", "/test/test2"),
        x.a("/{param1:[a-z]+}/{param2:[a-z]+2}/", "/test/test2/"),
        x.a("/{param1:[a-z]+}/{param2:[A-Z]+2}", "/test/TEST2"),
        x.a("/{param1:[a-z]+}/{param2:[A-Z]+2}/", "/test/TEST2/"),
        x.a("/{param1:[a-z]+}/{param2:\\d+}", "/test/123"),
        x.a("/{param1:[a-z]+}/{param2:\\d+}/", "/test/123/"),
        x.a("/{param1:[A-Z]+}/{param2}", "/TEST/test2", "/TEST/TEST2", "/TEST/123"),
        x.a("/{param1:[A-Z]+}/{param2}/", "/TEST/test2/", "/TEST/TEST2/", "/TEST/123/"),
        x.a("/{param1:[A-Z]+}/{param2:[a-z]+2}", "/TEST/test2"),
        x.a("/{param1:[A-Z]+}/{param2:[a-z]+2}/", "/TEST/test2/"),
        x.a("/{param1:[A-Z]+}/{param2:[A-Z]+2}", "/TEST/TEST2"),
        x.a("/{param1:[A-Z]+}/{param2:[A-Z]+2}/", "/TEST/TEST2/"),
        x.a("/{param1:[A-Z]+}/{param2:\\d+}", "/TEST/123"),
        x.a("/{param1:[A-Z]+}/{param2:\\d+}/", "/TEST/123/"),
        x.a(
            "/{param1:\\d+}/{param2}",
            "/123/test2",
            "/123/TEST2",
            varyCase("/123/test2"),
            "/123/456"),
        x.a(
            "/{param1:\\d+}/{param2}/",
            "/123/test2/",
            "/123/TEST2/",
            varyCase("/123/test2/"),
            "/123/456/"),
        x.a("/{param1:\\d+}/{param2:[a-z]+2}", "/123/test2"),
        x.a("/{param1:\\d+}/{param2:[a-z]+2}/", "/123/test2/"),
        x.a("/{param1:\\d+}/{param2:[A-Z]+2}", "/123/TEST2"),
        x.a("/{param1:\\d+}/{param2:[A-Z]+2}/", "/123/TEST2/"),
        x.a("/{param1:\\d+}/{param2:\\d+}", "/123/456"),
        x.a("/{param1:\\d+}/{param2:\\d+}/", "/123/456/"),
        x.a(
            "/{param:.*}",
            "/",
            "//",
            "/test",
            "/TEST",
            varyCase("/test"),
            "/test/",
            "/TEST/",
            varyCase("/test/"),
            "/test//",
            "/TEST//",
            varyCase("/test//"),
            "//test",
            "//TEST",
            varyCase("//test"),
            "//test/",
            "//TEST/",
            varyCase("//test/"),
            "//test//",
            "//TEST//",
            varyCase("//test//"),
            "/test/test2",
            "/test/TEST2",
            "/TEST/test2",
            "/TEST/TEST2",
            varyCase("/test/test2"),
            "/test/test2/",
            "/test/TEST2/",
            "/TEST/test2/",
            "/TEST/TEST2/",
            varyCase("/test/test2/"),
            "/123",
            "/123/",
            "/123/test2",
            "/123/TEST2",
            varyCase("/123/test2"),
            "/123/test2/",
            "/123/TEST2/",
            varyCase("/123/test2/"),
            "/test/123",
            "/TEST/123",
            varyCase("/test/123"),
            "/test/123/",
            "/TEST/123/",
            varyCase("/test/123/"),
            "/123/456",
            "/123/456/"));
  }

  @ParameterizedTest
  @MethodSource
  public void test(final String path, final String[] success) {
    final PathMatcher cs = new PathMatcher(path, false);
    final PathMatcher ci = new PathMatcher(path, true);

    final Match match =
        (test, ignoreCase, matches) -> {
          final Supplier<String> getMessage =
              () ->
                  new StringBuilder()
                      .append("Expected '")
                      .append(path)
                      .append(matches ? "' to match '" : "' NOT to match '")
                      .append(test)
                      .append(ignoreCase ? "' case-insensitively" : "' case-sensitively")
                      .toString();
          final boolean result = (ignoreCase ? ci : cs).matches(req(test));
          if (matches) {
            assertTrue(result, getMessage);
          } else {
            assertFalse(result, getMessage);
          }
        };

    for (final String lower : ALL_TESTS) {
      if (!equalsAnyIgnoreCase(lower, success)) {
        match.test(lower, false, false);
        continue;
      }

      match.test(lower, equalsAny(lower, success), true);

      final String upper = lower.toUpperCase();
      if (!upper.equals(lower)) {
        match.test(upper, equalsAny(upper, success), true);

        final String varied = varyCase(lower);
        match.test(varied, equalsAny(varied, success), true);
      }
    }
  }
}
