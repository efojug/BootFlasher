package com.efojug.bootflasher.ui.theme

sealed class SlotState {
    data class Success(val partitionDirectory: String) : SlotState()

    data class Error(val message: String) : SlotState()
}

data class SlotDetail(
    val slotName: String,
    val slotState: SlotState
) {
    companion object {
        val Empty = SlotDetail("", SlotState.Error(""))
    }
}

data class BootFlasherState(
    val isLoading: Boolean = true,
    val currentSlot: SlotDetail = SlotDetail.Empty,
    val slotDetails: List<SlotDetail> = emptyList(),
    val isASlotOnly: Boolean = false,
    val isFlashingImage: Boolean = false,
    val showConfirmDialog: Boolean = false,

    val flashSlot: SlotDetail = SlotDetail.Empty,

    val showFilePicker: Boolean = false,
    val selectedFile: Boolean = false,
    val selectedFilePath: String = ""
)