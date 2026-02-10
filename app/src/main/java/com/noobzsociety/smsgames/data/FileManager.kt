package com.noobzsociety.smsgames.data

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import java.io.File
import java.io.InputStream

class FileManager(private val context: Context) {

    private val filesDir = context.filesDir.resolve("imported_files")
    val files: MutableStateFlow<List<File>> = MutableStateFlow(emptyList())

    init {
        if (!filesDir.exists()) {
            filesDir.mkdirs()

        } else {
            files.update { filesDir.listFiles()?.toList() ?: emptyList() }
        }
    }

    fun isFileNameValid(fileName: String): Boolean {
        return fileName.isNotEmpty() && !fileName.contains(File.separator) && !fileName.contains("..")
    }

    fun insertFile(uri: Uri, newName: String) {
        val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
        inputStream?.use { input ->
            val file = File(filesDir, newName)
            file.outputStream().use { output ->
                input.copyTo(output)
            }

            files.update {
                it + file
            }
        }
    }

    fun deleteFile(fileName: String) {
        val file = File(filesDir, fileName)
        file.delete()

        files.update {
            it - file
        }
    }

    fun readFile(fileName: String): String = File(filesDir, fileName).readText()
}