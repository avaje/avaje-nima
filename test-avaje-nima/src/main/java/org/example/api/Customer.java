package org.example.api;

import io.avaje.jsonb.Json;
import io.avaje.validation.constraints.NotBlank;
import io.avaje.validation.constraints.Valid;
import io.soabase.recordbuilder.core.RecordBuilder;

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

) implements CustomerBuilder.With {}
