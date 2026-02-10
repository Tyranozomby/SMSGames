package com.noobzsociety.smsgames.ui.screens.files

import android.net.Uri
import androidx.lifecycle.ViewModel
import com.noobzsociety.smsgames.data.FileManager

class FilesViewModel(
    private val fileManager: FileManager,
): ViewModel() {
    val files = fileManager.files

    fun deleteFile(fileName: String) {
        fileManager.deleteFile(fileName)
    }

    fun insertFile(uri: Uri, newName: String) {
        fileManager.insertFile(uri, newName)
    }

    fun isFileNameValid(fileName: String): Boolean {
        return fileManager.isFileNameValid(fileName)
    }
}