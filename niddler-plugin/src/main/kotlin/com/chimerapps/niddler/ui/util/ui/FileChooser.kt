package com.chimerapps.niddler.ui.util.ui

import com.intellij.openapi.fileChooser.FileChooserFactory
import com.intellij.openapi.fileChooser.FileSaverDescriptor
import java.io.File

fun chooseSaveFile(title: String, extension: String): File? {
    val descriptor = FileSaverDescriptor(title, "")
    val dialog = FileChooserFactory.getInstance().createSaveFileDialog(descriptor, null)
    val result = dialog.save(null, null)

    return result?.file
}
