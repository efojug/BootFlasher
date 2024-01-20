package com.efojug.bootflasher.ui

import android.app.Activity
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.efojug.bootflasher.ui.theme.SlotState

@Composable
fun BootFlasherApp(
    modifier: Modifier = Modifier,
) {
    val appViewModel: BootFlasherAppViewModel = viewModel()
    val state by appViewModel.uiState.collectAsState()
    val aSlotOnly = state.isASlotOnly

    if (state.showConfirmDialog) {
        AlertDialog(
            title = {
                Text(text = stringResource(id = R.string.confirm_title))
            },
            text = {
                Text(
                    text = stringResource(
                        id = R.string.confirm_to_flash,
                        state.selectedFilePath,
                        (state.flashSlot.slotState as SlotState.Success).partitionDirectory
                    )
                )
            },
            onDismissRequest = {
                appViewModel.cancelDialog()
            },
            confirmButton = {
                Button(onClick = { appViewModel.confirmToFlashImage() }) {
                    Text(text = stringResource(id = R.string.confirm))
                }
            },
            dismissButton = {
                Button(onClick = { appViewModel.cancelDialog() }) {
                    Text(text = stringResource(id = R.string.cancel))
                }
            }
        )
    }

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

        if (state.isLoading) {
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
            } else {
                Text(
                    text = "${stringResource(id = R.string.current_slot)}: ${state.currentSlot.slotName}",
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }

            Column {
                state.slotDetails.forEach {
                    when (it.slotState) {
                        is SlotState.Error -> {
                            Text(
                                text = stringResource(
                                    id = R.string.unable_read_slot_path,
                                    it.slotName
                                )
                            )
                        }

                        is SlotState.Success -> {
                            FlashExportItem(
                                slotDetail = it,
                                onFlash = {
                                    appViewModel.flashSlotImage(it)
                                },
                                onExport = {
                                    appViewModel.exportSlotImage(
                                        it.slotName,
                                        it.slotState.partitionDirectory
                                    )
                                }
                            )
                        }
                    }
                }
            }

            val launcher =
                rememberLauncherForActivityResult(contract = ActivityResultContracts.StartActivityForResult()) {
                    if (it.resultCode == Activity.RESULT_OK) {
                        val contentPath =
                            it.data?.data?.path ?: return@rememberLauncherForActivityResult
                        val path = contentPath.split(":")[1]

                        appViewModel.onFileSelected(path)
                    }
                }

            LaunchedEffect(key1 = state.showFilePicker) {
                if (state.showFilePicker) {
                    val intent = Intent(Intent.ACTION_GET_CONTENT)
                    intent.setType("*/*")
                    launcher.launch(intent)
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

            LogComponent(formatPattern = "HH:mm:ss", logs = appViewModel.logs)
        }
    }
}

@Composable
private fun SlotDetailItem(modifier: Modifier = Modifier, slotDetail: SlotDetail) {
    Text(
        text = "${stringResource(id = R.string.partition)} ${slotDetail.slotName}: ${(slotDetail.slotState as SlotState.Success).partitionDirectory}",
        modifier = modifier
    )
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