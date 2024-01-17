package com.efojug.bootflasher.util

import com.efojug.bootflasher.log.LogMessage
import com.efojug.bootflasher.log.LogMessageType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object CommandExecutor {
    @JvmStatic
    suspend
    fun execute(cmd: String): List<LogMessage> = withContext(Dispatchers.IO) {
        val process = Runtime.getRuntime().exec("su -c $cmd")
        val result = mutableListOf<LogMessage>()

        launch {
            process.errorStream.bufferedReader().forEachLine {
                result.add(LogMessage(System.currentTimeMillis(), it, LogMessageType.ERROR))
            }
        }

        process.inputStream.bufferedReader().forEachLine {
            result.add(LogMessage(System.currentTimeMillis(), it, LogMessageType.INFO))
        }

        result
    }
}