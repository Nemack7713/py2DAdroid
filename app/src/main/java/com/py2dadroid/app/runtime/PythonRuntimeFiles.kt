package com.py2dadroid.app.runtime

import android.content.Context
import java.io.File

internal object PythonRuntimeFiles {
    fun prepare(
        context: Context,
        runtimeId: String
    ): File {
        val published = File(context.filesDir, "python-runtime")
        val marker = File(published, ".runtime-id")

        if (
            published.isDirectory &&
            marker.isFile &&
            marker.readText() == runtimeId
        ) {
            return published
        }

        val staging = File(context.filesDir, ".python-runtime-staging")
        if (staging.exists()) {
            check(staging.deleteRecursively()) {
                "Unable to clear stale CPython runtime staging directory"
            }
        }
        check(staging.mkdirs()) {
            "Unable to create CPython runtime staging directory"
        }

        extractDirectory(context, "python", staging)
        File(staging, ".runtime-id").writeText(runtimeId)

        if (published.exists()) {
            check(published.deleteRecursively()) {
                "Unable to replace previous CPython runtime"
            }
        }
        check(staging.renameTo(published)) {
            "Unable to publish CPython runtime files"
        }

        return published
    }

    private fun extractDirectory(
        context: Context,
        assetPath: String,
        destination: File
    ) {
        val children =
            context.assets.list(assetPath)
                ?: error("Unable to list CPython asset path: $assetPath")

        if (children.isEmpty()) {
            destination.parentFile?.mkdirs()
            context.assets.open(assetPath).use { input ->
                destination.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            return
        }

        check(destination.isDirectory || destination.mkdirs()) {
            "Unable to create CPython runtime directory: $destination"
        }

        for (child in children) {
            extractDirectory(
                context,
                "$assetPath/$child",
                File(destination, child)
            )
        }
    }
}
