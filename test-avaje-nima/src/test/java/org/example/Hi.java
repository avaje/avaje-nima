package org.example;

import io.avaje.nima.AppLifecycle;
import io.avaje.nima.Nima;

public class Hi {

  public static void main(String[] args) {
    var webServer = Nima.builder()
      .port(8082)
      .shutdownGraceMillis(30_000)
      .register(new AppLifecycle.Callback() {
        @Override
        public void preStart() {
          System.out.println("pre starting ...");
        }

        @Override
        public void postStop() {
          System.out.println("I've stopped");
        }
      }, 2000)
      .build();

    webServer.start();
  }
}
