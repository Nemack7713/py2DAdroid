plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
}

val requestedAbi = providers.gradleProperty("abiFilter").orElse("arm64-v8a")
val pythonVersion = providers.gradleProperty("pyVersion").orElse("3.14")
val pythonVariant = providers.gradleProperty("pyVariant").orElse("standard")
val pythonRuntimeRoot = providers.gradleProperty("pyRuntimeRoot").orElse("")

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

        externalNativeBuild {
            cmake {
                arguments += listOf(
                    "-DPY2D_PYTHON_VERSION=${pythonVersion.get()}",
                    "-DPY2D_PYTHON_VARIANT=${pythonVariant.get()}",
                    "-DPY2D_PYTHON_RUNTIME_ROOT=${pythonRuntimeRoot.get()}"
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
