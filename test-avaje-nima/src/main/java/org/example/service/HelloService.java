package org.example.service;

import io.avaje.inject.Component;

@Component
public class HelloService {

  public void ithrow() {
    callOne();
  }

  private void callOne() {
    try {
      callIt();
    } catch (IllegalStateException e) {
      throw new RuntimeException("I wrap it", e);
    }
  }

  private void callIt() {
    call3();
  }

  private void call3() {
    call4();
  }
  private void call4() {
    call5();
  }

  private void call5() {
    throw new IllegalStateException("I throw this!!");
  }
}
