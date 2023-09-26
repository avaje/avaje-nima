package org.example;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.util.List;

import org.example.api.Address;
import org.example.api.CreateOrderResponse;
import org.example.api.Customer;
import org.example.api.CustomerBuilder;
import org.example.api.OrderBuilder;
import org.example.api.OrderLine;
import org.junit.jupiter.api.Test;

import io.avaje.http.client.HttpException;
import io.avaje.inject.test.InjectTest;
import io.avaje.nima.test.TestClient;
import jakarta.inject.Inject;

@InjectTest
class CreateOrderTest {

  @Inject
  static TestClient httpClient;

  @Test
  void createOrder() {

    CustomerBuilder.builder().name("asd")
      .build();

    var address = new Address("", "line2", "foo", "ak");
    var customer = Customer.from(new Customer("C67", "Rob", address)).build();

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
