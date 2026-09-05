package com.py2dadroid.app.runtime

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class Py2DAdroidRuntimeService : Service() {
    private val serviceScope =
        CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val binder = RuntimeBinder()

    lateinit var runtime: PythonRuntime
        private set

    inner class RuntimeBinder : Binder() {
        fun service(): Py2DAdroidRuntimeService =
            this@Py2DAdroidRuntimeService
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onCreate() {
        super.onCreate()
        runtime = DefaultPythonRuntime(applicationContext)
        serviceScope.launch {
            runtime.start()
        }
    }

    override fun onDestroy() {
        if (::runtime.isInitialized) {
            runBlocking {
                runtime.stop()
            }
        }
        serviceScope.cancel()
        super.onDestroy()
    }
}
