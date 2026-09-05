# py2DAdroid

py2DAdroid is a modern Android-native host for direct CPython embedding.

Chaquopy is intentionally not part of the project.

## Direct CPython proof

The first authoritative runtime is the official Python 3.14.7 Android
embeddable package from python.org for arm64-v8a. The resolver pins its
published SHA-256 before extraction.

```text
Compose
  ↓
service-owned Kotlin runtime
  ↓
JNI
  ↓
PyConfig / Py_InitializeFromConfig
  ↓
official CPython 3.14
```

The package contributes `libpython3.14.so` and dependent
`lib*_python.so` libraries to JNI libs, and the Python 3.14 standard library
to APK assets. On-device, the standard library is extracted to app-private
storage before initialization.

The first execution target is deliberately narrow:

```text
eval("2 + 2") → "4"
```

Build properties:

```text
-PpyVersion=3.14
-PpyVariant=standard
-PabiFilter=arm64-v8a
-PpyRuntimeRoot=/absolute/path/to/verified/runtime/prefix
```

pybind11, free-threaded CPython, package management, WorkManager and automatic
GitHub Actions triggers remain outside this first proof.
