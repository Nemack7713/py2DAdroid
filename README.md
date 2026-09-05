# py2DAdroid

py2DAdroid is a modern Android-native host for direct CPython embedding.

Chaquopy is intentionally not part of the project.

## v0.1 architecture

```text
Jetpack Compose
      ↓
Kotlin runtime contract
      ↓
service-owned runtime lifecycle
      ↓
JNI control bridge
      ↓
verified direct CPython Android runtime
```

The current native probe is intentionally fail-visible. It reports that a
verified CPython runtime has not yet been wired rather than pretending that
Python execution is available.

## Build baseline

- Android Gradle Plugin 9.4
- built-in Kotlin through AGP 9
- Compose compiler plugin
- Java 17
- compile/target SDK 37
- minSdk 26
- arm64-v8a authoritative ABI
- CMake 3.22.1
- C++17
- standard CPython 3.14 intended first runtime

## Build properties

```text
-PpyVersion=3.14
-PpyVariant=standard
-PabiFilter=arm64-v8a
-PpyRuntimeRoot=/absolute/path/to/verified/runtime
```

## Experimental combinations

pybind11 and free-threaded CPython are independently opt-in. Their combination
is rejected by CMake until that specific pairing has an explicit validation
record.

## Next runtime proof

The next slice is deliberately narrow:

```text
Kotlin
  ↓ JNI
C++ host
  ↓
standard CPython 3.14
  ↓
eval("2 + 2")
  ↓
"4"
  ↓ JNI
Compose
```

No package manager, WorkManager layer, free-threaded runtime, pybind11 bridge,
or release automation is required for that proof.
