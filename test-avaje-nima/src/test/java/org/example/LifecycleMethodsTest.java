package org.example;

import io.avaje.nima.AppLifecycle;
import io.avaje.nima.Nima;
import org.example.web.HelloController;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LifecycleMethodsTest {

  static List<String> o = new ArrayList<>();

  @Test
  void lifecycleMethods() {
    Nima nima = Nima.builder()
      .port(8082)
      .shutdownGraceMillis(30_000)
      .preStart(() -> o.add("pre-start-100"), 100)
      .postStart(() -> o.add("post-start-100"), 100)
      .preStop(() -> o.add("pre-stop-100"), 100)
      .postStop(() -> o.add("post-stop-100"), 100)
      .register(
        new AppLifecycle.Callback() {
          @Override
          public void preStart() {
            o.add("pre-start-20");
          }

          @Override
          public void postStop() {
            o.add("post-stop-20");
          }
        },
        20)
      .build();


    var beanScope = nima.beanScope();
    assertThat(beanScope.get(HelloController.class)).isNotNull();
    assertThat(beanScope.get(HelloController.class)).isNotNull();

    var webServer = nima.server();

    webServer.start();
    webServer.stop();

    assertThat(o)
      .containsExactly(
        "pre-start-20",
        "pre-start-100",
        "post-start-100",
        "pre-stop-100",
        "post-stop-20",
        "post-stop-100");
  }
}
