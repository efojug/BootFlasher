package com.efojug.bootflasher.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.efojug.bootflasher.R
import com.efojug.bootflasher.ui.component.LogComponent
import com.efojug.bootflasher.ui.theme.SlotDetail

@Composable
fun BootFlasherApp(
    modifier: Modifier = Modifier,
) {
    val bootFlasherAppViewModel: BootFlasherAppViewModel = viewModel()
    val state by bootFlasherAppViewModel.uiState.collectAsState()
    val aSlotOnly = state.isASlotOnly

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

        if (state.loadingDeviceSlotDetails) {
            CircularProgressIndicator(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .width(32.dp)
            )
        } else {
            if (aSlotOnly) {
                Text(
                    text = stringResource(id = R.string.a_slot_only_error_message),
                    color = Color.Red,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            Text(text = "${stringResource(id = R.string.current_slot)}: ${state.currentSlot.slotName}")

            Column {
                state.slotDetails.forEach {
                    FlashExportItem(
                        slotDetail = it,
                        onFlash = {},
                        onExport = {}
                    )
                }
            }
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

            LogComponent(formatPattern = "HH:mm:ss", logs = bootFlasherAppViewModel.logs)
        }
    }
}

@Composable
private fun SlotDetailItem(modifier: Modifier = Modifier, slotDetail: SlotDetail) {
    when (slotDetail.slotState) {
        is com.efojug.bootflasher.ui.theme.SlotState.Error -> {
            Text(text = "${stringResource(id = R.string.partition)} ${slotDetail.slotName}: ${slotDetail.slotState.message}")
        }

        is com.efojug.bootflasher.ui.theme.SlotState.Success -> {
            Text(
                text = "${stringResource(id = R.string.partition)} ${slotDetail.slotName}: ${slotDetail.slotState}",
                modifier = modifier
            )
        }
    }
}

@Composable
private fun FlashExportItem(
    modifier: Modifier = Modifier, slotDetail: SlotDetail, onFlash: () -> Unit, onExport: () -> Unit
) {
    val slotName = slotDetail.slotName

    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        SlotDetailItem(slotDetail = slotDetail)

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