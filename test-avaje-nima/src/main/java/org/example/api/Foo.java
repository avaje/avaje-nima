package org.example.api;

import io.avaje.jsonb.Json;
import io.avaje.validation.constraints.NotBlank;
import io.avaje.validation.constraints.Valid;

@Json @Valid
public record Foo(int id, @NotBlank String name) {

}
