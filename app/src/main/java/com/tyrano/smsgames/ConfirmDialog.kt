package com.tyrano.smsgames

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.tyrano.smsgames.ui.theme.SMSGamesTheme

@Composable
fun ConfirmDialog(
    title: String,
    content: String,
    confirm: String,
    dismiss: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        modifier = Modifier.fillMaxWidth(),
        onDismissRequest = {
            onDismiss()
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    title,
                    modifier = Modifier.padding(top = 5.dp, bottom = 15.dp),
                    color = Color.Black
                )
                Text(content)
            }
        },
        confirmButton = {
            Button(onClick = { onDismiss() }) {
                Text(dismiss)
            }
        },
        dismissButton = {
            Button(onClick = { onConfirm() }) {
                Text(confirm)
            }
        }
    )
}


@Preview(showBackground = true)
@Composable
fun ConfirmDialogPreview() {
    SMSGamesTheme {
        ConfirmDialog(
            title = "Titre d'exemple",
            content = "Machin truc message de fou qui fait deux lignes",
            confirm = "Confirmer",
            dismiss = "Annuler",
            onConfirm = { /*TODO*/ },
            onDismiss = {/*TODO*/ })
    }
}