package com.jeffbrower.http;

import java.util.Objects;

public interface CaseUtil {
  static String upperUnderscoreToTitleKebab(final String s) {
    final StringBuilder b = new StringBuilder(s);
    for (int i = 1; i < b.length(); i++) {
      final char c = b.charAt(i);
      if (c == '_') {
        b.setCharAt(i++, '-');
      } else {
        b.setCharAt(i, Character.toLowerCase(c));
      }
    }
    return b.toString();
  }

  static String upperUnderscoreToTitleSpace(final String s) {
    final StringBuilder b = new StringBuilder(s);
    for (int i = 1; i < b.length(); i++) {
      final char c = b.charAt(i);
      if (c == '_') {
        b.setCharAt(i++, ' ');
      } else {
        b.setCharAt(i, Character.toLowerCase(c));
      }
    }
    return b.toString();
  }

  String LS = System.lineSeparator();
  String LST = LS + '\t';

  static String indent(final Object o) {
    return Objects.toString(o).replace(LS, LST);
  }
}
