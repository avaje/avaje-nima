package io.avaje.nima;

import io.avaje.http.client.HttpClient;
import io.avaje.inject.BeanScope;
import io.avaje.inject.spi.Builder;
import io.avaje.inject.spi.Module;
import io.helidon.http.Status;
import io.helidon.webserver.WebServer;
import io.helidon.webserver.http.HttpRouting;
import org.junit.jupiter.api.Test;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

class NimaTest {

//  static final class Slow implements Runnable {
//    @Override
//    public void run() {
//      try {
//        for (int i = 0; i < 10; i++) {
//          System.out.print(" stopping " + i);
//          Thread.sleep(1000);
//        }
//        System.out.println("slow stopped now");
//      } catch (InterruptedException e) {
//        e.printStackTrace();
//      }
//    }
//  }

  @Test
  void initTest() {

    BeanScope scope = BeanScope.builder()
      .modules(new EmptyModule())
      .bean(HttpRouting.Builder.class, routes())
      .build();

    WebServer webServer = Nima.builder()
      .configure(scope)
      .port(8085)
      .maxConcurrentRequests(100)
      .maxPayloadSize(4_000)
      .maxTcpConnections(200)
      .shutdownGraceMillis(5_000)
      .build()
      .start();

    var httpClient = HttpClient.builder().baseUrl("http://localhost:8085")
      .build();

    var res = httpClient.request()
      .path("hi")
      .GET()
      .asString();

    assertThat(res.statusCode()).isEqualTo(200);

    //webServer.stop();
    //webServer.stop();
  }

  static HttpRouting.Builder routes() {
    return HttpRouting.builder()

      .any("/*", (req, res) -> {
        System.out.println("before any /*");
        res.next();
      })
      .get("/redir", (req, res) -> {
        System.out.println("redir");
        res.status(Status.MOVED_PERMANENTLY_301);
        res.headers().location(URI.create("/hi"));
        res.send();
        //res.send("hi");
        //res.next();
      })
      .get("/hi", (req, res) -> {
        System.out.println("hi");
        res.send("hi");
        //res.next();
      })
      .get("/foo/{+blah}", (req, res) -> {
        System.out.println("foo");
        String blah = req.path().pathParameters().first("blah").orElseThrow();
        res.send("foo blah=" + blah);
      })
      .get("/bar/{blah}", (req, res) -> {
        System.out.println("bar");
        String blah = req.path().pathParameters().first("blah").orElseThrow();
        res.send("bar blah=" + blah);
      })
      .any("/*", (req, res) -> {
        System.out.println("after any /*");
        res.next();
      })
//      .addFilter((chain, req, res) -> {
//        System.out.println("Filter Before path:" + req.path().path() + " raw:" + req.path().rawPath()+" query:" + req.query().rawValue());
//        chain.proceed();
//        System.out.println("Filter After" + req.path());
//      }).addFilter((chain, req, res) -> {
//        System.out.println("Filter222 Before path:" + req.path().path());
//        chain.proceed();
//        System.out.println("Filter222 After" + req.path());
//      })
      ;
  }

  static class EmptyModule implements Module {

    @Override
    public Class<?>[] classes() {
      return new Class[0];
    }

    @Override
    public void build(Builder builder) {

    }
  }
}
