package com.jeffbrower.http;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;

public abstract class Server implements Runnable {
  private static final String CRLF = "\r\n";

  private static final Pattern FIRST_LINE =
      Pattern.compile("(\\w+)[ \t]+(\\S+)[ \t]+HTTP/(\\d)\\.(\\d)");
  private static final Pattern TOKEN = Pattern.compile("[0-9A-Za-z!#$%&'*+.^_`|~-]+");

  private static final java.text.SimpleDateFormat GMT_FORMAT;

  static {
    GMT_FORMAT = new java.text.SimpleDateFormat("E, d MMM yyyy HH:mm:ss 'GMT'", Locale.US);
    GMT_FORMAT.setTimeZone(TimeZone.getTimeZone("GMT"));
  }

  private final Charset urlCharset;

  public Server() {
    this(StandardCharsets.UTF_8);
  }

  public Server(final Charset urlCharset) {
    this.urlCharset = urlCharset;
  }

  public abstract Response process(Request request);

  @Override
  public final void run() {
    try (ServerSocket myServerSocket = new ServerSocket(80)) {
      System.out.println("Server started...");
      while (true) {
        try (Socket mySocket = myServerSocket.accept();
            InputStream is = mySocket.getInputStream();
            OutputStream os = mySocket.getOutputStream()) {
          // process request
          Response response = null;
          try {
            final Request request = buildRequest(is);
            response = process(request);
          } catch (final ErrorResponseException e) {
            e.printStackTrace();
            response = e.response;
          } catch (final Exception e) {
            e.printStackTrace();
            response = Response.of(Status.INTERNAL_SERVER_ERROR, e.getMessage());
          }

          // set date header, if not already set
          if (!response.headers.general.contains(GeneralHeader.DATE)) {
            response.headers.general.add(GeneralHeader.DATE, GMT_FORMAT.format(new Date()));
          }

          // build response
          final StringBuilder b = new StringBuilder();
          b.append("HTTP/1.1 ")
              .append(response.status.statusCode)
              .append(' ')
              .append(response.status.reasonPhrase)
              .append(CRLF);
          for (final Map.Entry<String, String> e : response.headers) {
            b.append(e.getKey()).append(": ").append(e.getValue()).append(CRLF);
          }
          b.append(CRLF);

          // write to stream
          os.write(StandardCharsets.US_ASCII.newEncoder().encode(CharBuffer.wrap(b)).array());
          if (response.body != null) {
            os.write(response.body);
          }
        } catch (final IOException e) {
          e.printStackTrace();
        }
      }
    } catch (final IOException e) {
      e.printStackTrace();
    }
  }

  private Request buildRequest(final InputStream is) throws IOException {
    final Request request = new Request();
    parseHeaders(is, request);

    if (request.method == null) {
      throw new BadRequestException("Empty request");
    }

    // get message body, if present
    final Optional<String> transferEncoding =
        request.headers.general.get(GeneralHeader.TRANSFER_ENCODING);
    final Optional<String> contentLength = request.headers.entity.get(EntityHeader.CONTENT_LENGTH);
    if (transferEncoding.isPresent()) {
      if (contentLength.isPresent()) {
        throw new BadRequestException("Cannot send both Transfer-Encoding and Content-Length");
      }

      // split list of encodings
      final Header.WithParams[] encs = Header.parseWithParams(transferEncoding.get());
      final Header.WithParams lastEnc = encs[encs.length - 1];
      if (!"chunked".equalsIgnoreCase(lastEnc.value)) {
        throw new BadRequestException("Last Transfer-Encoding of request must be 'chunked'");
      }
      if (!lastEnc.params.isEmpty()) {
        throw new BadRequestException("Transfer-Encoding value cannot have params");
      }

      // read chunked body
      final ByteArrayOutputStream os = new ByteArrayOutputStream();
      for (byte[] chunk = readChunk(is); chunk != null; chunk = readChunk(is)) {
        os.write(chunk);
      }
      request.body = os.toByteArray();

      // read trailers
      parseHeaders(is, request);

      // apply subsequent encodings
      for (int i = encs.length - 2; i >= 0; i--) {
        final Header.WithParams enc = encs[i];
        if (!enc.params.isEmpty()) {
          throw new BadRequestException("Transfer-Encoding value cannot have params");
        }
        if ("chunked".equalsIgnoreCase(enc.value)) {
          throw new BadRequestException("Duplicate Transfer-Encoding 'chunked'");
        } else if ("deflate".equalsIgnoreCase(enc.value)) {
          request.body =
              new InflaterInputStream(new ByteArrayInputStream(request.body)).readAllBytes();
        } else if ("gzip".equalsIgnoreCase(enc.value) || "x-gzip".equalsIgnoreCase(enc.value)) {
          request.body = new GZIPInputStream(new ByteArrayInputStream(request.body)).readAllBytes();
        } else if ("br".equalsIgnoreCase(enc.value)) {
          throw new ErrorResponseException(
              Status.NOT_IMPLEMENTED, "'br' Transfer-Encoding is not supported");
        } else if ("compress".equalsIgnoreCase(enc.value)
            || "x-compress".equalsIgnoreCase(enc.value)) {
          throw new ErrorResponseException(
              Status.NOT_IMPLEMENTED, "'compress' Transfer-Encoding is not supported");
        } else {
          throw new ErrorResponseException(
              Status.NOT_IMPLEMENTED, "Transfer-Encoding not supported: '" + enc + "'");
        }
      }
    } else if (contentLength.isPresent()) {
      final int size = Integer.parseInt(contentLength.get());
      if (size < 0) {
        throw new BadRequestException("Negative Content-Length: " + size);
      }
      if (size != 0) {
        request.body = is.readNBytes(size);
        if (request.body.length < size) {
          throw new BadRequestException(
              "Only received partial request: " + request.body.length + " / " + size);
        }
      }
    }

    return request;
  }

