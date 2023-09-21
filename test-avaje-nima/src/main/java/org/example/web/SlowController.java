package org.example.web;

import io.avaje.http.api.*;
import io.avaje.inject.PreDestroy;

@Controller
@Path("slow")
class SlowController {

  @PreDestroy
  void stopMe() throws InterruptedException {
    System.out.println("stopMe");
    for (int i = 0; i < 3; i++) {
      Thread.sleep(500);
      System.out.print(" " + i);
    }
    System.out.println("stop slow");
  }

  @Produces("text/plain")
  @Get
  String slow(@Default("10") int count ) throws InterruptedException {
    for (int i = 0; i < count; i++) {
      Thread.sleep(500);
      System.out.print(" " + i);
    }
    System.out.println("slow");
    return "slow response";
  }
}
