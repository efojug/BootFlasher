package com.efojug.bootflasher.ui

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.efojug.bootflasher.log.LogMessage
import com.efojug.bootflasher.log.LogMessageType
import com.efojug.bootflasher.ui.theme.BootFlasherState
import com.efojug.bootflasher.ui.theme.SlotDetail
import com.efojug.bootflasher.ui.theme.SlotState
import com.efojug.bootflasher.util.CommandExecutor
import com.efojug.bootflasher.util.SystemPropertiesUtil
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope

class BootFlasherAppViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(BootFlasherState())
    val uiState: StateFlow<BootFlasherState> = _uiState

    var logs = mutableStateListOf<LogMessage>()
        private set

    init {
        viewModelScope.launch {
            loadSlotDetails()
        }
    }

    private suspend fun loadSlotDetails() = supervisorScope {
        val aSlotOnly = SystemPropertiesUtil.getProperty("ro.build.ab_update") == "false"

        if (aSlotOnly) {
            val slotDetail = loadSlotDetail("boot")

            _uiState.update {
                it.copy(
                    loadingDeviceSlotDetails = false,
                    isASlotOnly = true,
                    currentSlot = slotDetail,
                    slotDetails = listOf(slotDetail)
                )
            }
        } else {
            val slotDetails = awaitAll(
                async { loadSlotDetail("boot_a") },
                async { loadSlotDetail("boot_b") }
            )

            val currentSlotName = SystemPropertiesUtil.getProperty("ro.boot.slot_suffix")

            _uiState.update {
                it.copy(
                    loadingDeviceSlotDetails = false,
                    isASlotOnly = false,
                    currentSlot = slotDetails.first { it.slotName == currentSlotName },
                    slotDetails = slotDetails
                )
            }
        }
    }

    private suspend fun loadSlotDetail(slotName: String): SlotDetail {
        val outputDetails = CommandExecutor.execute("readlink -f /dev/block/by-name/$slotName")

        outputDetails.forEach {
            if (it.type != LogMessageType.INFO) {
                logs.add(it)
            }
        }

        val slotState = if (outputDetails.size == 1) {
            SlotState.Success(outputDetails.first().message)
        } else {
            SlotState.Error("Unable to read slot information")
        }

        return SlotDetail(slotName, slotState)
    }
}