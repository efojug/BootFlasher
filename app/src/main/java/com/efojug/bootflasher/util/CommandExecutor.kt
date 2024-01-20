package com.efojug.bootflasher.util

import com.efojug.bootflasher.log.LogMessage
import com.efojug.bootflasher.log.LogMessageType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.Flow

object CommandExecutor {
    @JvmStatic
    suspend
    fun execute(cmd: String, onLog: (LogMessage) -> Unit = {}): List<LogMessage> = coroutineScope {
        val process = Runtime.getRuntime().exec("su -c $cmd")
        val result = mutableListOf<LogMessage>()

        launch {
            process.errorStream.bufferedReader().forEachLine {
                val logMessage = LogMessage(System.currentTimeMillis(), it, LogMessageType.ERROR)
                onLog(logMessage)
                result.add(logMessage)
            }
        }

        process.inputStream.bufferedReader().forEachLine {
            val logMessage = LogMessage(System.currentTimeMillis(), it, LogMessageType.INFO)
            onLog(logMessage)
            result.add(logMessage)
        }

        result
    }
}