  private enum HeaderParseState {
    NONE,
    CR,
    CR_LF,
    CR_LF_CR
  }

  private void parseHeaders(final InputStream is, final Request request) throws IOException {
    final StringBuilder headerLine = new StringBuilder();

    final Runnable addHeader =
        () -> {
          if (headerLine.length() == 0) {
            return;
          }

          final String line = headerLine.toString();
          headerLine.setLength(0);

          if (request.method != null) {
            final int colon = line.indexOf(":");
            if (colon == -1) {
              throw new BadRequestException("Malformed header: " + line);
            }

            final String key = line.substring(0, colon);
            if (!TOKEN.matcher(key).matches()) {
              throw new BadRequestException("Malformed header field: " + key);
            }

            request.headers.add(key, line.substring(colon + 1).trim());
            return;
          }

          final Matcher m = FIRST_LINE.matcher(line);
          if (!m.matches()) {
            throw new BadRequestException("Malformed request line: " + line);
          }

          request.method =
              Method.of(m.group(1))
                  .orElseThrow(
                      () -> new ErrorResponseException(Status.METHOD_NOT_ALLOWED, m.group(1)));

          final String urlString = m.group(2);
          final int q = urlString.indexOf('?');
          if (q == -1) {
            request.url = decodePercent(urlString, 0, urlString.length());
          } else {
            request.url = decodePercent(urlString, 0, q);
            decodeParams(urlString, q + 1, request.params);
          }
        };

    // parse headers
    boolean lastSpace = false;
    HeaderParseState state = HeaderParseState.NONE;

    outer:
    for (int c = is.read(); c != -1; c = is.read()) {
      if (c == '\r') {
        switch (state) {
          case NONE:
            state = HeaderParseState.CR;
            continue;
          case CR_LF:
            state = HeaderParseState.CR_LF_CR;
            continue;
          default:
            // double CR
            throw new BadRequestException("Unexpected CR");
        }
      }

      if (c == '\n') {
        switch (state) {
          case CR:
            state = HeaderParseState.CR_LF;
            continue;
          case CR_LF_CR:
            break outer;
          default:
            // no preceding CR
            throw new BadRequestException("Unexpected LF");
        }
      }

      // replace all horizontal whitespace with a single space
      if (c == '\t') {
        c = ' ';
      }

      switch (state) {
        case NONE:
          break;
        case CR_LF:
          state = HeaderParseState.NONE;
          // legacy: allow for continuation of the previous header
          if (c != ' ') {
            addHeader.run();
            lastSpace = false;
          }
          break;
        default:
          // lone CR
          throw new BadRequestException("Unexpected single CR");
      }

      // if horizontal whitespace, continue loop
      if (c == ' ') {
        if (!lastSpace) {
          lastSpace = true;
          headerLine.append(' ');
        }
        continue;
      } else {
        lastSpace = false;
      }

      // only support visible ASCII
      if (c < ' ' || c >= 0x7F) {
        throw new BadRequestException(String.format("Unexpected char %s (0x%02x)", c, c));
      }

      headerLine.append((char) c);
    }

    // Add remaining header, if any
    addHeader.run();
  }

