package org.example.api;

import io.avaje.jsonb.Json;
import io.avaje.validation.constraints.NotBlank;
import io.avaje.validation.constraints.NotEmpty;
import io.avaje.validation.constraints.NotNull;
import io.avaje.validation.constraints.Valid;
import io.soabase.recordbuilder.core.RecordBuilder;

import java.time.LocalDate;
import java.util.List;

@RecordBuilder
// addSingleItemCollectionBuilders=true
@RecordBuilder.Options(enableGetters=false, enableWither=false)
@Valid
@Json
public record Order(
  @NotBlank String refNumber,
  @NotNull LocalDate orderDate,

  @Valid Customer customer,

  @Valid @NotEmpty List<OrderLine> lines
  ) {
}
