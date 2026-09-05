package com.py2dadroid.app.runtime

import android.content.ComponentName
import android.content.ServiceConnection
import android.os.IBinder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class RuntimeServiceConnection : ServiceConnection {
    private val mutableService =
        MutableStateFlow<Py2DAdroidRuntimeService?>(null)

    val service: StateFlow<Py2DAdroidRuntimeService?> =
        mutableService.asStateFlow()

    override fun onServiceConnected(
        name: ComponentName?,
        binder: IBinder?
    ) {
        mutableService.value =
            (binder as? Py2DAdroidRuntimeService.RuntimeBinder)?.service()
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        mutableService.value = null
    }
}
