package org.example;

import io.avaje.http.client.BodyReader;
import io.avaje.http.client.HttpClient;
import io.avaje.inject.test.InjectTest;
import jakarta.inject.Inject;
import org.example.api.Foo;
import org.example.api.ValidationErrorMessage;
import org.example.web.HelloController;
import org.junit.jupiter.api.Test;

import java.net.http.HttpResponse;

import static org.assertj.core.api.Assertions.assertThat;

@InjectTest
class MainTest {

  @Inject
  static HttpClient httpClient;

  @Test
  void one() {
    HttpResponse<String> res = httpClient.request()
      .GET().asString();

    assertThat(res.body()).isEqualTo("hello world");
  }

  @Test
  void oneBean() {
    HelloController.Something bean = httpClient.request()
      .path("one")
      .GET().bean(HelloController.Something.class);

    assertThat(bean.id()).isEqualTo(52);
    assertThat(bean.name()).isEqualTo("Asdasd");
  }

  @Test
  void fooBean() {
    Foo bean = httpClient.request()
      .path("foo")
      .GET().bean(Foo.class);

    assertThat(bean.id()).isEqualTo(82);
    assertThat(bean.name()).isEqualTo("Foo here 42");
  }

  @Test
  void fooPostValid() {
    var response = httpClient.request()
      .path("foo")
      .body(new Foo(42, "good"))
      .POST().asString();

    assertThat(response.statusCode()).isEqualTo(201);
    assertThat(response.body()).isEqualTo("ok");
  }

  @Test
  void fooPostInValid() {
    var response = httpClient.request()
      .path("foo")
      .body(new Foo(42, ""))
      .POST()
      .asString();

    assertThat(response.statusCode()).isEqualTo(422);

    String body = response.body();
    System.out.println("body: "+body);
    BodyReader<ValidationErrorMessage> reader = httpClient.bodyAdapter().beanReader(ValidationErrorMessage.class);
    ValidationErrorMessage validationErrorMessage = reader.readBody(body);

    assertThat(validationErrorMessage.forPath("name").getMessage()).isEqualTo("must not be blank");
  }

  @Test
  void fooPostInValid_LanguageGerman() {
    var response = httpClient.request()
      .header("Accept-Language", "de")
      .path("foo")
      .body(new Foo(42, ""))
      .POST().asString();

    assertThat(response.statusCode()).isEqualTo(422);

    String body = response.body();
    System.out.println("body: "+body);
    BodyReader<ValidationErrorMessage> reader = httpClient.bodyAdapter().beanReader(ValidationErrorMessage.class);
    ValidationErrorMessage validationErrorMessage = reader.readBody(body);

    assertThat(validationErrorMessage.forPath("name").getMessage()).isEqualTo("darf nicht leer sein");
  }

  @Test
  void health() {
    HttpResponse<String> res = httpClient.request()
      .path("health")
      .GET().asString();

    assertThat(res.body()).isEqualTo("ok");
  }

  @Test
  void two() {
    HttpResponse<String> res = httpClient.request()
      .GET().asString();

    assertThat(res.body()).isEqualTo("hello world");
  }

  @Test
  void three() {
    HttpResponse<String> res = httpClient.request()
      .GET().asString();

    assertThat(res.body()).isEqualTo("hello world");
  }
}
