package com.py2dadroid.app.runtime

import com.py2dadroid.app.BuildConfig
import com.py2dadroid.app.nativebridge.CpythonNativeBridge
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class DefaultPythonRuntime : PythonRuntime {
    private val mutableState =
        MutableStateFlow<PythonRuntimeState>(PythonRuntimeState.Uninitialized)

    override val state: StateFlow<PythonRuntimeState> =
        mutableState.asStateFlow()

    override suspend fun start(): PythonRuntimeResult<Unit> {
        val runtimeId =
            "${BuildConfig.PYTHON_VARIANT}-${BuildConfig.PYTHON_VERSION}"

        mutableState.value = PythonRuntimeState.Starting(runtimeId)

        val error = try {
            CpythonNativeBridge.probeRuntime()
        } catch (failure: UnsatisfiedLinkError) {
            mutableState.value = PythonRuntimeState.Failed(
                FailureStage.NATIVE_LIBRARY,
                failure.message ?: "Unable to load py2DAdroid native host"
            )
            return PythonRuntimeResult.Failure(
                FailureStage.NATIVE_LIBRARY,
                failure.message ?: "Unable to load py2DAdroid native host"
            )
        }

        if (error.isNotEmpty()) {
            mutableState.value = PythonRuntimeState.Unavailable(error)
            return PythonRuntimeResult.Pending(error)
        }

        mutableState.value = PythonRuntimeState.Ready(
            pythonVersion = BuildConfig.PYTHON_VERSION,
            runtimeId = runtimeId
        )
        return PythonRuntimeResult.Success(Unit)
    }

    override suspend fun evaluate(
        expression: String
    ): PythonRuntimeResult<String> =
        PythonRuntimeResult.Pending(
            "Direct CPython evaluation is not wired yet"
        )

    override suspend fun execute(
        code: String
    ): PythonRuntimeResult<Unit> =
        PythonRuntimeResult.Pending(
            "Direct CPython execution is not wired yet"
        )

    override suspend fun runFile(
        path: String,
        args: List<String>
    ): PythonRuntimeResult<PythonExecutionResult> =
        PythonRuntimeResult.Pending(
            "Direct CPython file execution is not wired yet"
        )

    override suspend fun stop(): PythonRuntimeResult<Unit> {
        mutableState.value = PythonRuntimeState.Stopped
        return PythonRuntimeResult.Success(Unit)
    }
}
