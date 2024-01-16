package com.efojug.bootflasher

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.efojug.bootflasher.ui.BootFlasherApp
import com.efojug.bootflasher.ui.component.ErrorComponent
import com.efojug.bootflasher.ui.theme.BootFlasherTheme
import com.efojug.bootflasher.util.SystemPropertiesUtil

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val unlockedBootloader = SystemPropertiesUtil.getProperty(
            "ro.boot.flash.locked", "1"
        ) == "0" || SystemPropertiesUtil.getProperty(
            "ro.boot.verifiedbootstate", "green"
        ) == "orange"

        val aSlotOnly = SystemPropertiesUtil.getProperty("ro.build.ab_update") == "false"

        val hasRootPermission = checkRoot()

        setContent {
            BootFlasherTheme {
                if (!unlockedBootloader || !hasRootPermission) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp),
                        verticalArrangement = Arrangement.SpaceEvenly,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (!unlockedBootloader) {
                            ErrorComponent(
                                errorMessage = stringResource(id = R.string.not_unlock_bootloader)
                            )
                        }

                        if (!hasRootPermission) {
                            ErrorComponent(
                                errorMessage = stringResource(id = R.string.not_have_root)
                            )
                        }
                    }

                    return@BootFlasherTheme
                }

                Scaffold {
                    BootFlasherApp(modifier = Modifier.padding(it), aSlotOnly = aSlotOnly)
                }
            }
        }
    }
}

/*
* Check if your device has root permissions
* */
private fun checkRoot(): Boolean {
    return try {
        Runtime.getRuntime().exec("su")
        true
    } catch (e: Exception) {
        false
    }
}