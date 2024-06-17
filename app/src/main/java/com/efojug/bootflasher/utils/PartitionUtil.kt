package com.efojug.bootflasher.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object PartitionUtil {
    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    @JvmStatic
    fun performIOOperationAsync(onResult: (List<String>) -> Unit) {
        coroutineScope.launch(Dispatchers.IO) {
            val result = getPartitionsList()
            withContext(Dispatchers.Main) {
                onResult(result)
            }
        }
    }

    private suspend fun getPartitionsList(): List<String> {
        val partitions = mutableListOf<String>()

        CmdUtil.execute(cmd = "su -c find /dev/block/by-name/* -type l -print0 | xargs -0 su -c 'for arg; do echo \"\$(basename \"\$arg\") -> \$(readlink -f \"\$arg\")\"; done'", onInfo = {
            partitions.add(it)
        })

        return partitions
    }
}