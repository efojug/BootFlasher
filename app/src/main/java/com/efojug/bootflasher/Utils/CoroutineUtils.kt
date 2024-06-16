package com.efojug.bootflasher.Utils

import com.efojug.bootflasher.FirstFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object CoroutineUtils {
    @JvmStatic
    fun performIOOperationAsync(onResult: (String) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            val result = getPartitionsList()
            CoroutineScope(Dispatchers.Main).launch {
                onResult(result)
            }
        }
    }

    private suspend fun getPartitionsList(): String {
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