package io.avaje.nima.opentelemetry;

import io.helidon.webserver.http.Filter;
import io.opentelemetry.api.OpenTelemetry;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class NimaOtelFilterTest {

  @Test
  void buildExcludesHealthByDefault() {
    var filter = NimaOtelFilter.builder(OpenTelemetry.noop()).build();

    assertThat(excludedPaths(filter)).containsExactly("/health");
  }

  @Test
  void buildCombinesCustomExcludedPaths() {
    var filter = NimaOtelFilter.builder(OpenTelemetry.noop())
      .excludePaths("/metrics", "/ready")
      .build();

    assertThat(excludedPaths(filter)).containsExactly("/metrics", "/ready", "/health");
  }

  @Test
  void buildCanDisableHealthExclusion() {
    var filter = NimaOtelFilter.builder(OpenTelemetry.noop())
      .excludeHealthPaths(false)
      .excludePaths("/metrics")
      .build();

    assertThat(excludedPaths(filter)).containsExactly("/metrics");
  }

  @SuppressWarnings("unchecked")
  private static List<String> excludedPaths(Filter filter) {
    try {
      Field field = filter.getClass().getDeclaredField("excludedPaths");
      field.setAccessible(true);
      return (List<String>) field.get(filter);
    } catch (ReflectiveOperationException e) {
      throw new AssertionError(e);
    }
  }
}
