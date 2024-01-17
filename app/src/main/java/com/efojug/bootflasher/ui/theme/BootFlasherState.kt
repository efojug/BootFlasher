package com.efojug.bootflasher.ui.theme

sealed class SlotState {
    data class Success(val partitionDirectory: String) : SlotState()

    data class Error(val message: String) : SlotState()
}

data class SlotDetail(
    val slotName: String,
    val slotState: SlotState
)

data class BootFlasherState(
    val loadingDeviceSlotDetails: Boolean = true,
    val currentSlot: SlotDetail = SlotDetail("", SlotState.Error("")),
    val isASlotOnly: Boolean = false,
    val slotDetails: List<SlotDetail> = emptyList(),
)