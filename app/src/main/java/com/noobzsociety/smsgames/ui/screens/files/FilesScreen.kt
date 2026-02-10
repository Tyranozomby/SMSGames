package com.noobzsociety.smsgames.ui.screens.files

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.noobzsociety.smsgames.R
import com.noobzsociety.smsgames.ui.components.ScreenBase
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun FilesScreen(
    navHostController: NavHostController,
    viewModel: FilesViewModel = koinViewModel(),
) {
    val files by viewModel.files.collectAsState()

    var showRenameDialog by remember { mutableStateOf(false) }
    var newFileName by remember { mutableStateOf("") }
    var uriToSave: Uri? by remember { mutableStateOf(null) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            uriToSave = it
            newFileName = it.lastPathSegment ?: "unknown_file"
            showRenameDialog = true
        }
    }

    ScreenBase(navHostController, stringResource(R.string.title_files)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Button(onClick = { launcher.launch("*/*") }) {
                Text("Ajouter un fichier")
            }
            Spacer(modifier = Modifier.height(8.dp))
            LazyColumn {
                items(files.map { it.name }) { fileName ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = fileName, modifier = Modifier.padding(vertical = 4.dp))
                        IconButton(onClick = {
                            viewModel.deleteFile(fileName)
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = "Supprimer")
                        }
                    }
                }
            }
        }
    }

    if (showRenameDialog) {
        AlertDialog(
            onDismissRequest = {
                showRenameDialog = false
                uriToSave = null
            },
            title = { Text("Renommer le fichier") },
            text = {
                TextField(
                    value = newFileName,
                    onValueChange = { newFileName = it },
                    placeholder = { Text(newFileName) }
                )
            },
            confirmButton = {
                Button(onClick = {
                    if (viewModel.isFileNameValid(newFileName)) {
                        uriToSave?.let { uri ->
                            viewModel.insertFile(uri, newFileName)
                            newFileName = ""
                            showRenameDialog = false
                            uriToSave = null
                        }
                    } else {
                        // Show an error message or handle invalid file name
                    }
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                Button(onClick = {
                    showRenameDialog = false
                    uriToSave = null
                }) {
                    Text("Annuler")
                }
            }
        )
    }
}

//private fun getFilesDir(context: Context): File {
//    return context.filesDir
//        .resolve("imported_files")
//        .apply {
//            if (!exists()) {
//                mkdirs()
//            }
//        }
//}
//
//private fun getFilesInAppStorage(context: Context): List<String> {
//    val filesDir = getFilesDir(context)
//    return filesDir.listFiles()?.map { it.name } ?: emptyList()
//}
//
//private fun addFileToAppStorage(context: Context, uri: Uri, newFileName: String, onFileAdded: (String) -> Unit) {
//    val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
//    inputStream?.use { input ->
//        val file = File(getFilesDir(context), newFileName)
//        file.outputStream().use { output ->
//            input.copyTo(output)
//        }
//        onFileAdded(newFileName)
//    }
//}
//
//private fun deleteFile(context: Context, fileName: String) {
//    val file = File(getFilesDir(context), fileName)
//    file.delete()
//}
//
//private fun isValidFileName(fileName: String): Boolean {
//    // Check for invalid characters or sequences like "../"
//    return !fileName.contains("..") && !fileName.contains("/") && !fileName.contains("\\")
//}
