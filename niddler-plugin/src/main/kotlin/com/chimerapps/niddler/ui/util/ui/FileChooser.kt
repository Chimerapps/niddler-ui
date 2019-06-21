package com.chimerapps.niddler.ui.util.ui

import java.io.File
import javax.swing.JFileChooser

fun chooseSaveFile(title: String, extension: String): File? {
    val dialog = JFileChooser()
    dialog.dialogTitle = title
    if (dialog.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
        return dialog.selectedFile
    }
    return null
}
