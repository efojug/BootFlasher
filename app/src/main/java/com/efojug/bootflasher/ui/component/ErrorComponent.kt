package com.efojug.bootflasher.ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.efojug.bootflasher.R

@Composable
fun ErrorComponent(modifier: Modifier = Modifier, errorMessage: String) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = errorMessage,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun ConfirmableErrorComponent(
    modifier: Modifier = Modifier,
    errorMessage: String,
    confirmText: String,
    onConfirm: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        ErrorComponent(errorMessage = errorMessage, modifier = modifier)

        Spacer(modifier = Modifier.padding(6.dp))

        Button(onClick = { onConfirm() }) {
            Text(text = confirmText)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ConfirmableErrorComponentPreview() {
    ConfirmableErrorComponent(
        errorMessage = stringResource(id = R.string.not_unlock_bootloader),
        confirmText = "Confirm"
    ) {
    }
}

@Preview(showBackground = true)
@Composable
private fun NotRootPagePreview() {
    ErrorComponent(
        modifier = Modifier.fillMaxSize(),
        errorMessage = stringResource(id = R.string.not_have_root)
    )
}