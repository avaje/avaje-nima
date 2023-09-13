package org.example.api;

import io.avaje.recordbuilder.RecordBuilder;
import io.avaje.validation.constraints.Length;
import io.avaje.validation.constraints.Positive;
import io.avaje.validation.constraints.Valid;

@RecordBuilder
@Valid
@MyData
public record OrderLine(
  @Positive int productId,
  @Positive int quantity,

  @Length(max = 100) String description
) {
}
