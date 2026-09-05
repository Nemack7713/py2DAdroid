package com.py2dadroid.app.app

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.py2dadroid.app.runtime.Py2DAdroidRuntimeService
import com.py2dadroid.app.runtime.PythonRuntimeState
import com.py2dadroid.app.runtime.RuntimeServiceConnection
import kotlinx.coroutines.flow.MutableStateFlow

class MainActivity : ComponentActivity() {
    private val runtimeConnection = RuntimeServiceConnection()

    override fun onStart() {
        super.onStart()
        bindService(
            Intent(this, Py2DAdroidRuntimeService::class.java),
            runtimeConnection,
            Context.BIND_AUTO_CREATE
        )
    }

    override fun onStop() {
        unbindService(runtimeConnection)
        super.onStop()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                val service by
                    runtimeConnection.service.collectAsStateWithLifecycle()

                val fallback = remember {
                    MutableStateFlow<PythonRuntimeState>(
                        PythonRuntimeState.Uninitialized
                    )
                }

                val state by
                    (service?.runtime?.state ?: fallback)
                        .collectAsStateWithLifecycle()

                Surface(modifier = Modifier.fillMaxSize()) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        verticalArrangement =
                            Arrangement.spacedBy(
                                16.dp,
                                Alignment.CenterVertically
                            ),
                        horizontalAlignment =
                            Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "py2DAdroid",
                            style = MaterialTheme.typography.headlineMedium
                        )
                        Text(text = "Runtime state: $state")
                        Text(
                            text =
                                "Direct CPython Android runtime wiring is the next slice."
                        )
                    }
                }
            }
        }
    }
}
