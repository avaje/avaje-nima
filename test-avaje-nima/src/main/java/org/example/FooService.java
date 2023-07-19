package org.example;

import io.avaje.inject.Component;

@Component
class FooService {
  long helloThere() {
    return 42L;
  }
}
