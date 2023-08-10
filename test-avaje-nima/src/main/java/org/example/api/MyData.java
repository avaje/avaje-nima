package org.example.api;

import io.avaje.jsonb.Json;
import io.soabase.recordbuilder.core.RecordBuilder;

//@RecordBuilder
// addSingleItemCollectionBuilders=true
@RecordBuilder.Options(enableGetters=false, enableWither=false)
@Json
public @interface MyData {
}
