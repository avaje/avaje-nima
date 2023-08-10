package org.example.api;

import io.avaje.jsonb.Json;
import io.avaje.validation.constraints.NotBlank;
import io.avaje.validation.constraints.Valid;
import io.soabase.recordbuilder.core.RecordBuilder;

@RecordBuilder
@Valid
@Json
public record Address(
  @NotBlank String line1,
  String line2,
  @NotBlank String city,
  @NotBlank String region
) {

  public static AddressBuilder builder() {
    return AddressBuilder.builder();
  }
}
