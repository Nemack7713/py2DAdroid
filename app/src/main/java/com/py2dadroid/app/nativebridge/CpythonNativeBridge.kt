package com.py2dadroid.app.nativebridge

internal object CpythonNativeBridge {
    init {
        System.loadLibrary("py2d_host")
    }

    external fun probeRuntime(): String
}
