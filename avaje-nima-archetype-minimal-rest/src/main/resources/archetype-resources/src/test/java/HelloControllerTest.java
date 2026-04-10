package ${package};

import static org.assertj.core.api.Assertions.assertThat;

import java.net.http.HttpResponse;

import org.junit.jupiter.api.Test;

import io.avaje.http.client.HttpClient;
import io.avaje.inject.test.InjectTest;
import jakarta.inject.Inject;

@InjectTest
class HelloControllerTest {

  @Inject
  static HttpClient client;

  @Test
  void hi_returnsPlainText() {
    HttpResponse<String> res = client.request()
      .path("hi")
      .GET().asString();

    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(res.body()).isEqualTo("hi");
  }

  @Test
  void data_returnsJson() {
    HttpResponse<String> res = client.request()
      .path("hi/data")
      .GET().asString();

    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(res.body()).contains("message");
    assertThat(res.body()).contains("timestamp");
  }
}
