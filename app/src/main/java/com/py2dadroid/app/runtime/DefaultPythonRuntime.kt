package com.py2dadroid.app.runtime

import android.content.Context
import com.py2dadroid.app.BuildConfig
import com.py2dadroid.app.nativebridge.CpythonNativeBridge
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

class DefaultPythonRuntime(
    private val context: Context
) : PythonRuntime {
    private val mutableState =
        MutableStateFlow<PythonRuntimeState>(PythonRuntimeState.Uninitialized)

    override val state: StateFlow<PythonRuntimeState> =
        mutableState.asStateFlow()

    override suspend fun start(): PythonRuntimeResult<Unit> =
        withContext(Dispatchers.IO) {
            val runtimeId =
                "${BuildConfig.PYTHON_VARIANT}-${BuildConfig.PYTHON_VERSION}"
            mutableState.value = PythonRuntimeState.Starting(runtimeId)

            if (!BuildConfig.PYTHON_RUNTIME_PACKAGED) {
                val reason = "No verified CPython runtime was packaged into this build"
                mutableState.value = PythonRuntimeState.Unavailable(reason)
                return@withContext PythonRuntimeResult.Pending(reason)
            }

            val home = try {
                PythonRuntimeFiles.prepare(context, runtimeId)
            } catch (failure: Exception) {
                val reason = failure.message ?: "Unable to prepare CPython runtime files"
                mutableState.value =
                    PythonRuntimeState.Failed(FailureStage.RUNTIME_PACK, reason)
                return@withContext
                    PythonRuntimeResult.Failure(FailureStage.RUNTIME_PACK, reason)
            }

            val error = try {
                CpythonNativeBridge.initialize(
                    home.absolutePath,
                    context.cacheDir.absolutePath
                )
            } catch (failure: UnsatisfiedLinkError) {
                val reason =
                    failure.message ?: "Unable to load py2DAdroid native host"
                mutableState.value =
                    PythonRuntimeState.Failed(FailureStage.NATIVE_LIBRARY, reason)
                return@withContext
                    PythonRuntimeResult.Failure(FailureStage.NATIVE_LIBRARY, reason)
            }

            if (error.isNotEmpty()) {
                mutableState.value =
                    PythonRuntimeState.Failed(FailureStage.INITIALIZATION, error)
                return@withContext
                    PythonRuntimeResult.Failure(FailureStage.INITIALIZATION, error)
            }

            mutableState.value = PythonRuntimeState.Ready(
                pythonVersion = BuildConfig.PYTHON_VERSION,
                runtimeId = runtimeId
            )
            PythonRuntimeResult.Success(Unit)
        }

    override suspend fun evaluate(
        expression: String
    ): PythonRuntimeResult<String> =
        withContext(Dispatchers.Default) {
            if (state.value !is PythonRuntimeState.Ready) {
                return@withContext PythonRuntimeResult.Failure(
                    FailureStage.EXECUTION,
                    "CPython runtime is not ready"
                )
            }

            val result = CpythonNativeBridge.evaluate(expression)
            if (result.startsWith("ERROR:")) {
                PythonRuntimeResult.Failure(
                    FailureStage.EXECUTION,
                    result.removePrefix("ERROR:")
                )
            } else {
                PythonRuntimeResult.Success(result)
            }
        }

    override suspend fun execute(
        code: String
    ): PythonRuntimeResult<Unit> =
        PythonRuntimeResult.Pending(
            "Direct CPython statement execution is the next runtime operation"
        )

    override suspend fun runFile(
        path: String,
        args: List<String>
    ): PythonRuntimeResult<PythonExecutionResult> =
        PythonRuntimeResult.Pending(
            "Direct CPython file execution is the next runtime operation"
        )

    override suspend fun stop(): PythonRuntimeResult<Unit> =
        withContext(Dispatchers.Default) {
            if (state.value is PythonRuntimeState.Uninitialized ||
                state.value is PythonRuntimeState.Unavailable ||
                state.value is PythonRuntimeState.Stopped
            ) {
                mutableState.value = PythonRuntimeState.Stopped
                return@withContext PythonRuntimeResult.Success(Unit)
            }

            val error = CpythonNativeBridge.shutdown()
            if (error.isNotEmpty()) {
                mutableState.value =
                    PythonRuntimeState.Failed(FailureStage.SHUTDOWN, error)
                PythonRuntimeResult.Failure(FailureStage.SHUTDOWN, error)
            } else {
                mutableState.value = PythonRuntimeState.Stopped
                PythonRuntimeResult.Success(Unit)
            }
        }
