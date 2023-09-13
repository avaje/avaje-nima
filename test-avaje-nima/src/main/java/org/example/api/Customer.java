package org.example.api;

import io.avaje.jsonb.Json;
import io.avaje.recordbuilder.RecordBuilder;
import io.avaje.validation.constraints.NotBlank;
import io.avaje.validation.constraints.Valid;

@RecordBuilder
@Valid
@Json
public record Customer(
  @NotBlank(max = 20)
  String id,

  @NotBlank // @Max(20)
  String name,

  @Valid
  Address shippingAddress

){
  /**
   * Return a new builder for Customer
   */
  public static CustomerBuilder builder() {
    return CustomerBuilder.builder();
  }

  /**
   * Return a new builder given the source Customer
   */
  public static CustomerBuilder from(Customer source) {
    return CustomerBuilder.builder(source);
  }
}
