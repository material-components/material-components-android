# A Guide to navigating the Repo's Library

Want to know what is going on in the file structure? Here is how you can navigate the GitHub repository to get to where you need to...

## The core Android library

The main gradle Android library posted in [releases](https://github.com/material-components/material-components-android/releases) that holds all of the Material Components ia located under **[lib/](https://github.com/material-components/material-components-android/tree/master/lib)**.

In the library's [main Java package](https://github.com/material-components/material-components-android/tree/master/lib/java/com/google/android/material) are subpackages available that each hold available or in-development design components:

*   [appbar/](https://github.com/material-components/material-components-android/tree/master/lib/java/com/google/android/material/appbar/)
*   [bottomnavigation/](https://github.com/material-components/material-components-android/tree/master/lib/java/com/google/android/material/bottomnavigation/)
*   [button/](https://github.com/material-components/material-components-android/tree/master/lib/java/com/google/android/material/button/)

Design component subpackages usable in the releases are utilizing the public API to be exposed to the user of the library . Classes in **[internal/](https://github.com/material-components/material-components-android/tree/master/lib/java/com/google/android/material/internal)** are part of the protected API and are used to
support the public API classes.

In addition, other subpackages who are siblings with the component subpackages can also play helpful roles in supporting components (like [math/](https://github.com/material-components/material-components-android/tree/master/lib/java/com/google/android/material/math) and [stateful/](https://github.com/material-components/material-components-android/tree/master/lib/java/com/google/android/material/stateful)).

If you are curious about specific components, we provide documentation for each available component in the [docs/components/](https://github.com/material-components/material-components-android/tree/master/docs/components) directory.
