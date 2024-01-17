package com.efojug.bootflasher.ui.component

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.efojug.bootflasher.log.LogMessage
import java.text.SimpleDateFormat

@Composable
fun LogComponent(modifier: Modifier = Modifier, formatPattern: String, logs: List<LogMessage>) {
    val formatter = remember {
        SimpleDateFormat(formatPattern)
    }

    Card(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(6.dp)
        ) {
            items(logs) {
                Text(text = "${formatter.format(it.time)} - ${it.type}: ${it.message}")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LogComponentPreview() {
    LogComponent(formatPattern = "HH:mm:ss", logs = listOf(LogMessage(1, "Hello")))
}