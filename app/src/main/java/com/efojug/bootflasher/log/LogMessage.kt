package com.efojug.bootflasher.log

data class LogMessage(
    val time: Long,
    val message: String,
    val type: LogMessageType = LogMessageType.INFO
)

enum class LogMessageType {
    INFO,
    WARNING,
    ERROR
}