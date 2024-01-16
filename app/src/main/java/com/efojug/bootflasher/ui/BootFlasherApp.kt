package com.efojug.bootflasher.ui

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.efojug.bootflasher.R
import com.efojug.bootflasher.ui.theme.BootFlasherTheme
import com.efojug.bootflasher.util.CommandExecutor

@Composable
fun BootFlasherApp(
    modifier: Modifier = Modifier,
    bootFlasherAppViewModel: BootFlasherAppViewModel = viewModel(),
    aSlotOnly: Boolean
) {

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = stringResource(id = R.string.main_title),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        if (aSlotOnly) {
            Text(
                text = stringResource(id = R.string.a_slot_only_error_message),
                color = Color.Red,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyLarge
            )
        }

//        Text(
//            text = "${stringResource(id = R.string.current_slot)}: ${
//                SystemPropertiesUtil.getProperty(
//                    "ro.boot.slot_suffix",
//                )
//            }"
//        )

        FlashExportItem(
            slotName = if (aSlotOnly) "boot" else "boot_a",
            onFlash = {},
            onExport = {},
            modifier = Modifier.fillMaxWidth()
        )

        if (!aSlotOnly) {
            FlashExportItem(
                slotName = "boot_b",
                onFlash = {},
                onExport = {},
                modifier = Modifier.fillMaxWidth()
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = stringResource(id = R.string.application_log),
                style = MaterialTheme.typography.titleMedium
            )

            Card(modifier = Modifier.fillMaxSize()) {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth()
                ) {

                }
            }
        }
    }
}

@Composable
private fun SlotItem(modifier: Modifier = Modifier, slotName: String) {
    val context = LocalContext.current
    var slotPartition by remember {
        mutableStateOf("")
    }

    LaunchedEffect(Unit) {
        try {
            val commandOutputDetails =
                CommandExecutor.execute("readlink -f /dev/block/by-name/$slotName")
            slotPartition = commandOutputDetails.first().message
        } catch (e: Exception) {
            slotPartition = context.getString(R.string.error)
        }
    }

    Text(
        text = "${stringResource(id = R.string.partition)} $slotName: ${
            slotPartition.ifBlank {
                stringResource(id = R.string.loading)
            }
        }",
        modifier = modifier
    )
}

@Composable
private fun FlashExportItem(
    modifier: Modifier = Modifier, slotName: String, onFlash: () -> Unit, onExport: () -> Unit
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        SlotItem(slotName = slotName)

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            Button(onClick = { onFlash() }) {
                Text(text = "${stringResource(id = R.string.flash)} $slotName")
            }

            Button(onClick = { onExport() }) {
                Text(text = "${stringResource(id = R.string.export)} $slotName")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AppPreview() {
    BootFlasherTheme {
        Scaffold {
            BootFlasherApp(modifier = Modifier.padding(it), aSlotOnly = false)
        }
    }
}

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL
)
@Composable
private fun AppDarkPreview() {
    BootFlasherTheme {
        Scaffold {
            BootFlasherApp(modifier = Modifier.padding(it), aSlotOnly = true)
        }
    }
}