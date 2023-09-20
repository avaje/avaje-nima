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
Add the dependency.

```xml
<dependency>
    <groupId>io.avaje</groupId>
    <artifactId>avaje-nima</artifactId>
    <version>${version}</version>
</dependency>
```

And add avaje-jsonb-generator as an annotation processor. (it will transitively include all the avaje processors)
```xml
<dependency>
    <groupId>io.avaje</groupId>
    <artifactId>avaje-nima-generator</artifactId>
    <version>${version}</version>
    <optional>true</optional>
    <scope>provided</scop>
</dependency>
```

## Step 2 - Use the `Nima` Class to start your application
The `Nima` class will start a `BeanScope`, register generated controller routes, and start the helidon server.

The `Nima` class will search your `BeanScope` for a `WebServerConfig.Builder` class, if you provide one in your `BeanScope` it will be used to configure the webserver.

```java
void main() {
    Nima.builder().build();
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
