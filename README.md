[![Build](https://github.com/avaje/avaje-nima/actions/workflows/build.yml/badge.svg)](https://github.com/avaje/avaje-nima/actions/workflows/build.yml)
[![Maven Central](https://img.shields.io/maven-central/v/io.avaje/avaje-nima.svg?label=Maven%20Central)](https://mvnrepository.com/artifact/io.avaje/avaje-nima)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://github.com/avaje/avaje-nima/blob/master/LICENSE)
[![Discord](https://img.shields.io/discord/1074074312421683250?color=%237289da&label=discord)](https://discord.gg/Qcqf9R27BR)
# avaje-nima

Convenience Library to make using the varios avaje libraries with helidon easier.

Along with Helidon SE, It transitively brings in the following avaje services:
- `config`
- `http-api`
- `http-client`
- `inject`
- `jsonb`
- `validator`

The generator module transitively brings in the following avaje services and annotation processors:
- `http-client-generator`
- `http-helidon-generator`
- `inject-generator`
- `jsonb-generator`
- `record-builder`
- `spi-service`
- `validator-generator`

# How to use

## Step 1 - Add dependencies
Add the `avaje-nima` dependency.

```xml
<dependency>
    <groupId>io.avaje</groupId>
    <artifactId>avaje-nima</artifactId>
    <version>${version}</version>
</dependency>
```

Add the `avaje-nima-test` as a test dependency. This is to support testing
the application by starting the webserver on a random port for tests.

```xml
<dependency>
    <groupId>io.avaje</groupId>
    <artifactId>avaje-nima-test</artifactId>
    <version>0.4-SNAPSHOT</version>
    <scope>test</scope>
</dependency>
```

And add `avaje-nima-generator` as an annotation processor. This will transitively
include all the avaje processors.
```xml
<dependency>
    <groupId>io.avaje</groupId>
    <artifactId>avaje-nima-generator</artifactId>
    <version>${version}</version>
    <optional>true</optional>
    <scope>provided</scop>
</dependency>
```

## Step 2 - Create a controller

```java
import io.avaje.http.api.*;

@Controller
@Path("/")
class HelloController {

    @Produces("text/plain")
    @Get
    String hello() {
        return "hello world";
    }
}
```
On compilation, a `HelloController$Route` will be generated into `generated-sources`. This will
have Helidon SE adapter code for the HelloController registering the path and handling the
request and response.

On compilation, a `HelloController$DI` and `HelloController$Route$DI` will be generated to handle
dependency injection for the controller and the route adapter.


## Step 3 - Use the `Nima` Class to start the application
The `Nima` class will start a `BeanScope`, register generated controller routes, and start the helidon webserver.

The `Nima` class will search your `BeanScope` for a `WebServerConfig.Builder` class, if you provide one in your
`BeanScope` it will be used to configure the webserver.

```java
  void main() {

    var webServer = Nima.builder()
      .port(8080)
      .build();

    webServer.start();
  }
```

## Step 4 - Run and curl test
Run the application and use curl to test

```java
curl localhost:8080
```

## Step 5 - Create a unit test

Create a unit test that will start the application on a random port, and make a call to
the controller method and assert the response is as expected.

```java
package org.example;

import io.avaje.http.client.HttpClient;
import io.avaje.inject.test.InjectTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.net.http.HttpResponse;

import static org.assertj.core.api.Assertions.assertThat;

@InjectTest
class HelloTest {

    // Injecting a HttpClient means the test plugin will
    // automatically start the application on a random port
    // and inject the client (and shutdown the webserver after
    // the test has completed).
    @Inject
    static HttpClient httpClient;

    @Test
    void hello() {
        HttpResponse<String> res = httpClient.request()
          .GET()
          .asString();

      assertThat(res.statusCode()).isEqualTo(200);
      assertThat(res.body()).isEqualTo("hello world");
    }

}

```



## module use

The `io.avaje.nima` module transitively includes:
- `io.avaje.config`
- `io.avaje.http.api`
- `io.avaje.inject`
- `io.avaje.jsonb`
- `io.avaje.jsonb.plugin`
- `io.helidon.webserver`


```java
module nima.example {

  requires io.avaje.nima;

  requires io.avaje.http.client; //if using avaje http client
  requires io.avaje.validation.http; //if using avaje validator
  requires static io.avaje.recordbuilder; //if using avaje record builder
  requires static io.avaje.spi; //if using avaje services
}
```
