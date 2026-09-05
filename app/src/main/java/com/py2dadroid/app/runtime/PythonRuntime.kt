package com.py2dadroid.app.runtime

import kotlinx.coroutines.flow.StateFlow

interface PythonRuntime {
    val state: StateFlow<PythonRuntimeState>

    suspend fun start(): PythonRuntimeResult<Unit>

    suspend fun evaluate(
        expression: String
    ): PythonRuntimeResult<String>

    suspend fun execute(
        code: String
    ): PythonRuntimeResult<Unit>

    suspend fun runFile(
        path: String,
        args: List<String> = emptyList()
    ): PythonRuntimeResult<PythonExecutionResult>

    suspend fun stop(): PythonRuntimeResult<Unit>
}
