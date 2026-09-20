# Getting Started with HTMX

How to build a server-rendered HTML user interface with **avaje-nima**, **JStache**, and
**HTMX**.

This guide uses a conventional route split:

- `/ui` for full HTML pages and HTMX fragment endpoints
- `/api` for JSON APIs
- `/static` for browser assets

Keeping these namespaces separate makes authentication and authorization rules easier to
reason about.

## Add the dependency

Add `avaje-nima-htmx` to the server module:

```xml
<dependency>
  <groupId>io.avaje</groupId>
  <artifactId>avaje-nima-htmx</artifactId>
  <version>1.13</version>
</dependency>
```

Replace the version with the current published avaje-nima version when starting a new
project.

The dependency provides the Nima HTMX integration, JStache template rendering, and a
default `TemplateRender` bean. No reflection-based template engine configuration is
required.

## Create a JStache view

Place the template path configuration in the same package as the view models:

```java
@JStachePath(prefix = "ui/", suffix = ".mustache")
package example.web.view;

import io.jstach.jstache.JStachePath;
```

Create a view model for the page:

```java
package example.web.view;

import io.jstach.jstache.JStache;

@JStache(path = "index")
public record IndexView() {
}
```

The corresponding template is `src/main/resources/ui/index.mustache`.

## Add an HTML controller

Mark the controller with `@Html`. Returned view models are rendered through the injected
`TemplateRender` implementation:

```java
package example.web;

import io.avaje.htmx.api.Html;
import io.avaje.http.api.Controller;
import io.avaje.http.api.Get;
import io.avaje.http.api.Path;
import example.web.view.IndexView;

@Html
@Controller
@Path("/ui")
public class IndexController {

  @Get
  public IndexView index() {
    return new IndexView();
  }
}
```

The page is available at `GET /ui`.

If `/` should be an entry point, redirect it to `/ui` rather than serving the same page
from both routes. A small `HttpFeature` is sufficient:

```java
@Component
final class RootRedirect implements HttpFeature {

  @Override
  public void setup(HttpRouting.Builder routing) {
    routing.get("/", (request, response) ->
      response.status(SEE_OTHER_303)
        .header("Location", "/ui")
        .send());
  }
}
```

## Add HTMX to the page

HTMX can be loaded from a CDN during development, or vendored into the application's
static resources for a self-contained deployment. The page should reference the
application's static route:

```html
<!doctype html>
<html lang="en">
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>Example</title>
  <link rel="stylesheet" href="/static/vendor/pico.min.css">
  <script src="/static/vendor/htmx.min.js"></script>
</head>
<body>
<main>
  <h1>Example</h1>
</main>
</body>
</html>
```

Pin the HTMX version rather than using an unversioned URL. If using HTMX 4, review its
upgrade notes before migrating existing applications, particularly for explicit attribute
inheritance and renamed event names.

## Add a search form and fragment endpoint

Put value-sensitive HTMX triggers on the input that owns the value. This ensures that the
`changed` modifier evaluates the input value and that the request includes the input's
form parameter:

```html
<form>
  <label for="organisation-name">Search by name</label>
  <input id="organisation-name"
         name="name"
         type="search"
         hx-post="/ui/organisations/search"
         hx-trigger="input changed delay:300ms, search"
         hx-target="#search-results"
         hx-indicator="#search-indicator">
  <span id="search-indicator" class="htmx-indicator">Searching...</span>
</form>
<div id="search-results"></div>
```

The `search` event also supports submitting the search from the browser's search or enter
control. `hx-target` replaces the result element with the returned HTML fragment.

Bind form data with `@Form` and return a fragment view model:

```java
@Html
@Controller
@Path("/ui")
public class OrganisationController {

  private final OrganisationService service;

  OrganisationController(OrganisationService service) {
    this.service = service;
  }

  @Form
  @Post("/organisations/search")
  public OrganisationSearchView search(String name) {
    var searchTerm = name == null ? "" : name.trim();
    var results = searchTerm.isEmpty()
      ? List.<Organisation>of()
      : service.findByName(searchTerm, 20);
    return new OrganisationSearchView(results);
  }
}
```

Use a separate JStache model for the fragment:

```java
@JStache(path = "organisation-search")
public record OrganisationSearchView(List<Organisation> organisations) {
}
```

The fragment template should contain only the HTML that belongs inside
`#search-results`, not another document:

```mustache
<table>
  <thead>
  <tr>
    <th scope="col">Organisation</th>
    <th scope="col">Central ID</th>
  </tr>
  </thead>
  <tbody>
  {{#organisations}}
  <tr>
    <td>{{name}}</td>
    <td>{{centralOrganisationId}}</td>
  </tr>
  {{/organisations}}
  </tbody>
</table>
{{^organisations}}
<p>No organisations found.</p>
{{/organisations}}
```

Reuse application services directly from the HTML controller. Do not make an internal HTTP
request to the application's own JSON API.

`@HxRequest` can be added when a handler must only run for HTMX requests. It filters on the
`HX-Request` header, so tests for that handler must send the header explicitly. For simple
fragment endpoints, a normal form `POST` is often sufficient and also makes the endpoint
usable without JavaScript.

## Serve static resources

Register classpath resources with an `HttpFeature`:

```java
@Component
final class StaticRoutes implements HttpFeature {

  @Override
  public void setup(HttpRouting.Builder routing) {
    staticContent(routing, "images");
    staticContent(routing, "static");
  }

  private void staticContent(HttpRouting.Builder routing, String path) {
    var service = StaticContentFeature.createService(
      ClasspathHandlerConfig.create(path));
    routing.register("/" + path, service);
  }
}
```

For example, `src/main/resources/static/vendor/htmx.min.js` is served at
`/static/vendor/htmx.min.js`.

## Native image resources

GraalVM native images need resource patterns for classpath files served at runtime. Add a
`resource-config.json` under the service's native-image metadata directory:

```json
{
  "resources": [
    { "pattern": "static/.*" },
    { "pattern": "images/.*" },
    { "pattern": "ui/.*" }
  ]
}
```

Include `ui/.*` when templates are loaded as classpath resources by the chosen rendering
setup, even if generated template adapters also embed some template content.

## Test HTML pages and fragments

Use `@InjectTest` and the generated typed controller API:

```java
@InjectTest
class IndexControllerTest {

  @Inject
  IndexControllerTestAPI controller;

  @Test
  void indexReturnsHtmlPage() {
    HttpResponse<String> response = controller.index();

    assertThat(response.statusCode()).isEqualTo(200);
    assertThat(response.headers().firstValue("content-type"))
      .hasValueSatisfying(value -> assertThat(value).startsWith("text/html"));
    assertThat(response.body())
      .contains("<title>Example</title>")
      .contains("hx-post=\"/ui/organisations/search\"");
  }
}
```

For a form-backed fragment, seed the service dependency or test database, call the
generated method, and assert the returned HTML contains the matching data and expected
fragment structure. Also cover blank input and no-result responses.
