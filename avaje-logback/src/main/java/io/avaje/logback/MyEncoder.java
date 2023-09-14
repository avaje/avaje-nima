package io.avaje.logback;

import ch.qos.logback.classic.pattern.ThrowableHandlingConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.encoder.EncoderBase;
import io.avaje.jsonb.JsonWriter;
import io.avaje.jsonb.spi.PropertyNames;
import io.avaje.jsonb.stream.JsonStream;
import io.avaje.logback.abbreviator.TrimPackageAbbreviator;

import java.io.ByteArrayOutputStream;
import java.time.Instant;

public class MyEncoder extends EncoderBase<ILoggingEvent> {

  private static final byte[] EMPTY_BYTES = new byte[0];
  private final PropertyNames properties;
  private final JsonStream json;

  private byte[] headerBytes = EMPTY_BYTES;
  private byte[] footerBytes = EMPTY_BYTES;

  //private final ExtendedThrowableProxyConverter throwableConverter = new ExtendedThrowableProxyConverter();
  private final ThrowableHandlingConverter throwableConverter;

  public MyEncoder() {
    this.json = JsonStream.builder().build();
    this.properties = json.properties("@timestamp", "level", "logger", "message", "thread", "stack_trace");

    var converter = new ShortenedThrowableConverter();
    converter.setMaxDepthPerThrowable(3);

    var de = new TrimPackageAbbreviator();
    de.setTargetLength(20);
    converter.setClassNameAbbreviator(de);
    converter.setRootCauseFirst(true);
    throwableConverter = converter;//new ExtendedThrowableProxyConverter(); // converter
  }

  @Override
  public void start() {
    super.start();
    throwableConverter.start();
  }

  @Override
  public void stop() {
    super.stop();
    throwableConverter.stop();
  }

  @Override
  public byte[] headerBytes() {
    return headerBytes;
  }

  @Override
  public byte[] encode(ILoggingEvent event) {
    String stackTraceBody = throwableConverter.convert(event);
    int extra = stackTraceBody.isEmpty() ? 0 : 20 + stackTraceBody.length();

    final var threadName = event.getThreadName();
    final var message = event.getFormattedMessage();
    final var loggerName = event.getLoggerName();
    final int bufferSize = 100 + extra + message.length() + threadName.length() + loggerName.length();
    final var outputStream = new ByteArrayOutputStream(bufferSize);

    Instant instant = Instant.ofEpochMilli(event.getTimeStamp());
    JsonWriter writer = json.writer(outputStream);
    writer.beginObject(properties);
    writer.name(0);
    writer.rawValue(instant.toString());
    writer.name(1);
    writer.rawValue(event.getLevel().toString());
    writer.name(2);
    writer.value(loggerName);
    writer.name(3);
    writer.value(message);
    writer.name(4);
    writer.value(threadName);

    if (!stackTraceBody.isEmpty()) {
      writer.name(5);
      writer.value(stackTraceBody);
    }

    writer.endObject();
    writer.writeNewLine();
    writer.flush();

    return outputStream.toByteArray();
  }

  @Override
  public byte[] footerBytes() {
    return footerBytes;
  }
}
