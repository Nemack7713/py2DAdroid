plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
}

val requestedAbi = providers.gradleProperty("abiFilter").orElse("arm64-v8a")
val pythonVersion = providers.gradleProperty("pyVersion").orElse("3.14")
val pythonVariant = providers.gradleProperty("pyVariant").orElse("standard")
val pythonRuntimeRoot = providers.gradleProperty("pyRuntimeRoot").orElse("")

val runtimeRoot = pythonRuntimeRoot.get()
val generatedPythonAssets = layout.buildDirectory.dir("generated/py2dPython/assets")
val generatedPythonJni = layout.buildDirectory.dir("generated/py2dPython/jniLibs")

val preparePythonAssets by tasks.registering(Sync::class) {
    onlyIf { runtimeRoot.isNotBlank() }
    doFirst {
        val stdlib = file("$runtimeRoot/lib/python${pythonVersion.get()}")
        require(stdlib.isDirectory) {
            "Verified CPython stdlib not found: $stdlib"
        }
    }
    from("$runtimeRoot/lib/python${pythonVersion.get()}") {
        into("python/lib/python${pythonVersion.get()}")
        exclude("**/__pycache__/**")
    }
    into(generatedPythonAssets)
}

val preparePythonJniLibs by tasks.registering(Sync::class) {
    onlyIf { runtimeRoot.isNotBlank() }
    doFirst {
        val libpython = file("$runtimeRoot/lib/libpython${pythonVersion.get()}.so")
        require(libpython.isFile) {
            "Verified CPython shared library not found: $libpython"
        }
    }
    from("$runtimeRoot/lib") {
        include("libpython*.*.so")
        include("lib*_python.so")
        into(requestedAbi.get())
    }
    into(generatedPythonJni)
}

tasks.named("preBuild") {
    dependsOn(preparePythonAssets, preparePythonJniLibs)
}

android {
    namespace = "com.py2dadroid.app"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.py2dadroid.app"
        minSdk = 26
        targetSdk = 37
        versionCode = 1
        versionName = "0.1.0"

        ndk {
            abiFilters += requestedAbi.get()
        }

        buildConfigField("String", "PYTHON_VERSION", "\"${pythonVersion.get()}\"")
        buildConfigField("String", "PYTHON_VARIANT", "\"${pythonVariant.get()}\"")
        buildConfigField("boolean", "PYTHON_RUNTIME_PACKAGED", runtimeRoot.isNotBlank().toString())

        externalNativeBuild {
            cmake {
                arguments += listOf(
                    "-DPY2D_PYTHON_VERSION=${pythonVersion.get()}",
                    "-DPY2D_PYTHON_VARIANT=${pythonVariant.get()}",
                    "-DPY2D_PYTHON_RUNTIME_ROOT=$runtimeRoot"
                )
                cppFlags += listOf("-std=c++17", "-Wall", "-Wextra", "-Werror")
            }
        }
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
            isMinifyEnabled = false
        }
        release {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }

    sourceSets {
        getByName("main") {
            assets.srcDir(generatedPythonAssets)
            jniLibs.srcDir(generatedPythonJni)
        }
    }

    androidResources {
        noCompress += "gz"
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2026.08.00")
    implementation(composeBom)
    implementation("androidx.activity:activity-compose:1.13.0")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.10.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")
    debugImplementation("androidx.compose.ui:ui-tooling")
    testImplementation("junit:junit:4.13.2")
}
