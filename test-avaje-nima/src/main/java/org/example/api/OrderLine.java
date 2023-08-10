package org.example.api;

import io.avaje.validation.constraints.Length;
import io.avaje.validation.constraints.Positive;
import io.avaje.validation.constraints.Valid;

@Valid
@MyData
public record OrderLine(
  @Positive int productId,
  @Positive int quantity,

  @Length(max = 100) String description
) {
}
