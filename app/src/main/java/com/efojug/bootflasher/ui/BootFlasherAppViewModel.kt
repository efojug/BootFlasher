package com.efojug.bootflasher.ui

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.efojug.bootflasher.log.LogMessage
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
import java.text.SimpleDateFormat
import java.util.Date
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class BootFlasherAppViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(BootFlasherState())
    val uiState: StateFlow<BootFlasherState> = _uiState

    var logs = mutableStateListOf<LogMessage>()
        private set

    private var flashTaskConfirmContinuation: Continuation<Unit>? = null

    init {
        viewModelScope.launch {
            loadSlotDetails()
        }
    }

    fun cancelDialog() {
        _uiState.update {
            it.copy(
                showConfirmDialog = false
            )
        }
    }

    fun confirmToFlashImage() {
        _uiState.update {
            it.copy(
                showConfirmDialog = false,
                isLoading = true
            )
        }
        flashTaskConfirmContinuation?.resume(Unit)
    }

    fun flashSlotImage(slotDetail: SlotDetail) {
        viewModelScope.launch {
            val state = uiState.value

            if (slotDetail.slotState !is SlotState.Success) {
                return@launch
            }

            suspendCoroutine {
                flashTaskConfirmContinuation = it

                _uiState.update {
                    it.copy(
                        flashSlot = slotDetail,
                        showConfirmDialog = true,
                        showFilePicker = true
                    )
                }
            }

            accessSlot(slotDetail.slotState.partitionDirectory)
            println("Flash ${state.selectedFilePath} to ${slotDetail.slotState.partitionDirectory}")
            copy(state.selectedFilePath, slotDetail.slotState.partitionDirectory)

            _uiState.update {
                it.copy(
                    isLoading = false,
                    selectedFile = false
                )
            }
        }
    }

    fun onFileSelected(path: String) {
        _uiState.update {
            it.copy(
                showFilePicker = false,
                selectedFile = true,
                selectedFilePath = path
            )
        }
    }

    fun exportSlotImage(slotName: String, partitionDirectory: String) {
        viewModelScope.launch {
            accessSlot(partitionDirectory)

            copy(
                partitionDirectory,
                "/storage/emulated/0/Download/${slotName}_${
                    SimpleDateFormat("yyyyMMddHHmmss").format(Date())
                }.img"
            )
        }
    }

    private suspend fun accessSlot(partitionDirectory: String) {
        CommandExecutor.execute("blockdev --setrw $partitionDirectory") { logs.add(it) }
    }

    private suspend fun copy(from: String, to: String) {
        viewModelScope.launch {
            CommandExecutor.execute(
                "dd if=$from of=$to bs=4M;sync"
            ) {
                logs.add(it)
            }
        }
    }

    private suspend fun loadSlotDetails() = supervisorScope {
        val aSlotOnly = SystemPropertiesUtil.getProperty("ro.build.ab_update") != "true"

        if (aSlotOnly) {
            val slotDetail = loadSlotDetail("boot")

            _uiState.update {
                it.copy(
                    isLoading = false,
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
                    isLoading = false,
                    isASlotOnly = false,
                    currentSlot = slotDetails.first { it.slotName.contains(currentSlotName) },
                    slotDetails = slotDetails
                )
            }
        }
    }

    private suspend fun loadSlotDetail(slotName: String): SlotDetail {
        val outputDetails = CommandExecutor.execute("readlink -f /dev/block/by-name/$slotName")

        val slotState = if (outputDetails.size == 1) {
            SlotState.Success(outputDetails.first().message)
        } else {
            SlotState.Error("Unable to read slot information")
        }

        return SlotDetail(slotName, slotState)
    }
}