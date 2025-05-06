package org.example;

import io.avaje.http.client.HttpClient;
import io.avaje.inject.test.InjectTest;
import io.helidon.webserver.WebServer;
import jakarta.inject.Inject;
import org.example.web.HelloControllerTestAPI;
import org.junit.jupiter.api.Test;
import java.net.http.HttpResponse;

import static org.assertj.core.api.Assertions.assertThat;

@InjectTest
class InjectClientsTest {

  @Inject
  HelloControllerTestAPI helloControllerTestAPI;

  @Inject
  HttpClient httpClient;

  @Inject
  HttpClient.Builder httpClientBuilder;

  @Inject
  WebServer webServer;

  @Test
  void test() {

    assertThat(webServer).isNotNull();
    assertThat(webServer.port()).isNotEqualTo(0);
    assertThat(httpClient).isNotNull();
    assertThat(httpClientBuilder).isNotNull();
    assertThat(helloControllerTestAPI).isNotNull();

    HttpResponse<String> res = helloControllerTestAPI.hello();
    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(res.body()).isEqualTo("hello world");

    var res2 = httpClient.request().GET().asString();
    assertThat(res2.statusCode()).isEqualTo(200);
    assertThat(res2.body()).isEqualTo("hello world");

    var res3 = httpClientBuilder.build().request().GET().asString();
    assertThat(res3.statusCode()).isEqualTo(200);
    assertThat(res3.body()).isEqualTo("hello world");
  }
}
