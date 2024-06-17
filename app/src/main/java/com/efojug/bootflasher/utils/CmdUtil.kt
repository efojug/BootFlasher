package com.efojug.bootflasher.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object CmdUtil {
    suspend fun execute(
        cmd: String,
        onInfo: ((String) -> Unit)? = null,
        onError: ((String) -> Unit)? = null
    ): Unit = withContext(Dispatchers.IO) {
        val process = Runtime.getRuntime().exec(cmd)

        onError?.let { onError ->
            launch {
                process.errorStream.bufferedReader().forEachLine {
                    onError(it)
                }
            }
        }

        onInfo?.let { onInfo ->
            process.inputStream.bufferedReader().forEachLine {
                onInfo(it + "\n")
            }
        }
    }
}