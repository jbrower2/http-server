package com.jeffbrower.http;

import java.util.EnumMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Stream;

public final class Headers implements Iterable<Map.Entry<String, String>> {
  public final GeneralHeaders general;
  public final RequestHeaders request;
  public final ResponseHeaders response;
  public final EntityHeaders entity;
  public final Map<String, String> additional;

  Headers(final boolean request) {
    general = new GeneralHeaders();
    this.request = new RequestHeaders(request);
    response = new ResponseHeaders(!request);
    entity = new EntityHeaders();
    additional = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
  }

  public void reset() {
    general.reset();
    request.reset();
    response.reset();
    entity.reset();
    additional.clear();
  }

  public Optional<String> get(final String key) {
    final Optional<ResponseHeader> res = ResponseHeader.of(key);
    if (res.isPresent()) {
      return response.get(res.get());
    }

    final Optional<RequestHeader> req = RequestHeader.of(key);
    if (req.isPresent()) {
      return request.get(req.get());
    }

    final Optional<GeneralHeader> g = GeneralHeader.of(key);
    if (g.isPresent()) {
      return general.get(g.get());
    }

    final Optional<EntityHeader> e = EntityHeader.of(key);
    if (e.isPresent()) {
      return entity.get(e.get());
    }

    return Optional.ofNullable(additional.get(key));
  }

  public Optional<String> replace(final String key, final String value) {
    final Optional<ResponseHeader> res = ResponseHeader.of(key);
    if (res.isPresent()) {
      return response.replace(res.get(), value);
    }

    final Optional<RequestHeader> req = RequestHeader.of(key);
    if (req.isPresent()) {
      return request.replace(req.get(), value);
    }

    final Optional<GeneralHeader> g = GeneralHeader.of(key);
    if (g.isPresent()) {
      return general.replace(g.get(), value);
    }

    final Optional<EntityHeader> e = EntityHeader.of(key);
    if (e.isPresent()) {
      return entity.replace(e.get(), value);
    }

    return Optional.ofNullable(additional.put(key, value));
  }

  public Optional<String> add(final String key, final String value) {
    final Optional<ResponseHeader> res = ResponseHeader.of(key);
    if (res.isPresent()) {
      return response.add(res.get(), value);
    }

    final Optional<RequestHeader> req = RequestHeader.of(key);
    if (req.isPresent()) {
      return request.add(req.get(), value);
    }

    final Optional<GeneralHeader> g = GeneralHeader.of(key);
    if (g.isPresent()) {
      return general.add(g.get(), value);
    }

    final Optional<EntityHeader> e = EntityHeader.of(key);
    if (e.isPresent()) {
      return entity.add(e.get(), value);
    }

    return Optional.ofNullable(
        additional.merge(
            key, value, (a, b) -> Header.combineMultiple0(key, Header.Type.SINGLE_VALUE, a, b)));
  }

  @Override
  public Iterator<Map.Entry<String, String>> iterator() {
    return Stream.concat(
            general.stringStream(),
            Stream.concat(
                Stream.concat(request.stringStream(), response.stringStream()),
                Stream.concat(entity.stringStream(), additional.entrySet().stream())))
        .iterator();
  }

  @Override
  public String toString() {
    final StringBuilder b = new StringBuilder().append('[').append(System.lineSeparator());
    if (!general.isEmpty()) {
      b.append("\tgeneral: ").append(CaseUtil.indent(general)).append(System.lineSeparator());
    }
    if (!request.isEmpty()) {
      b.append("\trequest: ").append(CaseUtil.indent(request)).append(System.lineSeparator());
    }
    if (!response.isEmpty()) {
      b.append("\tresponse: ").append(CaseUtil.indent(response)).append(System.lineSeparator());
    }
    if (!entity.isEmpty()) {
      b.append("\tentity: ").append(CaseUtil.indent(entity)).append(System.lineSeparator());
    }
    if (!additional.isEmpty()) {
      b.append("\tadditional: ").append(CaseUtil.indent(additional)).append(System.lineSeparator());
    }
    return b.append(']').toString();
  }

  public static final class GeneralHeaders extends EnumHeaders<GeneralHeader> {
    private GeneralHeaders() {
      super(GeneralHeader.class);
    }
  }

  public static final class RequestHeaders extends EnumHeaders<RequestHeader> {
    private RequestHeaders(final boolean allow) {
      super(allow ? RequestHeader.class : null);
    }
  }

  public static final class ResponseHeaders extends EnumHeaders<ResponseHeader> {
    private ResponseHeaders(final boolean allow) {
      super(allow ? ResponseHeader.class : null);
    }
  }

  public static final class EntityHeaders extends EnumHeaders<EntityHeader> {
    private EntityHeaders() {
      super(EntityHeader.class);
    }
  }

  public abstract static class EnumHeaders<K extends Enum<K> & Header>
      implements Iterable<Map.Entry<K, String>> {
    private final EnumMap<K, String> map;

    private EnumHeaders(final Class<K> keyClass) {
      this.map = keyClass == null ? null : new EnumMap<>(keyClass);
    }

    public void reset() {
      if (map != null) {
        map.clear();
      }
    }

    public boolean isEmpty() {
      return map == null || map.isEmpty();
    }

    public boolean contains(final K key) {
      return map != null && map.containsKey(key);
    }

    public Optional<String> get(final K key) {
      return map == null ? Optional.empty() : Optional.ofNullable(map.get(key));
    }

    public Optional<String> replace(final K key, final String value) {
      if (map == null) {
        throw Status.BAD_REQUEST.exception("Header '" + key + "' not allowed");
      }
      return Optional.ofNullable(map.put(key, value));
    }

    public Optional<String> add(final K key, final String value) {
      if (map == null) {
        throw Status.BAD_REQUEST.exception("Header '" + key + "' not allowed");
      }
      return Optional.ofNullable(
          map.merge(key, value, (existing, newValue) -> key.combineMultiple(existing, newValue)));
    }

    public Set<Map.Entry<K, String>> asSet() {
      return map == null ? Set.of() : map.entrySet();
    }

    Stream<Map.Entry<String, String>> stringStream() {
      return asSet().stream().map(e -> Map.entry(e.getKey().toString(), e.getValue()));
    }

    @Override
    public Iterator<Map.Entry<K, String>> iterator() {
      return asSet().iterator();
    }

    @Override
    public String toString() {
      final StringBuilder b = new StringBuilder().append('[').append(System.lineSeparator());
      map.forEach(
          (k, v) -> b.append('\t').append(k).append(": ").append(v).append(System.lineSeparator()));
      return b.append(']').toString();
    }
  }
}
