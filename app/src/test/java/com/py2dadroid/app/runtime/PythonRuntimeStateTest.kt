package com.py2dadroid.app.runtime

import org.junit.Assert.assertEquals
import org.junit.Test

class PythonRuntimeStateTest {
    @Test
    fun readyStateCarriesRuntimeIdentity() {
        val state = PythonRuntimeState.Ready(
            pythonVersion = "3.14",
            runtimeId = "standard-3.14"
        )

        assertEquals("3.14", state.pythonVersion)
        assertEquals("standard-3.14", state.runtimeId)
    }
}
