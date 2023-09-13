package io.avaje.nima;

import io.avaje.inject.BeanScope;
import io.helidon.http.Http;
import io.helidon.webserver.WebServer;
import io.helidon.webserver.http.HttpRouting;
import org.junit.jupiter.api.Test;

import java.net.URI;

class NimaTest {

  public static void main(String[] args) {
    var builder = HttpRouting.builder()

      .any("/*", (req, res) -> {
        System.out.println("before any /*");
        res.next();
      })
      .get("/redir", (req, res) -> {
        System.out.println("redir");
        res.status(Http.Status.MOVED_PERMANENTLY_301);
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
    BeanScope scope = BeanScope.builder()
      .bean(HttpRouting.Builder.class, builder)
      .build();

    WebServer webServer = Nima.builder()
      .configure(scope)
      .port(8082)
      .build()
      .start();
  }

  @Test
  void initTest() {

    //nima.port();

  }
}
