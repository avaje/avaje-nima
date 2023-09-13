package org.example;

import io.avaje.nima.Nima;

import java.io.IOException;
//import java.util.logging.LogManager;

public class Main {

  public static void main(String[] args) throws IOException {

//    LogManager.getLogManager().readConfiguration(
//      Main.class.getResourceAsStream("/logging.properties"));

    var webServer = Nima.builder()
      .port(8082)
      .build();

    webServer.start();
  }
}
