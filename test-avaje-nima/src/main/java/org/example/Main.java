package org.example;

import io.avaje.nima.Nima;

public class Main {

  public static void main(String[] args) {

    var webServer = Nima.builder()
      .port(8082)
      .build();

    webServer.start();
  }
}
