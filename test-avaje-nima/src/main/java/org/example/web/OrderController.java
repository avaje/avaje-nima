package org.example.web;

import io.avaje.http.api.Controller;
import io.avaje.http.api.Path;
import io.avaje.http.api.Post;
import io.avaje.http.api.Valid;
import org.example.api.CreateOrderResponse;
import org.example.api.Order;

import java.time.Instant;

@Valid
@Controller
@Path("/orders")
public class OrderController {

  @Post
  CreateOrderResponse create(Order order) {
    System.out.println("got order " + order);
    return new CreateOrderResponse(42, Instant.now());
  }
}
