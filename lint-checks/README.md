## Lint checks

- `ThemeCheckColorAccent` : Detects usages colorAccent in style resources
- `ThemeCheckColorPrimaryDark` : Detects usages colorPrimaryDark in style resources

## Setup in your project

In order to get the lint checks running on the code in your module:

1.  Open the `build.gradle` file for your application.
2.  Make sure that the `repositories` section includes Google's Maven Repository
    `google()`. For example:

    ```groovy
      allprojects {
        repositories {
          google()
          jcenter()
        }
      }
    ```

3.  Add the library to the `dependencies` section:

    ```groovy
      dependencies {
        // ...
        lintChecks 'com.google.android.material:material-lint-checks:x.x.x'
        // ...
      }
    ```

Visit
[Google's Maven Repository](https://maven.google.com/web/index.html#com.google.android.material:material-lint-checks)
or
[MVN Repository](https://mvnrepository.com/artifact/com.google.android.material/material-lint-checks)
to find the latest version of the library.

## Usage

Run lint on your app module with the following Gradle command:

```
./gradlew app:lintDebug
```

## Disable Lint checks:

If you only need some of lint checks included you can disable them as follows:

In your `build.gradle` file:

```
android {
   lintOptions {
        disable 'ThemeCheckColorAccent', 'ThemeCheckColorPrimaryDark'
    }
}
```