  private String decodePercent(final String str, final int from, final int to) {
    if (from > to) {
      throw new IllegalArgumentException(from + " > " + to);
    }

    int i = from;
    while (true) {
      final int c = str.codePointAt(i);
      if (c == '+' || c == '%') {
        break;
      }
      if ((i += Character.charCount(c)) == to) {
        return str.substring(from, to);
      }
    }

    try {
      final CharsetEncoder enc = urlCharset.newEncoder();

      final ByteArrayOutputStream baos = new ByteArrayOutputStream(to - from);
      baos.writeBytes(enc.encode(CharBuffer.wrap(str.substring(from, i))).array());

      for (int c; i < to; ) {
        switch (c = str.codePointAt(i)) {
          case '%':
            if ((i += 3) > to) {
              throw new BadRequestException("Unclosed percent-encoding.");
            }

            try {
              baos.write(Integer.parseInt(str.substring(i - 2, i), 16));
            } catch (final NumberFormatException e) {
              throw new BadRequestException("Bad percent-encoding.", e);
            }
            break;

          case '+':
            c = ' ';
          default:
            baos.writeBytes(enc.encode(CharBuffer.wrap(Character.toChars(c))).array());
            i += Character.charCount(c);
            break;
        }
      }

      return urlCharset.newDecoder().decode(ByteBuffer.wrap(baos.toByteArray())).toString();
    } catch (final CharacterCodingException e) {
      throw new BadRequestException("Invalid " + urlCharset.name(), e);
    }
  }

  private void decodeParams(
      final String paramString, final int from, final Map<String, List<String>> params) {
    final int len = paramString.length();
    for (int start = from, amp, eq; start < len; start = amp + 1) {
      amp = paramString.indexOf('&', start);
      if (amp == -1) {
        amp = len;
        eq = paramString.indexOf('=', start);
      } else {
        eq = -1;
        for (int i = start; i < amp; i++) {
          if (paramString.charAt(i) == '=') {
            eq = i;
            break;
          }
        }
      }

      params
          .computeIfAbsent(
              decodePercent(paramString, start, eq == -1 ? amp : eq), x -> new ArrayList<>())
          .add(eq == -1 ? null : decodePercent(paramString, eq + 1, amp));
    }
  }

  private enum ChunkParseState {
    EMPTY,
    NUM,
    EXT,
    CR
  }

  private static byte[] readChunk(final InputStream is) throws IOException {
    int size = 0;

    ChunkParseState state = ChunkParseState.EMPTY;
    for (int c = is.read(); c != -1; c = is.read()) {
      if (c == '\r') {
        switch (state) {
          case NUM:
          case EXT:
            state = ChunkParseState.CR;
            continue;
          default:
            throw new BadRequestException("Unexpected CR in 'chunked' body");
        }
      }

      if (c == '\n') {
        if (state != ChunkParseState.CR) {
          throw new BadRequestException("Unexpected LF in 'chunked' body");
        }
        break;
      }

      if (c == ';') {
        if (state != ChunkParseState.NUM) {
          throw new BadRequestException("Unexpected ';' in 'chunked' body");
        }
        state = ChunkParseState.EXT;
        continue;
      }

      switch (state) {
        case EXT:
          continue;

        case EMPTY:
          state = ChunkParseState.NUM;
        case NUM:
          final int d = Character.digit(c, 16);
          if (d == -1) {
            throw new BadRequestException("Expected chunk size but found '" + c + "'");
          }

          // would cause overflow on shift
          if ((size & 0x7800_0000) != 0) {
            throw new BadRequestException("Chunk size too large");
          }

          size = size << 4 | d;
          break;

        default:
          throw new BadRequestException("Unexpected single CR in 'chunked' body");
      }
    }

    // last chunk has size 0
    if (size == 0) {
      return null;
    }

    // read this chunk
    final byte[] buf = is.readNBytes(size);
    if (buf.length < size) {
      throw new BadRequestException("Only received partial chunk: " + buf.length + " / " + size);
    }

    return buf;
  }

  public static void main(final String[] args) {
    new Server() {
      @Override
      public Response process(final Request request) {
        System.out.println(request.method + " " + request.url);
        request.params.forEach(
            (k, vs) ->
                System.out.println(
                    "\tquery: '"
                        + k
                        + "' = "
                        + vs.stream()
                            .map(v -> v == null ? "<null>" : "'" + v + "'")
                            .collect(Collectors.joining(", "))));
        System.out.println("\theaders: " + CaseUtil.indent(request.headers));
        return new Response();
      }
    }.run();
  }
}
