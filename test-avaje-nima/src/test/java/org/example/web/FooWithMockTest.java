package org.example.web;

import io.avaje.http.client.HttpClient;
import io.avaje.inject.test.InjectTest;
import jakarta.inject.Inject;
import org.example.api.Foo;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.net.http.HttpResponse;

import static org.assertj.core.api.Assertions.assertThat;

@InjectTest
class FooWithMockTest {

  @Mock
  static FooService fooService;

  @Inject
  static HttpClient httpClient;

  @BeforeAll
  static void beforeAll() {
    Mockito.when(fooService.helloThere()).thenReturn(100L);
  }

  @Test
  void foo_expect_mockUsedInResponse() {
    HttpResponse<Foo> res = httpClient.request()
      .path("foo")
      .GET().as(Foo.class);

    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(res.body().name()).isEqualTo("Foo here 100");
  }
}
