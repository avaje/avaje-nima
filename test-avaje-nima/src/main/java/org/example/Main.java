package org.example;

import io.avaje.nima.Nima;
import org.example.api.AddressBuilder;
import org.example.api.Order;
import org.example.api.OrderBuilder;
import org.example.api.OrderLine;

import java.time.LocalDate;

public class Main {

  public static void main(String[] args)  {

    var order = Order.builder()
      .orderDate(LocalDate.now())
      .addLines(new OrderLine(23, 10, "asd"))
      .addLines(new OrderLine(12, 11, "asd"))
      .build();

    var webServer = Nima.builder()
      .port(8082)
      .build();

    webServer.start();
  }
}
