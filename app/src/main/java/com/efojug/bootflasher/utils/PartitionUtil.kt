package com.efojug.bootflasher.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object PartitionUtil {
    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    @JvmStatic
    fun performIOOperationAsync(onResult: (String) -> Unit) {
        coroutineScope.launch(Dispatchers.IO) {
            val result = getPartitionsList()
            withContext(Dispatchers.Main) {
                onResult(result)
            }
        }
    }

    private suspend fun getPartitionsList(): String {
        val partitions = StringBuilder()

        CmdUtil.execute(cmd = "su -c find /dev/block/by-name/* -exec sh -c 'echo \"\$(basename \"\$1\") -> \$(readlink -f \"\$1\")\"' _ {} \\;", onInfo = {
            partitions.append(it)
        })
        return partitions.toString()
    }
}