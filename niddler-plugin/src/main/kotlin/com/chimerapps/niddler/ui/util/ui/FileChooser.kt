package com.chimerapps.niddler.ui.util.ui

import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.fileChooser.FileChooserFactory
import com.intellij.openapi.fileChooser.FileSaverDescriptor
import com.intellij.openapi.fileChooser.FileTypeDescriptor
import com.intellij.openapi.vfs.VirtualFile
import java.io.File

@Suppress("UNUSED_PARAMETER")
fun chooseSaveFile(title: String, extension: String): File? {
    val descriptor = FileSaverDescriptor(title, "")
    val dialog = FileChooserFactory.getInstance().createSaveFileDialog(descriptor, null)
    val result = dialog.save(null, null)

    return result?.file
}

fun chooseOpenFile(title: String): VirtualFile? {
    val descriptor = FileTypeDescriptor(title, ".xml")

    val dialog = FileChooserFactory.getInstance().createFileChooser(descriptor, null, null)
    val result = dialog.choose(null)

    return result.getOrNull(0)
}