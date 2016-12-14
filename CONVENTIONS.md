# Code conventions

Since we all want to spend more time coding and less time fiddling with
whitespace, Material Components for Android uses code conventions and styles to
encourage consistency. Code with a consistent style is easier (and less
error-prone!) to review, maintain, and understand.

### Be consistent

If the style guide is not explicit about a particular situation, the cardinal
rule is to **be consistent**. For example, take a look at the surrounding code
and follow its lead, or look for similar cases elsewhere in the codebase.

## General conventions

TODO: Add general conventions

## Style

We provide configurations for Android Studio that allow for auto-formatting to
the below guidelines. Java style is also enforced via checkstyle as part of our
Gradle build.

TODO: Link to IDE presets

### Java

We follow the
[Google Java Style Guide](https://google.github.io/styleguide/javaguide.html).

### XML

- 2 space indentation
- Resource naming (including IDs) is `lowercase_with_underscores`
- Attribute ordering:
  1. `xmlns:android`
  2. other `xmlns:`
  3. `android:id`
  4. `style`
  5. `android:layout_` attributes
  6. `android:padding` attributes
  7. other `android:` attributes
  8. `app:` attributes
  9. `tool:` attributes
