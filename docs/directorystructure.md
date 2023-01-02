# Material component directory structure

All of the Material Components are located under
**[lib/](https://github.com/material-components/material-components-android/tree/master/lib)**.

Classes in the library are separated into directories that are specific to each component, e.g.:

*   [appbar/](https://github.com/material-components/material-components-android/tree/master/lib/java/com/google/android/material/appbar/)
*   [bottomnavigation/](https://github.com/material-components/material-components-android/tree/master/lib/java/com/google/android/material/bottomnavigation/)
*   [button/](https://github.com/material-components/material-components-android/tree/master/lib/java/com/google/android/material/button/)

Classes in the component directories comprise the public API; these can be used directly in your
applications. Classes in **internal/** are part of the protected API and are used to
support the public API classes.
