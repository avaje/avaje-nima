package org.example.api;

import io.avaje.jsonb.Json;
import io.avaje.recordbuilder.RecordBuilder;
import io.avaje.validation.constraints.NotBlank;
import io.avaje.validation.constraints.NotEmpty;
import io.avaje.validation.constraints.NotNull;
import io.avaje.validation.constraints.Valid;

import java.time.LocalDate;
import java.util.List;

@RecordBuilder
@Valid
@Json
public record Order(
  @NotBlank String refNumber,
  @NotNull LocalDate orderDate,

  @Valid Customer customer,

  @Valid @NotEmpty List<OrderLine> lines
) {

  public static OrderBuilder builder() {
    return OrderBuilder.builder();
  }
}
