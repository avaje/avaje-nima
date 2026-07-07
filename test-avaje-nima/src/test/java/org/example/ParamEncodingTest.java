package org.example;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.http.HttpResponse;

import org.junit.jupiter.api.Test;

import io.avaje.http.client.HttpClient;
import io.avaje.inject.test.InjectTest;
import jakarta.inject.Inject;

/**
 * Verifies the treatment of '+' (and other characters needing encoding)
 * in path parameters versus query parameters, comparing avaje-nima
 * (Helidon based) behaviour against the fixes made in avaje-jex PR #416
 * (https://github.com/avaje/avaje-jex/pull/416).
 */
@InjectTest
class ParamEncodingTest {

  @Inject
  static HttpClient httpClient;

  // --- Path parameters ---

  @Test
  void pathParam_literalPlus_isNotDecodedToSpace() {
    // '+' embedded raw in the path segment - .path() bypasses client encoding
    HttpResponse<String> res = httpClient.request()
      .path("params/path/a+b")
      .GET().asString();

    assertThat(res.statusCode()).isEqualTo(200);
    // Path params use RFC3986 style decoding - '+' is literal, not a space
    assertThat(res.body()).isEqualTo("a+b");
  }

  @Test
  void pathParam_encodedPlus_decodesToLiteralPlus() {
    HttpResponse<String> res = httpClient.request()
      .path("params/path/a%2Bb")
      .GET().asString();

    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(res.body()).isEqualTo("a+b");
  }

  @Test
  void pathParam_encodedSpace_decodesToSpace() {
    HttpResponse<String> res = httpClient.request()
      .path("params/path/a%20b")
      .GET().asString();

    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(res.body()).isEqualTo("a b");
  }

  // --- Query parameters ---

  @Test
  void queryParam_literalPlus_isDecodedToSpace() {
    // '+' embedded raw in the query string - .path() bypasses client encoding
    HttpResponse<String> res = httpClient.request()
      .path("params/query?value=a+b")
      .GET().asString();

    assertThat(res.statusCode()).isEqualTo(200);
    // Query params use application/x-www-form-urlencoded style decoding - '+' means space
    assertThat(res.body()).isEqualTo("a b");
  }

  @Test
  void queryParam_encodedPlus_decodesToLiteralPlus() {
    HttpResponse<String> res = httpClient.request()
      .path("params/query?value=a%2Bb")
      .GET().asString();

    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(res.body()).isEqualTo("a+b");
  }

  @Test
  void queryParam_encodedSpace_decodesToSpace() {
    HttpResponse<String> res = httpClient.request()
      .path("params/query?value=a%20b")
      .GET().asString();

    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(res.body()).isEqualTo("a b");
  }

  @Test
  void queryParam_usingClientEncoding_roundTripsCorrectly() {
    // queryParam() on the client URL-encodes the value (space -> '+', '+' -> %2B)
    HttpResponse<String> res = httpClient.request()
      .path("params/query")
      .queryParam("value", "a+b c")
      .GET().asString();

    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(res.body()).isEqualTo("a+b c");
  }

  @Test
  void queryParam_complexMessage_roundTripsCorrectly() {
    String message = "He asked: \"Does 1 + 1 / 1 = 200% for 'efficiency'?\"";
    HttpResponse<String> res = httpClient.request()
      .path("params/query")
      .queryParam("value", message)
      .GET().asString();

    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(res.body()).isEqualTo(message);
  }

  @Test
  void queryParam_missing_usesDefault() {
    HttpResponse<String> res = httpClient.request()
      .path("params/query-default")
      .GET().asString();

    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(res.body()).isEqualTo("default");
  }
}
