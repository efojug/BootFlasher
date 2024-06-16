package com.efojug.bootflasher.utils

import com.efojug.bootflasher.FirstFragment
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

    private fun getPartitionsList(): String {
        val str: String = FirstFragment().exeCmd("ls -l /dev/block/by-name", false)
        var tmp = StringBuilder()
        val res = StringBuilder()
        var space = 0
        var output = false
        for (i in str.indices) {
            if (output) {
                tmp.append(str[i])
                continue
            }
            if (str[i] == '\n') {
                res.append(tmp.toString())
                space = 0
                output = false
                tmp = StringBuilder()
                continue
            }
            if (!output) {
                if (str[i] == ' ' && space < 7) space++
                if (space == 7) output = true
            }
        }
        return res.toString()
    }
}