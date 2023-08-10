package org.example;

import io.avaje.http.client.HttpClient;
import io.avaje.http.client.HttpException;
import io.avaje.inject.test.InjectTest;
import jakarta.inject.Inject;
import org.example.api.*;
import org.junit.jupiter.api.Test;

import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

@InjectTest
class CreateOrderTest {

  @Inject
  static HttpClient httpClient;

  @Test
  void createOrder() {

    CustomerBuilder.builder().name("asd")
      .build()
      .withId("asd");

    var address = new Address("", "line2", "foo", "ak");
    var customer = CustomerBuilder.Customer("C67", "Rob", address);

    var lines = List.of(new OrderLine(54,3, "aaa"),
      new OrderLine(55,10, "bb"),
      new OrderLine(-1,2, "cc"));

    var order = OrderBuilder.builder()
      .orderDate(LocalDate.now())
      .refNumber("FG45")
      .customer(customer)
      .lines(lines)
      .build();

    try {
      HttpResponse<CreateOrderResponse> res = httpClient.request()
        .path("orders")
        .body(order)
        .POST().as(CreateOrderResponse.class);

      assertThat(res.statusCode()).isEqualTo(200);
      assertThat(res.body().id()).isEqualTo(42);

    } catch (HttpException e) {
      String s = e.bodyAsString();
      System.out.println(s);
    }

  }
}
