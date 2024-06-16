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

        CmdUtil.execute(cmd = "su -c ls -l /dev/block/by-name | cut -d ' ' -f 8-", onInfo = {
            partitions.add(it)
        })

        return partitions
    }
}