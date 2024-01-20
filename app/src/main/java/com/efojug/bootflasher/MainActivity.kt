package com.efojug.bootflasher

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.core.app.ActivityCompat
import com.efojug.bootflasher.ui.BootFlasherApp
import com.efojug.bootflasher.ui.component.ConfirmableErrorComponent
import com.efojug.bootflasher.ui.component.ErrorComponent
import com.efojug.bootflasher.ui.theme.BootFlasherTheme
import com.efojug.bootflasher.util.SystemPropertiesUtil

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val permissions = arrayOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )

        val needRequestPermissions = permissions
            .map {
                ActivityCompat.checkSelfPermission(
                    this,
                    it
                ) != PackageManager.PERMISSION_GRANTED
            }
            .any { it }

        if (needRequestPermissions) {
            ActivityCompat.requestPermissions(this, permissions, 1145)
        }

        val hasRootPermission = checkRoot()

        var hasManageFilePermission by mutableStateOf(Environment.isExternalStorageManager())

        val launcher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                hasManageFilePermission = Environment.isExternalStorageManager()
            }

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
                    if (!unlockedBootloader || !hasRootPermission || !hasManageFilePermission) {
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

                            if (!hasManageFilePermission) {
                                ConfirmableErrorComponent(
                                    errorMessage = stringResource(id = R.string.no_file_permissions),
                                    confirmText = stringResource(
                                        id = R.string.request_permission
                                    )
                                ) {
                                    val intent =
                                        Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)

                                    intent.setData(Uri.parse("package:$packageName"))
                                    launcher.launch(intent)
                                }
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