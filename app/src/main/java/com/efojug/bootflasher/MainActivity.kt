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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.efojug.bootflasher.ui.BootFlasherApp
import com.efojug.bootflasher.ui.component.ConfirmableErrorComponent
import com.efojug.bootflasher.ui.component.ErrorComponent
import com.efojug.bootflasher.ui.theme.BootFlasherTheme
import com.efojug.bootflasher.util.SystemPropertiesUtil

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val hasRootPermission = checkRoot()

        setContent {
            var unlockedBootloader by remember {
                mutableStateOf(
                    SystemPropertiesUtil.getProperty(
                        "ro.boot.flash.locked", "1"
                    ) == "0" || SystemPropertiesUtil.getProperty(
                        "ro.boot.verifiedbootstate", "green"
                    ) == "orange"
                )
            }

            BootFlasherTheme {
                Scaffold {
                    if (!unlockedBootloader || !hasRootPermission) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(it)
                                .padding(8.dp),
                            verticalArrangement = Arrangement.SpaceEvenly,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            if (!hasRootPermission) {
                                ErrorComponent(
                                    errorMessage = stringResource(id = R.string.not_have_root)
                                )
                            }

                            if (!unlockedBootloader) {
                                ConfirmableErrorComponent(
                                    errorMessage = stringResource(id = R.string.not_unlock_bootloader),
                                    confirmText = stringResource(id = R.string.confirm_unlock_bootloader)
                                ) {
                                    unlockedBootloader = true
                                }
                            }
                        }

                        return@Scaffold
                    }

                    BootFlasherApp(modifier = Modifier.padding(it))
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