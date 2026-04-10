package ${package};

import io.avaje.nima.Nima;

public class Main {

  public static void main(String[] args) {
    var webServer = Nima.builder()
      .port(8080)
      .build();

    webServer.start();
  }
}
