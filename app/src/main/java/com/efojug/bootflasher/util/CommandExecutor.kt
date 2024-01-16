package com.efojug.bootflasher.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class CommandOutputDetail(
    val time: Long,
    val message: String
)

object CommandExecutor {
    @JvmStatic
    suspend
    fun execute(cmd: String): List<CommandOutputDetail> = withContext(Dispatchers.IO) {
        val process = Runtime.getRuntime().exec("su -c $cmd")
        val result = mutableListOf<CommandOutputDetail>()

        launch {
            process.errorStream.bufferedReader().forEachLine {
                throw Exception(it)
//                result.add(CommandOutputDetail(System.currentTimeMillis(), it))
            }
        }

        process.inputStream.bufferedReader().forEachLine {
            result.add(CommandOutputDetail(System.currentTimeMillis(), it))
        }

        result
    }
}