package com.py2dadroid.app.runtime

sealed interface PythonRuntimeState {
    data object Uninitialized : PythonRuntimeState

    data class Starting(
        val runtimeId: String
    ) : PythonRuntimeState

    data class Ready(
        val pythonVersion: String,
        val runtimeId: String
    ) : PythonRuntimeState

    data class Unavailable(
        val reason: String
    ) : PythonRuntimeState

    data class Failed(
        val stage: FailureStage,
        val reason: String
    ) : PythonRuntimeState

    data object Stopped : PythonRuntimeState
}

enum class FailureStage {
    RUNTIME_PACK,
    NATIVE_LIBRARY,
    INITIALIZATION,
    EXECUTION,
    SHUTDOWN
}
