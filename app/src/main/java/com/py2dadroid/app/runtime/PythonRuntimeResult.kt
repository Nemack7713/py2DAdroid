package com.py2dadroid.app.runtime

sealed interface PythonRuntimeResult<out T> {
    data class Success<T>(val value: T) : PythonRuntimeResult<T>
    data class Pending(val reason: String) : PythonRuntimeResult<Nothing>
    data class Failure(
        val stage: FailureStage,
        val reason: String
    ) : PythonRuntimeResult<Nothing>
}

data class PythonExecutionResult(
    val stdout: String,
    val stderr: String,
    val exitCode: Int
)
