package com.py2dadroid.app.nativebridge

internal object CpythonNativeBridge {
    init {
        System.loadLibrary("py2d_host")
    }

    external fun initialize(
        pythonHome: String,
        cacheDir: String
    ): String

    external fun evaluate(expression: String): String

    external fun shutdown(): String
}
