package org.example;

import io.avaje.nima.Nima;

public class Main {

  public static void main(String[] args) {
    // BeanScope beanScope = BeanScope.builder().build();

   var webServer =  Nima.builder()
     //.configure(beanScope)
     .port(8082)
     .build();

    webServer.start();
  }
}
